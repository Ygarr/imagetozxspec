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

import javax.swing.JFileChooser;

import uk.co.silentsoftware.ui.ImageToZxSpec;

/**
 * Listener to show the Image To ZX Spec file output selection dialog
 */
public class FileOutputListener implements ActionListener {
	
	private final Component parentComponent;
	
	public FileOutputListener(Component parentComponent) {
		this.parentComponent = parentComponent;
	}
	
	public void actionPerformed(ActionEvent ae) {
		JFileChooser jfc = new JFileChooser();
		jfc.setDialogTitle(getCaption("dialog_choose_output"));
		if (ImageToZxSpec.getOutFolder() != null) {
			jfc.setCurrentDirectory(ImageToZxSpec.getOutFolder());
		}
		jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (JFileChooser.APPROVE_OPTION == jfc.showSaveDialog(parentComponent)) {
			ImageToZxSpec.setOutFolder(jfc.getSelectedFile());
		}
	}
}
