package org.cy3sbml.gui;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.net.URL;
import java.util.*;

import org.codefx.libfx.control.webview.WebViewHyperlinkListener;
import org.codefx.libfx.control.webview.WebViews;

import org.cy3sbml.util.AttributeUtil;
import org.cytoscape.model.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Handle hyperlink events in WebView.
 * Either opens browser for given hyperlink or triggers Cytoscape actions
 * for subsets of special hyperlinks.
 *
 * This provides an easy solution for integrating app functionality
 * with click on hyperlinks.
 * Alternative javascript upcalls could be performed.
 */
public class BrowserHyperlinkListener implements WebViewHyperlinkListener{
    private static final Logger logger = LoggerFactory.getLogger(BrowserHyperlinkListener.class);

    @Override
    public boolean hyperlinkUpdate(HyperlinkEvent hyperlinkEvent) {
        logger.info(WebViews.hyperlinkEventToString(hyperlinkEvent));

        // clicked url
        URL url = hyperlinkEvent.getURL();
        Boolean cancel = processURLEvent(url);
        return cancel;
    }

    /**
     * Processes the given url.
     * Decides what to do if a given URL is encountered.
     * Here the actions are called.
     *
     * @param url
     * @return cancel action, i.e. is the WebView event further processed
     */
    private static Boolean processURLEvent(URL url){
        if (url != null) {
            String s = url.toString();

            openURLinExternalBrowser(s);
            return true;
        }
        // This is a link we should load, do not cancel.
        return false;
    }

    /** Open url in external webView. */
    public static void openURLinExternalBrowser(String url){
        logger.debug("Open in external webView <" + url +">");
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                OpenBrowser.openURL(url);
            }
        });
    }

    /**
     * Returns the first matching node.
     * @param network
     * @param attribute
     * @param identifier
     * @return
     */
    private static CyNode getNodeByAttribute(CyNetwork network, String attribute, String identifier) {
        logger.info("Searching for node in network");
        Collection<CyRow> rows = network.getDefaultNodeTable().getMatchingRows(attribute, identifier);
        CyNode node = null;
        if (rows != null && rows.size()>0){
            // return first matching one
            CyRow row = rows.iterator().next();
            node = network.getNode(row.get(CyTable.SUID, Long.class));
        } else {
            logger.info(String.format("node not in current network: %s:%s", attribute, identifier));
        }
        return node;
    }

    /**
     * Selects given node in network.
     * Unselects all other nodes.
     * @param network
     * @param node
     */
    private static void selectNodeInNetwork(CyNetwork network, CyNode node){
        if (node != null) {
            // unselect all
            List<CyNode> nodes = CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true);
            for (CyNode n : nodes) {
                AttributeUtil.set(network, n, CyNetwork.SELECTED, false, Boolean.class);
            }
            // select node
            logger.info("selected node");
            AttributeUtil.set(network, node, CyNetwork.SELECTED, true, Boolean.class);
        }
    }

}
