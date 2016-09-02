package org.cy3sbml;

import org.cytoscape.io.BasicCyFileFilter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.util.StreamUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.zip.ZipInputStream;

/**
 * Archive Filter class.
 * Which files and uris are accepted as archive files.
 */
public class ArchiveFileFilter extends BasicCyFileFilter {
    private static final Logger logger = LoggerFactory.getLogger(ArchiveFileFilter.class);

    /**
     * Constructor.
     */
    public ArchiveFileFilter(StreamUtil streamUtil) {
        super(
                new String[]{"zip", "omex", ""},
                new String[]{"application/zip", "application/octet-stream"},
                "Archive network reader (cy3robundle)",
                DataCategory.NETWORK,
                streamUtil
        );
    }

    /**
     * Indicates which URI the FileFilter accepts.
     *
     * @param uri URI to check
     * @param category
     * @return
     */
    @Override
    public boolean accepts(URI uri, DataCategory category) {
        try {
            return accepts(streamUtil.getInputStream(uri.toURL()), category);
        } catch (IOException e) {
            logger.error("Error while creating stream from uri", e);
            return false;
        }
    }

    /**
     * Indicates which streams the FileFilter accepts.
     *
     * @param stream
     * @param category
     * @return
     */
    @Override
    public boolean accepts(InputStream stream, DataCategory category) {
        if (!category.equals(DataCategory.NETWORK)) {
            return false;
        }
        try {
            return checkZipStream(stream);
        } catch (IOException e) {
            logger.error("Error while checking header", e);
            return false;
        }
    }

    /**
     * Checks if the stream is a zip stream of an archive.
     *
     * @param stream
     * @return
     * @throws IOException
     */
    private boolean checkZipStream(InputStream stream) throws IOException {

        // Check if stream is zipped
        boolean isZipped = new ZipInputStream(stream).getNextEntry() != null;

        // additional checks ?
        return isZipped;
    }

}
