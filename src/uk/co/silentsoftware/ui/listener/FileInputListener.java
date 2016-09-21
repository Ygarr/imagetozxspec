/* Image to ZX Spec
 * Copyright (C) 2014 Silent Software (Benjamin Brown)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package uk.co.silentsoftware.ui.listener;

import static uk.co.silentsoftware.config.LanguageSupport.getCaption;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import uk.co.silentsoftware.ui.ImageToZxSpec;

/**
 * Listener to show the Image To ZX Spec file input selection dialog
 */
public class FileInputListener implements ActionListener {
	
	private final Component parentComponent;
	
	public FileInputListener(Component parentComponent) {
		this.parentComponent = parentComponent;	
	}
	
	public void actionPerformed(ActionEvent ae) {
		JFileChooser jfc = new JFileChooser(){
			static final long serialVersionUID = 1L;
			public void approveSelection() {
				for (File f:this.getSelectedFiles()) {
					if (f.isDirectory()) {
						return;
					}
				}
				super.approveSelection();
			}
		};
		jfc.setDialogTitle(getCaption("dialog_choose_input"));
		jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		if (ImageToZxSpec.getInFiles() != null && ImageToZxSpec.getInFiles().length > 0) {
			jfc.setCurrentDirectory(ImageToZxSpec.getInFiles()[0].getParentFile());
		}
		jfc.setAcceptAllFileFilterUsed(false);
		jfc.setFileFilter(new FileFilter() {
			
			public String getDescription() {
				return getCaption("dialog_file_formats");
			}
			public boolean accept(File f) {
				return ImageToZxSpec.isSupported(f);
			}
		});
		
		jfc.setMultiSelectionEnabled(true);
		if (JFileChooser.APPROVE_OPTION == jfc.showOpenDialog(parentComponent)) {
			ImageToZxSpec.setInFiles(jfc.getSelectedFiles());
			ImageToZxSpec.processPreview();
		}
	}
}