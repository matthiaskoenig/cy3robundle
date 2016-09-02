package org.cy3sbml;

import java.io.IOException;
import java.io.InputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.AbstractInputStreamTaskFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * TaskFactory for ArchiveReaderTask.
 */
public class ArchiveReaderTaskFactory extends AbstractInputStreamTaskFactory {
    private static final Logger logger = LoggerFactory.getLogger(ArchiveReaderTaskFactory.class);

    private final CyNetworkFactory networkFactory;
    private final CyNetworkViewFactory networkViewFactory;
    private final VisualMappingManager visualMappingManager;
    private final CyLayoutAlgorithmManager layoutAlgorithmManager;

    /**
     * Constructor.
     */
    public ArchiveReaderTaskFactory(CyFileFilter filter,
                                    CyNetworkFactory networkFactory,
                                    CyNetworkViewFactory networkViewFactory,
                                    VisualMappingManager visualMappingManager,
                                    CyLayoutAlgorithmManager layoutAlgorithmManager) {
        super(filter);
        this.networkFactory = networkFactory;
        this.networkViewFactory = networkViewFactory;
        this.visualMappingManager = visualMappingManager;
        this.layoutAlgorithmManager = layoutAlgorithmManager;
    }

    /**
     * Create the TaskIterator.
     *
     * @param inputStream
     * @param inputName
     * @return
     */
    @Override
    public TaskIterator createTaskIterator(InputStream inputStream, String inputName) {
        logger.debug("createTaskIterator: input stream name: " + inputName);

        try {
            return new TaskIterator(
                    new ArchiveReaderTask(inputStream, inputName,
                            networkFactory,
                            networkViewFactory,
                            visualMappingManager,
                            layoutAlgorithmManager)
            );
        } catch (IOException e) {
            logger.error("Error in creating TaskIterator for ArchiveReaderTask.", e);
            e.printStackTrace();
            return null;
        }
    }
}
