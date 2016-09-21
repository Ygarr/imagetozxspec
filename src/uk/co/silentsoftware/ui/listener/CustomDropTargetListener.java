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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import uk.co.silentsoftware.ui.ImageToZxSpec;

/**
 * Custom dnd listener to allow file drop to open preview window.
 * Uses a hack to check for broken drag and drop java support.
 */
public class CustomDropTargetListener implements DropTargetListener {

	private static final String URI_LIST_MIME_TYPE = "text/uri-list;class=java.lang.String";
	private static final String LINE_SEPARATOR_PATTERN = "\r\n|[\n\r\u2028\u2029\u0085\u000C]";
	private DataFlavor uriListFlavor;

	public CustomDropTargetListener() {
		try {
			uriListFlavor = new DataFlavor(URI_LIST_MIME_TYPE);
		} catch (ClassNotFoundException e) {
			// TODO: Uh oh - we'll just have to test for this case later :(
			uriListFlavor = null;
		}
	}
	
	/**
	 * Hack for Linux' broken drag and drop support for java
	 * 
	 * @param data
	 * @return
	 * @throws URISyntaxException 
	 */
	private static List<File> textURIListToFileList(String data) throws URISyntaxException {
		List<File> list = new ArrayList<File>(1);
		String[] lines = data.split(LINE_SEPARATOR_PATTERN);
		for (String line : lines) {
			
			// Comment line - useless for us.
			if (line.startsWith("#")) {
				continue;
			}
			File file = new File(new URI(line));
			list.add(file);
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void drop(DropTargetDropEvent dtde) {
		try {
			Transferable tr = dtde.getTransferable();
			DataFlavor[] flavours = tr.getTransferDataFlavors();
			
			// Iterate the flavours we have for this event
			for (DataFlavor flavour : flavours) {
			
				List<File> files = new ArrayList<File>(0);
				
				// Is the OS playing nicely? This is the correct data flavour
				// for what we want (seems only MS Windows plays ball here...)
				if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					if (flavour.isFlavorJavaFileListType()) {
						dtde.acceptDrop(DnDConstants.ACTION_REFERENCE);
						files = (List<File>) tr.getTransferData(flavour);
					}
				}
				// We've got ourselves a half-assed DnD implementation
				// that uses a uri list.
				else if (uriListFlavor != null && dtde.isDataFlavorSupported(uriListFlavor)) {
					if (flavour.isMimeTypeEqual(uriListFlavor)) {
						dtde.acceptDrop(DnDConstants.ACTION_REFERENCE);
				        String data = (String) tr.getTransferData(uriListFlavor);
				        files = textURIListToFileList(data);
					}
				}
				
				// We didn't get the desired flavour, keep trying until we do
				// (or run out).
				if (files.size() == 0) {
					continue;
				}
				
				// Filter unsupported files from our list of files.
				Iterator<File> iter = files.iterator();
				while (iter.hasNext()) {
					File f = iter.next();
					if (!ImageToZxSpec.isSupported(f)) {
						files.remove(f);
					}
				}
				
				// Only convert the first file.
				// TODO: Do we need to do more for drag and drop at this
				// point?
				File[] inFiles = files.toArray(new File[0]);
				ImageToZxSpec.setInFiles(inFiles);
				if (inFiles[0].isDirectory()) {
					ImageToZxSpec.setOutFolder(inFiles[0]);
				} else {
					ImageToZxSpec.setOutFolder(inFiles[0].getParentFile());
				}

				// For drag and drop we want feedback so we're showing the
				// preview dialog which will show the first file.
				ImageToZxSpec.showPreview();
				ImageToZxSpec.processPreview();
				
				// We got what we came for - don't bother to keep checking
				break;
			}
		} catch (Exception io) {
			JOptionPane
					.showMessageDialog(null, "A file error has occurred: "
							+ io.getMessage(), "Guru meditation",
							JOptionPane.OK_OPTION);
		} finally {
			dtde.dropComplete(true);
		}
	}
	
	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {
		System.out.println();
	}
	@Override
	public void dragOver(DropTargetDragEvent dtde) {
		System.out.println();
	}
	@Override
	public void dragExit(DropTargetEvent dte) {
		System.out.println();
	}
	@Override
	public void dragEnter(DropTargetDragEvent dtde) {
		System.out.println();
	}
}
