package org.cy3sbml.actions;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.HashSet;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.util.swing.FileChooserFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Importing SBML networks..
 */
public class ArchiveAction extends AbstractCyAction{
	private static final Logger logger = LoggerFactory.getLogger(ArchiveAction.class);
	private static final long serialVersionUID = 1L;

    private static final String ICON_IMPORT = "/gui/images/import.png";
    private static final String DESCRIPTION_IMPORT = "COMBINE Archive & ResearchObject import";
    private static final float GRAVITY_IMPORT = (float) 100.0;



	public ArchiveAction(){
		super(ArchiveAction.class.getSimpleName());

		ImageIcon icon = new ImageIcon(getClass().getResource(ICON_IMPORT));
		putValue(LARGE_ICON_KEY, icon);
		
		this.putValue(SHORT_DESCRIPTION, DESCRIPTION_IMPORT);
		setToolbarGravity(GRAVITY_IMPORT);
	}
		
	public boolean isInToolBar() {
		return true;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		logger.debug("actionPerformed()");
        System.out.println("read archive file");
		
		// open new file open dialog
		Collection<FileChooserFilter> filters = new HashSet<>();
		String[] extensions = {"", "zip", "omex", "ro"};
		filters.add(new FileChooserFilter("Archive files, Research Bundles, COMBINE Archives (*, *.zip, *.omex, *.ro)", extensions));

        /*
		File[] files = adapter.fileUtil.getFiles(adapter.cySwingApplication.getJFrame(), 
				DESCRIPTION_IMPORT, FileDialog.LOAD, filters);
		
		if ((files != null) && (files.length != 0)) {
			for (int i = 0; i < files.length; i++) {
				logger.info("Load: " + files[i].getName());
				TaskIterator iterator = adapter.loadNetworkFileTaskFactory.createTaskIterator(files[i]);
				adapter.synchronousTaskManager.execute(iterator);
			}
		}
         */
	}



}
