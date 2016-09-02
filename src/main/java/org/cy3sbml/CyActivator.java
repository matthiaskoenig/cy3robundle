package org.cy3sbml;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;


import org.apache.xerces.util.XMLChar;
import org.cy3sbml.actions.ArchiveAction;
import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.task.read.LoadNetworkFileTaskFactory;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import org.cytoscape.service.util.AbstractCyActivator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Entry point to cy3sbml.
 * 
 * The CyActivator registers the cy3sbml services with OSGI. This is the class
 * used for startup of the app by Cytoscape 3.
 */
public class CyActivator extends AbstractCyActivator {
    private static Logger logger = LoggerFactory.getLogger(CyActivator.class);

	public CyActivator() {
		super();
	}
	
	/**
	 * Starts the cy3sbml OSGI bundle.
	 */
	@Override
	public void start(BundleContext bc) {
		try {
		    System.out.println("--------------------------------------");
            System.out.println("cy3robundle");
            System.out.println("--------------------------------------");
            BundleInformation bundleInfo = new BundleInformation(bc);

            // Loading extension bundles from resources
            String[] extensionBundles = {
                "extension/org.apache.xerces.extension-0.0.1.jar"
            };
            logger.info("Install extension bundle");
            Bundle bundle = bc.getBundle();
            for (String extensionBundle: extensionBundles){
                URL jarUrl = bundle.getEntry(extensionBundle);
                InputStream input = jarUrl.openStream();
                bc.installBundle(jarUrl.getPath(), input);
                input.close();
            }

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


            // Create archive reader
            ArchiveAction changeStateAction = new ArchiveAction();
            registerService(bc, changeStateAction, CyAction.class, new Properties());



            ///////////////////////////////////////////////
            /* Get services */
            CySwingApplication cySwingApplication = getService(bc, CySwingApplication.class);

            CyApplicationManager cyApplicationManager = getService(bc, CyApplicationManager.class);
            CyNetworkManager cyNetworkManager = getService(bc, CyNetworkManager.class);
            CyNetworkViewManager cyNetworkViewManager = getService(bc, CyNetworkViewManager.class);
            VisualMappingManager visualMappingManager = getService(bc, VisualMappingManager.class);
            CyLayoutAlgorithmManager cyLayoutAlgorithmManager = getService(bc, CyLayoutAlgorithmManager.class);

            DialogTaskManager dialogTaskManager = getService(bc, DialogTaskManager.class);
            @SuppressWarnings("rawtypes")
            SynchronousTaskManager synchronousTaskManager = getService(bc, SynchronousTaskManager.class);
            @SuppressWarnings("rawtypes")
            TaskManager taskManager = getService(bc, TaskManager.class);

            CyNetworkFactory cyNetworkFactory = getService(bc, CyNetworkFactory.class);
            CyNetworkViewFactory cyNetworkViewFactory = getService(bc, CyNetworkViewFactory.class);

            @SuppressWarnings("unchecked")
            CyProperty<Properties> cyProperties = getService(bc, CyProperty.class, "(cyPropertyName=cytoscape3.props)");
            @SuppressWarnings("unchecked")
            CyProperty<Properties> appProperties = getService(bc, CyProperty.class, "(cyPropertyName=cy3sbml.props)");

            OpenBrowser openBrowser = getService(bc, OpenBrowser.class);
            FileUtil fileUtil = getService(bc, FileUtil.class);
            LoadNetworkFileTaskFactory loadNetworkFileTaskFactory = getService(bc, LoadNetworkFileTaskFactory.class);
            ///////////////////////////////////////////////


            // Archive file reader
            StreamUtil streamUtil = getService(bc, StreamUtil.class);
            ArchiveFileFilter archiveFilter = new ArchiveFileFilter(streamUtil);

            ArchiveReaderTaskFactory archiveReaderTaskFactory = new ArchiveReaderTaskFactory(archiveFilter);
            Properties archiveReaderProps = new Properties();
            archiveReaderProps.setProperty("readerDescription", "Archive file reader (cy3robundle)");
            archiveReaderProps.setProperty("readerId", "archiveNetworkReader");
            registerAllServices(bc, archiveReaderTaskFactory, archiveReaderProps);


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

	@Override
    public void shutDown(){

    }
}

