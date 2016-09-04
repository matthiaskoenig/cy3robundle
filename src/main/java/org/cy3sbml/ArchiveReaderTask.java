package org.cy3sbml;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipError;
import java.util.zip.ZipInputStream;

import org.apache.taverna.robundle.manifest.Agent;
import org.cy3sbml.util.AttributeUtil;
import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.*;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

import org.apache.taverna.robundle.Bundle;
import org.apache.taverna.robundle.Bundles;
import org.apache.taverna.robundle.manifest.Manifest;
import org.apache.taverna.robundle.manifest.PathAnnotation;
import org.apache.taverna.robundle.manifest.PathMetadata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create CyNetworks from Archives.
 *
 * This can be either a COMBINE Archive or ResearchObject,
 * or any other file type supported by taverna robundle.
 */
public class ArchiveReaderTask extends AbstractTask implements CyNetworkReader {
	private static final Logger logger = LoggerFactory.getLogger(ArchiveReaderTask.class);
    public static final String ARCHIVE_LAYOUT = "force-directed";

	private String fileName;
	private final InputStream stream;
	private final CyNetworkFactory networkFactory;

	private final CyNetworkViewFactory viewFactory;
    private final VisualMappingManager visualMappingManager;
    private final CyLayoutAlgorithmManager layoutAlgorithmManager;


	private CyRootNetwork rootNetwork;
	private CyNetwork network;       // global network of all SBML information
    private HashMap<String, CyNode> id2node;

    private TaskMonitor taskMonitor;

	/**
     * Constructor.
     */
	public ArchiveReaderTask(InputStream stream, String fileName,
                             CyNetworkFactory networkFactory,
                             CyNetworkViewFactory viewFactory,
                             VisualMappingManager visualMappingManager,
                             CyLayoutAlgorithmManager layoutAlgorithmManager) {

		this.stream = stream;
		this.fileName = fileName;
		this.networkFactory = networkFactory;
		this.viewFactory = viewFactory;
        this.visualMappingManager = visualMappingManager;
        this.layoutAlgorithmManager = layoutAlgorithmManager;

	}

    /**
     * Get created networks from reader.
     *
     * @return
     */
    @Override
    public CyNetwork[] getNetworks() {
        CyNetwork[] networks = { network };
        return networks;
    }

    /**
     * Build NetworkViews for given network.
     *
     * @param network
     * @return
     */
    @Override
    public CyNetworkView buildCyNetworkView(final CyNetwork network) {
        logger.debug("buildCyNetworkView");

        // Create view
        CyNetworkView view = viewFactory.createNetworkView(network);

        // Set style
        // VisualMappingManager only available in OSGI context
        /*
        if (visualMappingManager != null) {
            String styleName = "default";
            VisualStyle style = SBMLStyleManager.getVisualStyleByName(visualMappingManager, styleName);
            if (style != null) {
                visualMappingManager.setVisualStyle(style, view);
            }
        }
        */

        // layout
		if (layoutAlgorithmManager != null) {
			CyLayoutAlgorithm layout = layoutAlgorithmManager.getLayout(ARCHIVE_LAYOUT);
			if (layout == null) {
				layout = layoutAlgorithmManager.getLayout(CyLayoutAlgorithmManager.DEFAULT_LAYOUT_NAME);
				logger.warn(String.format("'{}' layout not found; default layout used.", ARCHIVE_LAYOUT));
			}
			TaskIterator itr = layout.createTaskIterator(view, layout.getDefaultLayoutContext(), CyLayoutAlgorithm.ALL_NODE_VIEWS, "");
			Task nextTask = itr.next();
			try {
				nextTask.run(taskMonitor);
			} catch (Exception e) {
				throw new RuntimeException("Could not finish layout", e);
			}
		}

        // finished
        return view;
    }


    /**
     * Cancel task.
     */
    @Override
    public void cancel() {}

    /**
     * Parse archive network.
     * @param taskMonitor
     * @throws Exception
     */
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		logger.debug("<--- Start Archive Reader --->");
        this.taskMonitor = taskMonitor;
		try {
			if (taskMonitor != null){
				taskMonitor.setTitle("archive reader");
				taskMonitor.setProgress(0.0);
			}
			if (cancelled){
				return;
			}

            id2node = new HashMap<>();


            /*
            The streamUtils transformed the InputStream to a ZipInputInstream

            if (source.toString().toLowerCase().endsWith(GZIP))
                newIs = new GZIPInputStream(proxyIs);
            else if (source.toString().toLowerCase().endsWith(ZIP))
                newIs = new ZipInputStream(proxyIs);
             */

            if (stream instanceof ZipInputStream){
                ZipInputStream zis = (ZipInputStream) stream;

                /// RO ///
                // read the RO manifest file
                // one central file describing the content
                //.ro/metadata.json

                // many files describing the individual metadata
                // metadata.rdf
                // metadata.json


                /// OMEX ///
                // read OMEX manifest file
                // only one central file describing
                // manifest.xml (content)
                // metadata.rdf (metadata about content)

                // read all entries from zip file
                ZipEntry ze = null;
                while ((ze = zis.getNextEntry()) != null) {
                    System.out.println("Unzipping " + ze.getName());

                    // write files
                    /*
                    FileOutputStream fout = new FileOutputStream(ze.getName());
                    for (int c = zin.read(); c != -1; c = zin.read()) {
                        fout.write(c);
                    }
                    */
                    zis.closeEntry();
                    //fout.close();
                }
                zis.close();
            } else {
                logger.error("Stream is not ZipInputStream");
                System.out.println(stream);
            }


			// Create empty root network and node map
			network = networkFactory.createNetwork();

			// To create a new CySubNetwork with the same CyNetwork's CyRootNetwork, cast your CyNetwork to
			// CySubNetwork and call the CySubNetwork.getRootNetwork() method:
			// 		CyRootNetwork rootNetwork = ((CySubNetwork)network).getRootNetwork();
			// CyRootNetwork also provides methods to create and add new subnetworks (see CyRootNetwork.addSubNetwork()).
			rootNetwork = ((CySubNetwork) network).getRootNetwork();


            //////////////////////////////////////////////////////////////////
            // Read information from manifest file
            //////////////////////////////////////////////////////////////////

            // Read archive
            try {
                Bundle bundle = Bundles.openBundle(stream);
                System.out.println(bundle);

                Manifest manifest = bundle.getManifest();
                System.out.println(manifest);
                System.out.println("CreatedBy: " + manifest.getCreatedBy());
                System.out.println("CreatedOn: " + manifest.getCreatedOn());

                System.out.println("<manifest>");
                List<Path> pathList = manifest.getManifest();
                for (Path p: pathList){
                    System.out.println(p);
                }

                // locations & aggregates (either files or uris)
                System.out.println("<aggregates>");
                List<PathMetadata> aggregates = manifest.getAggregates();
                for (PathMetadata metaData: aggregates){
                    System.out.println(metaData);
                    // create the single node
                    CyNode n = createNodeForPath(metaData);
                }

                // TODO: create intermediate nodes and edges




                System.out.println("<annotations>");
                for (PathAnnotation a: manifest.getAnnotations()){
                    System.out.println(a);
                }
                if (taskMonitor != null){
                    taskMonitor.setProgress(0.4);
                }

            }catch(ZipError e){
                logger.error("Could not read the zip file.");
                logger.error("Rename archives ending in *.zip with *.zip1");
                /*
                // The input stream could not be read as zip file.
                // This is for instance the case if *.zip ending
                // The resulting stream is than an
                //      BufferedInputStream(ZipInputStream)
                ZipInputStream zis = (ZipInputStream) stream;
                // read all entries from zip file
                ZipEntry ze = null;
                while ((ze = zis.getNextEntry()) != null) {
                    System.out.println("Unzipping " + ze.getName());

                    // write files

                    FileOutputStream fout = new FileOutputStream(ze.getName());
                    for (int c = zin.read(); c != -1; c = zin.read()) {
                        fout.write(c);
                    }

                    zis.closeEntry();
                    //fout.close();
                }
                zis.close();
                */
            }

			//////////////////////////////////////////////////////////////////
            // Base network
            //////////////////////////////////////////////////////////////////

            // Set naming
            String name = "Archive";
            rootNetwork.getRow(rootNetwork).set(CyNetwork.NAME, String.format("%s", name));
            network.getRow(network).set(CyNetwork.NAME, String.format("All: %s", name));

			if (taskMonitor != null){
				taskMonitor.setProgress(0.8);
			}
			logger.debug("<--- End Archive Reader --->");
			
		
		} catch (Throwable t){
			logger.error("Could not read Archive!", t);
			t.printStackTrace();
		}
	}

	public static final String AGGREGATE_TYPE_URI = "uri";
    public static final String AGGREGATE_TYPE_FILE = "file";
    public static final String TYPE_AGGREGATE = "aggregate";
    public static final String TYPE_FOLDER = "folder";


    public static final String NODE_ATTR_AGGREGATE_TYPE = "aggregate-type";
    public static final String NODE_ATTR_TYPE = "type";
    public static final String NODE_ATTR_NAME = "shared name";
    public static final String NODE_ATTR_FORMAT = "format";
    public static final String NODE_ATTR_MEDIATYPE = "mediatype";

    public static final String NODE_ATTR_AUTHORED_BY = "authoredBy";
    public static final String NODE_ATTR_AUTHORED_ON = "authoredOn";
    public static final String NODE_ATTR_CREATED_BY = "createdBy";
    public static final String NODE_ATTR_CREATED_ON = "createdOn";


	private CyNode createNodeForPath(PathMetadata md){
	    // Create single node
	    CyNode n = network.addNode();
        // Set attributes

        String id = null;
        if (md.getUri() != null) {
            id = md.getUri().toString();
            AttributeUtil.set(network, n, NODE_ATTR_AGGREGATE_TYPE, AGGREGATE_TYPE_URI, String.class);
            AttributeUtil.set(network, n, NODE_ATTR_TYPE, TYPE_AGGREGATE, String.class);
            AttributeUtil.set(network, n, NODE_ATTR_NAME, md.getUri().toString(), String.class);

        }
        if (md.getFile() != null) {
            id = md.getUri().toString();
            AttributeUtil.set(network, n, NODE_ATTR_AGGREGATE_TYPE, AGGREGATE_TYPE_FILE, String.class);
            AttributeUtil.set(network, n, NODE_ATTR_TYPE, TYPE_AGGREGATE, String.class);
            AttributeUtil.set(network, n, NODE_ATTR_NAME, md.getFile().toString(), String.class);
        }

        if (md.getConformsTo() != null){
            AttributeUtil.set(network, n, NODE_ATTR_FORMAT, md.getConformsTo().toString(), String.class);
        }

        if (md.getMediatype() != null) {
            AttributeUtil.set(network, n, NODE_ATTR_MEDIATYPE, md.getMediatype(), String.class);
        }

        if (md.getAuthoredBy() != null){
            String text = getAgentsString(md.getAuthoredBy());
            AttributeUtil.set(network, n, NODE_ATTR_AUTHORED_BY, text, String.class);
        }
        if (md.getAuthoredOn() != null){
            FileTime time = md.getAuthoredOn();
            AttributeUtil.set(network, n, NODE_ATTR_AUTHORED_ON, time.toString(), String.class);
        }
        if (md.getCreatedBy() != null){
            String text = getAgentString(md.getCreatedBy());
            AttributeUtil.set(network, n, NODE_ATTR_CREATED_BY, text, String.class);
        }
        if (md.getCreatedOn() != null){
            FileTime time = md.getCreatedOn();
            AttributeUtil.set(network, n, NODE_ATTR_CREATED_ON, time.toString(), String.class);
        }

        return n;
    }

    /**
     * Agents string representation.
     *
     * @param agents
     * @return
     */
    private String getAgentsString(List<Agent> agents){
        String text = "";
        for (Agent a: agents){
            text += getAgentString(a) + "; ";
        }
        return text;
    }

    /**
     * Create String for agent.
     *
     * @param agent
     * @return
     */
    private String getAgentString(Agent agent){
        String text = String.format(
                "%s (uri=%s, orcid=%s)",
                agent.getName(), agent.getUri(), agent.getOrcid());
        return text;
    }


}
