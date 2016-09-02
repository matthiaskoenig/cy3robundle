package org.cy3sbml;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipInputStream;

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
 * This can be either COMBINE Archives or ResearchObjects.
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


            /*
            The streamUtils transformed the InputStream to a ZipInputInstream

            if (source.toString().toLowerCase().endsWith(GZIP))
                newIs = new GZIPInputStream(proxyIs);
            else if (source.toString().toLowerCase().endsWith(ZIP))
                newIs = new ZipInputStream(proxyIs);
             */

            if (stream instanceof ZipInputStream){
                ZipInputStream zipStream = (ZipInputStream) stream;

            }

			// Read archive
            Bundle bundle = Bundles.openBundle(stream);
            System.out.println(bundle);

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

            Manifest manifest = bundle.getManifest();
            System.out.println(manifest);

            System.out.println("CreatedBy: " + manifest.getCreatedBy());
            System.out.println("CreatedOn: " + manifest.getCreatedOn());

            System.out.println("<manifest>");
            List<Path> pathList = manifest.getManifest();
            for (Path p: pathList){
                System.out.println(p);
            }

            System.out.println("<aggregates>");
            List<PathMetadata> aggregates = manifest.getAggregates();
            for (PathMetadata metaData: aggregates){
                System.out.println(metaData);
            }
            System.out.println("<annotations>");
            for (PathAnnotation a: manifest.getAnnotations()){
                System.out.println(a);
            }
			if (taskMonitor != null){
				taskMonitor.setProgress(0.4);
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

}
