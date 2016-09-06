package org.cy3sbml.gui;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.taverna.robundle.Bundle;
import org.apache.taverna.robundle.manifest.Manifest;
import org.apache.taverna.robundle.manifest.PathAnnotation;
import org.apache.taverna.robundle.manifest.PathMetadata;
import org.cy3sbml.ArchiveReaderTask;
import org.cy3sbml.BundleAnnotation;
import org.cy3sbml.BundleManager;
import org.cy3sbml.util.AttributeUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Updates the Panel information based on selection.
 */
public class BundlePanelUpdater implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(BundlePanelUpdater.class);

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

        // selected nodes
        List<CyNode> nodes = CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true);

        // information for selected node(s)
        Bundle bundle = bundleManager.getCurrentBundle();
        if (bundle == null){
            System.out.println("No bundle for current network");
            panel.setText(TEMPLATE_NO_BUNDLE);
        } else {
            BundleAnnotation bundleAnnotation = bundleManager.getCurrentBundleAnnotation();
            Map<String, List<String>> pathAnnotations = bundleAnnotation.getPathAnnotations();

            // Get annotation for node (default to root)
            String path = "/";
            // TODO: get root path

            if (nodes != null && nodes.size() > 0) {
                CyNode n = nodes.get(0);
                path = AttributeUtil.get(network, n, ArchiveReaderTask.NODE_ATTR_PATH, String.class);
            }

            // create html
            // TODO: proper formating
            String text = String.format("<h1>%s</h1>\n", path);
            // TODO: link to file
            if (pathAnnotations != null && pathAnnotations.containsKey(path)){
                for (String s : pathAnnotations.get(path)) {
                    text += String.format("<p><span class=\"code\">%s</span></p>",
                            StringEscapeUtils.escapeHtml(s));
                }
            }
            panel.setText(text);
        }

    }

}
