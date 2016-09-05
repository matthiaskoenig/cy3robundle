package org.cy3sbml.gui;

import org.apache.taverna.robundle.Bundle;
import org.apache.taverna.robundle.manifest.Manifest;
import org.apache.taverna.robundle.manifest.PathAnnotation;
import org.apache.taverna.robundle.manifest.PathMetadata;
import org.cy3sbml.BundleManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

/**
 * Updates the Panel information based on selection.
 */
public class BundlePanelUpdater implements Runnable {

    private static final String TEMPLATE_NO_NODE =
            "<h2>No information</h2>" +
            "<p>No SBML object registered for node in ObjectMapper.</p>" +
            "<p>Some nodes do not have SBase objects associated with them, e.g." +
            "the <code>AND</code> and <code>OR</code> nodes in the FBC package.</p>" +
            "<p>Other examples are the base units like <code>dimensionless</code>" +
            "or <code>mole</code> which are not part of the model.</p>";

    private static final String TEMPLATE_NO_BUNDLE =
            "<h2>No information</h2>" +
            "<p>No bundle associated with current network.</p>";


    private static final String TEMPLATE_LOAD_WEBSERVICE =
            "<h2>Web Services</h2>" +
            "<p><i class=\"fa fa-spinner fa-spin fa-3x fa-fw\"></i>\n" +
            "Loading information from WebServices ...</p>";


    private BundlePanel panel;
	private CyNetwork network;

    public BundlePanelUpdater(BundlePanel panel, CyNetwork network) {
        this.panel = panel;
        this.network = network;
    }

    /**
     * Here the node information update is performed.
     * If multiple nodes are selected only the information for the first node is displayed.
     */
    public void run() {
        BundleManager bundleManager = BundleManager.getInstance();

        // selected node SUIDs
        LinkedList<Long> suids = new LinkedList<>();
        List<CyNode> nodes = CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true);
        for (CyNode n : nodes){
            suids.add(n.getSUID());
        }

        // information for selected node(s)
        Bundle bundle = bundleManager.getCurrentBundle();
        if (bundle != null){

            if (suids.size() > 0){
                // use first node
                Long suid = suids.get(0);
                // TODO: get the annotation for the given node
                // This is done via the annotations


                panel.setText(String.format("<h1>%s</h1>", suid));
                try {
                    Manifest manifest = bundle.getManifest();
                    PathMetadata metaData;

                    for (PathAnnotation a: manifest.getAnnotations()){
                        System.out.println(a);
                        List<URI> uris = a.getAboutList();

                        // TODO: get the annotation file
                        // Annotation: /metadata.rdf about /README.md

                        // TODO: read information from annotation file (RDF or JSON)
                        // recommends an XML serialization of the Resource Description Framework [35]

                    }


                } catch (IOException e){
                    e.printStackTrace();
                }




            } else {
                panel.setText(TEMPLATE_NO_NODE);
            }

        } else {
            panel.setText(TEMPLATE_NO_BUNDLE);
        }
    }

}
