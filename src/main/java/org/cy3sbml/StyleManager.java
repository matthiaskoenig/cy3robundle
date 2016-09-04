package org.cy3sbml;

import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.task.read.LoadVizmapFileTaskFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Set;

/**
 * Class to load visual styles.
 * The style manager is a singleton class.
 */
public class StyleManager implements SessionLoadedListener {
    private static final Logger logger = LoggerFactory.getLogger(StyleManager.class);
    private static StyleManager uniqueInstance;

    private LoadVizmapFileTaskFactory loadVizmapFileTaskFactory;
    private VisualMappingManager vmm;

    public static final String[] STYLES = {ArchiveReaderTask.ARCHIVE_STYLE};

    public static synchronized StyleManager getInstance(LoadVizmapFileTaskFactory loadVizmapFileTaskFactory,
                                                        VisualMappingManager vmm) {
        if (uniqueInstance == null) {
            uniqueInstance = new StyleManager(loadVizmapFileTaskFactory, vmm);
        }
        return uniqueInstance;
    }

    /**
     * Constructor.
     */
    private StyleManager(LoadVizmapFileTaskFactory loadVizmapFileTaskFactory, VisualMappingManager vmm) {
        logger.debug("SBMLStyleManager created");
        this.loadVizmapFileTaskFactory = loadVizmapFileTaskFactory;
        this.vmm = vmm;
    }

    /**
     * Load the visual styles of the app.
     */
    public void loadStyles() {
        for (String styleName : STYLES) {
            logger.info("Load visual style: " + styleName);
            String resource = String.format("/styles/%s.xml", styleName);
            InputStream styleStream = getClass().getResourceAsStream(resource);
            // Check if already existing
            VisualStyle style = getVisualStyleByName(vmm, styleName);
            if (styleName.equals(style.getTitle())) {
                continue;
            } else {
                loadVizmapFileTaskFactory.loadStyles(styleStream);
            }
        }
    }

    /**
     * Get the visual style by name.
     * If no style for given styleName exists, the default style is returned.
     * <p>
     * This is a fix until the function is implemented on the vmm
     * https://code.cytoscape.org/redmine/issues/2174
     */
    public static VisualStyle getVisualStyleByName(VisualMappingManager vmm, String styleName) {
        Set<VisualStyle> styles = vmm.getAllVisualStyles();
        for (VisualStyle style : styles) {
            if (style.getTitle().equals(styleName)) {
                return style;
            }
        }
        logger.debug("style [" + styleName + "] not in VisualStyles, default style used.");
        return vmm.getDefaultVisualStyle();
    }

    @Override
    public void handleEvent(SessionLoadedEvent e) {
        logger.debug("SessionAboutToBeLoadedEvent");
        loadStyles();
    }

}
