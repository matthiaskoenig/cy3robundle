package org.cy3sbml.util;


import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;

/**
 * Utils for working with networks.
 */
public class NetworkUtil {

    /**
     * Get SUID of root network.
     * Returns null if the network is null.
     */
    public static Long getRootNetworkSUID(CyNetwork network){
        Long suid = null;
        if (network != null){
            CyRootNetwork rootNetwork = ((CySubNetwork)network).getRootNetwork();
            suid = rootNetwork.getSUID();
        }
        return suid;
    }

    /**
     * Get rootNetwork for given network.
     */
    public static CyNetwork getRootNetwork(CyNetwork network){
        CyNetwork rootNetwork = null;
        if (network != null){
            rootNetwork = ((CySubNetwork)network).getRootNetwork();
        }
        return rootNetwork;
    }
}
