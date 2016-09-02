package org.cy3sbml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

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
        logger.info("new ArchiveReaderTaskFactory");
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
        logger.info("createTaskIterator: input stream name: " + inputName);

        ArchiveReaderTask task;
        try {
            task = new ArchiveReaderTask(
                    copyInputStream(inputStream),
                    inputName,
                    networkFactory,
                    networkViewFactory,
                    visualMappingManager,
                    layoutAlgorithmManager);
        } catch (IOException e){
            task = null;
            logger.error("Error copying stream", e);
        }
        return new TaskIterator(task);
    }

    /**
     * Copy InputStream.
     *
     * @param is
     * @return
     */
    private static InputStream copyInputStream(InputStream is) throws IOException {
        ByteArrayOutputStream copy = new ByteArrayOutputStream();
        int chunk = 0;
        byte[] data = new byte[1024*1024];
        while((-1 != (chunk = is.read(data)))) {
            copy.write(data, 0, chunk);
        }
        is.close();
        return new ByteArrayInputStream( copy.toByteArray() );
    }
}
