package org.cy3sbml;

import java.io.File;
import java.util.Properties;

import org.cy3sbml.gui.BundlePanel;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.task.read.LoadVizmapFileTaskFactory;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;
import org.cytoscape.view.model.events.NetworkViewAddedListener;
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

            // BundleManager
            CyApplicationManager cyApplicationManager = getService(bc, CyApplicationManager.class);
            BundleManager bundleManager = BundleManager.getInstance(cyApplicationManager);
            registerService(bc, bundleManager, NetworkAboutToBeDestroyedListener.class, new Properties());

            // panels

            BundlePanel bundlePanel = BundlePanel.getInstance(cySwingApplication, cyApplicationManager, appDirectory);
            registerService(bc, bundlePanel, CytoPanelComponent.class, new Properties());
            registerService(bc, bundlePanel, RowsSetListener.class, new Properties());
            registerService(bc, bundlePanel, SetCurrentNetworkListener.class, new Properties());
            registerService(bc, bundlePanel, NetworkAddedListener.class, new Properties());
            registerService(bc, bundlePanel, NetworkViewAddedListener.class, new Properties());
            registerService(bc, bundlePanel, NetworkViewAboutToBeDestroyedListener.class, new Properties());

            bundlePanel.activate();
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

