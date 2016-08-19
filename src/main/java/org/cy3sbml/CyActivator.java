package org.cy3sbml;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;


import org.apache.xerces.util.XMLChar;
import org.cytoscape.application.CyApplicationConfiguration;
import org.osgi.framework.BundleContext;

import org.cytoscape.service.util.AbstractCyActivator;


/**
 * Entry point to cy3sbml.
 * 
 * The CyActivator registers the cy3sbml services with OSGI. This is the class
 * used for startup of the app by Cytoscape 3.
 */
public class CyActivator extends AbstractCyActivator {

	public CyActivator() {
		super();
	}
	
	/**
	 * Starts the cy3sbml OSGI bundle.
	 */
	public void start(BundleContext bc) {
		try {
		    System.out.println("--------------------------------------");
            System.out.println("cy3robundle");
            System.out.println("--------------------------------------");
            BundleInformation bundleInfo = new BundleInformation(bc);

            // Default configuration directory used for all cy3sbml files
            CyApplicationConfiguration configuration = getService(bc, CyApplicationConfiguration.class);
            File cyDirectory = configuration.getConfigurationDirectoryLocation();
            File appDirectory = new File(cyDirectory, bundleInfo.getName());
            if(appDirectory.exists() == false) {
                appDirectory.mkdir();
            }

            // Extract all resource files for JavaFX (no bundle access)
            final ResourceExtractor resourceHandler = new ResourceExtractor(bc, appDirectory);
            resourceHandler.extract();


            // FIXME: xerces dependency nightmare
			// research object
            XMLChar c;

            System.out.println("--------------------------------------");
			System.out.println("Research Object");
			System.out.println("--------------------------------------");
            URI fileURI = ResourceExtractor.fileURIforResource("/ro/investigation-96-2.ro.zip");
            System.out.println("uri: " + fileURI);

			Path roPath = Paths.get(fileURI);
            System.out.println("path: " + roPath);
            System.out.println("read bundle");
			ROBundle.readBundle(roPath);

            System.out.println("--------------------------------------");
			
		} catch (Throwable e){
			System.out.println("Could not start server!");
			e.printStackTrace();
		}
	}
}

