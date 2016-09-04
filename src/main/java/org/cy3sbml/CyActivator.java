package org.cy3sbml;

import java.io.File;
import java.util.Properties;

import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.task.read.LoadVizmapFileTaskFactory;
import org.osgi.framework.BundleContext;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.task.read.LoadNetworkFileTaskFactory;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.SynchronousTaskManager;

import org.cy3sbml.actions.ArchiveAction;

import org.cytoscape.service.util.AbstractCyActivator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Read archive files into Cytoscape.
 *
 * Uses the robundle of the taverna-language supporting among others
 * Zip files, COMBINE archive files, ResearchObjects.
 *
 * TODO: visual styles & node images based on media type
 *
 * TODO: information panel (with option to open secondary files in browser & RDF information)
 * TODO: read secondary files (i.e. SBML & others)
 */
public class CyActivator extends AbstractCyActivator {
    private static Logger logger = LoggerFactory.getLogger(CyActivator.class);

	public CyActivator() {
		super();
	}
	
	/**
	 * Start OSGI bundle.
	 */
	@Override
	public void start(BundleContext bc) {
		try {
		    System.out.println("--------------------------------------");
            System.out.println("cy3robundle");
            System.out.println("--------------------------------------");
            BundleInformation bundleInfo = new BundleInformation(bc);

            // Default configuration directory
            CyApplicationConfiguration configuration = getService(bc, CyApplicationConfiguration.class);
            File cyDirectory = configuration.getConfigurationDirectoryLocation();
            File appDirectory = new File(cyDirectory, bundleInfo.getName());
            if(appDirectory.exists() == false) {
                appDirectory.mkdir();
            }

            // Extract resource files for JavaFX (no bundle access)
            final ResourceExtractor resourceHandler = new ResourceExtractor(bc, appDirectory);
            resourceHandler.extract();

            // load visual styles
            VisualMappingManager visualMappingManager = getService(bc, VisualMappingManager.class);
            LoadVizmapFileTaskFactory loadVizmapFileTaskFactory = getService(bc, LoadVizmapFileTaskFactory.class);
            StyleManager styleManager = StyleManager.getInstance(loadVizmapFileTaskFactory, visualMappingManager);
            styleManager.loadStyles();
            registerService(bc, styleManager, SessionLoadedListener.class, new Properties());


            // Archive action
            CySwingApplication cySwingApplication = getService(bc, CySwingApplication.class);
            FileUtil fileUtil = getService(bc, FileUtil.class);
            SynchronousTaskManager synchronousTaskManager = getService(bc, SynchronousTaskManager.class);
            LoadNetworkFileTaskFactory loadNetworkFileTaskFactory = getService(bc, LoadNetworkFileTaskFactory.class);

            ArchiveAction changeStateAction = new ArchiveAction(cySwingApplication, fileUtil,
                                                                loadNetworkFileTaskFactory, synchronousTaskManager);
            registerService(bc, changeStateAction, CyAction.class, new Properties());

            // Archive file reader
            CyLayoutAlgorithmManager layoutAlgorithmManager = getService(bc, CyLayoutAlgorithmManager.class);
            CyNetworkFactory networkFactory = getService(bc, CyNetworkFactory.class);
            CyNetworkViewFactory networkViewFactory = getService(bc, CyNetworkViewFactory.class);

            StreamUtil streamUtil = getService(bc, StreamUtil.class);
            ArchiveFileFilter archiveFilter = new ArchiveFileFilter(streamUtil);

            ArchiveReaderTaskFactory archiveReaderTaskFactory = new ArchiveReaderTaskFactory(
                    archiveFilter,
                    networkFactory,
                    networkViewFactory,
                    visualMappingManager,
                    layoutAlgorithmManager);
            Properties archiveReaderProps = new Properties();
            archiveReaderProps.setProperty("readerDescription", "Archive file reader (cy3robundle)");
            archiveReaderProps.setProperty("readerId", "archiveNetworkReader");
            registerAllServices(bc, archiveReaderTaskFactory, archiveReaderProps);

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

