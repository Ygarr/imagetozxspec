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

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import uk.co.silentsoftware.ui.ImageToZxSpec;

/**
 * Opens a message dialog containing the about information
 */
public class AboutListener implements ActionListener {

	private final static String HOME_PAGE = "http://www.silentsoftware.co.uk";
	
	@Override
	public void actionPerformed(ActionEvent e) {		
		long maxHeap = Runtime.getRuntime().maxMemory();
		long currentHeapUse = Runtime.getRuntime().totalMemory();
		int cpus = Runtime.getRuntime().availableProcessors();
		JTextPane aboutField = new JTextPane();
		aboutField.setContentType("text/html");
		aboutField.setText("Image to ZX Spec is a simple to use program which applies a Sinclair ZX Spectrum<br>" +
				"effect to images, creates Spectrum playable slideshows from images or \"video\"<br>" +
				"from compatible video files.<br><br>"+
				"This software is copyright Silent Software (Benjamin Brown), uses Caprica Software<br>"+
				"Limited's VLCJ 3.0.0,  Oracle's JMF 2.1.1e, the video only packages of Jffmpeg 1.1.0<br>"+
				"and XStream 1.4.4. These modules are all licenced and copyrighted separately. Jffmpeg<br>" +
				"does not have Ogg/Vorbis packages or any native code thus this program does not<br>"+
				"require the BSD licence that is part of the core Jffmpeg distribution.<br><br>" +
				"Processors: "+cpus+"<br>"+
				"Used Java Memory: "+currentHeapUse/1024/1024+"MB<br>"+
				"Total Java Memory: "+maxHeap/1024/1024+"MB<br><br>"+
				"Visit Silent Software at <a href='"+HOME_PAGE+"'>"+HOME_PAGE+"</a>");
		aboutField.putClientProperty(JTextPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		aboutField.setEditable(false);
		aboutField.setOpaque(false);
		aboutField.addHyperlinkListener(new HyperlinkListener() {
			
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (HyperlinkEvent.EventType.ACTIVATED == e.getEventType())	{
					openHomePage();
				}
				
			}
		});
		JOptionPane.showMessageDialog(null, aboutField, "About Image to ZX Spec", JOptionPane.INFORMATION_MESSAGE, ImageToZxSpec.IMAGE_ICON);
	}

	private static void openHomePage() {
		URI uri = null;
		try {
			uri = new URI(HOME_PAGE);
		} catch (URISyntaxException e) {
		}
		if (Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().browse(uri);
			} catch (IOException e) {
			}
		}
	}
}
