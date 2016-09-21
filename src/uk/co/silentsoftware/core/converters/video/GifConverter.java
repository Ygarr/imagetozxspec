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
package uk.co.silentsoftware.core.converters.video;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.IIOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;

import org.w3c.dom.Node;

import uk.co.silentsoftware.config.OptionsObject;

/**
 * Simple ImageIO based GIF converter.
 * 
 * Note this converter holds a state and thus must be
 * initialised via createSequence and reset with 
 * createGif (which is intended to be the final call). 
 * This class is NOT thread safe!
 */
public class GifConverter {

	/**
	 * The anim gif's frames
	 */
	private List<BufferedImage> frames;
	
	/**
	 * Byte array required to save the created gif
	 */
	private ByteArrayOutputStream baos;
	
	/**
	 * Create the gif and clear down afterwards.
	 * 
	 * @return
	 * @throws Exception 
	 */
	public byte[] createGif() throws Exception {
		try {
			ImageWriter iw = ImageIO.getImageWritersByFormatName("gif").next();
			ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
			iw.setOutput(ios);
			iw.prepareWriteSequence(null);
			int p = 0;
			for (BufferedImage frame : frames) {
				ImageWriteParam iwp = iw.getDefaultWriteParam();
				IIOMetadata metadata = iw.getDefaultImageMetadata(new ImageTypeSpecifier(frame), iwp);
				configureMetaData(metadata, String.valueOf(OptionsObject.getInstance().getGifDisplayTimeMillis() / 10L), p++, 0);
				IIOImage ii = new IIOImage(frame, null, metadata);
				iw.writeToSequence(ii, null);
			}
			iw.endWriteSequence();
			ios.close();
			return baos.toByteArray();
		} finally {
			baos = null;
			frames = null;
		}
	}
	
	/**
	 * Adds a single buffered image to the gif being created.
	 * @param source
	 * @throws IOException
	 */
	public void addFrame(BufferedImage source) throws IOException {
		frames.add(source);	   
	}

	/**
	 * Create a new gif sequence
	 * 
	 * @param gifDisplayTimeMills
	 * @param loop
	 * @throws IIOException
	 * @throws IOException
	 */
	public void createSequence(int gifDisplayTimeMills) throws IIOException, IOException {
		baos = new ByteArrayOutputStream();
		
		// LinkedList used for performance reasons.
		frames = new LinkedList<>();
	}
	
	private void configureMetaData(IIOMetadata meta, String delayTime, int imageIndex, int loopCount) throws IIOInvalidTreeException {
		String metaFormat = meta.getNativeMetadataFormatName();
		Node root = meta.getAsTree(metaFormat);
		Node child = root.getFirstChild();
		while (child != null) {
			if ("GraphicControlExtension".equals(child.getNodeName()))
				break;
			child = child.getNextSibling();
		}
		IIOMetadataNode gce = (IIOMetadataNode) child;
		gce.setAttribute("userDelay", "FALSE");
		gce.setAttribute("delayTime", delayTime);
		gce.setAttribute("disposalMethod", "none");

		if (imageIndex == 0) {
			IIOMetadataNode aes = new IIOMetadataNode("ApplicationExtensions");
			IIOMetadataNode ae = new IIOMetadataNode("ApplicationExtension");
			ae.setAttribute("applicationID", "NETSCAPE");
	        ae.setAttribute("authenticationCode", "2.0");
			byte[] uo = new byte[] { 0x1, (byte) (loopCount & 0xFF), (byte) ((loopCount >> 8) & 0xFF) };
			ae.setUserObject(uo);
			aes.appendChild(ae);
			root.appendChild(aes);
		}
		meta.setFromTree(metaFormat, root);		
	}
}
