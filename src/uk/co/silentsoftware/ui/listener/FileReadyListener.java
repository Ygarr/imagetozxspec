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

import javax.swing.JOptionPane;

import uk.co.silentsoftware.ui.ImageToZxSpec;

/**
 * Listener that wraps the input and output file listeners to provide
 * an indication as to whether it is okay to start processing (i.e.
 * input and output folders have been correctly set).
 */
public class FileReadyListener implements ActionListener {

	private final Component parentComponent;
	private final OperationFinishedListener operationFinishedListener;

	public FileReadyListener(Component parentComponent, OperationFinishedListener operationFinishedListener) {
		this.parentComponent = parentComponent;
		this.operationFinishedListener = operationFinishedListener;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (ImageToZxSpec.getInFiles() == null || ImageToZxSpec.getInFiles().length == 0) {
			JOptionPane.showMessageDialog(null, getCaption("dialog_choose_input_first"), getCaption("dialog_files_not_selected"), JOptionPane.OK_OPTION);
			new FileInputListener(parentComponent).actionPerformed(null);
		}
		if (ImageToZxSpec.getOutFolder() == null) {
			JOptionPane.showMessageDialog(null, getCaption("dialog_choose_folder_first"), getCaption("dialog_folder_not_selected"), JOptionPane.OK_OPTION);
			new FileOutputListener(parentComponent).actionPerformed(null);
		}
		if (operationFinishedListener != null) {
			operationFinishedListener.operationFinished(ImageToZxSpec.getInFiles() != null 
					&& ImageToZxSpec.getInFiles().length >0 && ImageToZxSpec.getOutFolder() != null);
		}
	}
}
