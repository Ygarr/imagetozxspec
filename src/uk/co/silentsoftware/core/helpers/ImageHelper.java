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
package uk.co.silentsoftware.core.helpers;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;

import uk.co.silentsoftware.config.SpectrumDefaults;

/**
 * Helper class for basic image manipulation
 */
public final class ImageHelper {

	/**
	 * Private constructor since we want static use only
	 */
	private ImageHelper(){}
	
	/**
	 * Scale an image to a given width and height 
	 * 
	 * @param img
	 * @param width
	 * @param height
	 * @return
	 */
	public static BufferedImage quickScaleImage(Image img, int width, int height) {
		
		// Skip if resize not required or dimensions are -1 (no resize)
		if (img.getWidth(null) == width && img.getHeight(null) == height || -1 == width && -1 == height) {
			return ImageHelper.copyImage(img);
		}
		final Image scaled = img.getScaledInstance(width, height, BufferedImage.SCALE_FAST);
		BufferedImage copy = new BufferedImage(SpectrumDefaults.SCREEN_WIDTH, SpectrumDefaults.SCREEN_HEIGHT, BufferedImage.TYPE_INT_RGB);		
		copyImage(scaled, copy);
		return copy;
	}
	
	/**
	 * Convenience method for copying an image without
	 * passing in an image to copy to.
	 * 
	 * @param source
	 * @return
	 */
	public static BufferedImage copyImage(final Image source) {
		BufferedImage copy = new BufferedImage(source.getWidth(null), source.getHeight(null), BufferedImage.TYPE_INT_RGB);
		copyImage(source, copy);
		return copy;
	}
	
	/**
	 * Pixel for pixel copy of an image
	 * 
	 * @param source
	 * @param dest
	 */
	public static void copyImage(final Image source, BufferedImage dest) {
		 Graphics2D g = dest.createGraphics();
	     g.drawImage(source, null, null);
	     g.dispose();
	}
	
	/**
	 * Pixel for pixel copy of an image
	 * 
	 * @param source
	 * @param dest
	 */
	public static void copyImage(final Image source, BufferedImage dest, Point p) {
		 Graphics g = dest.getGraphics();
	     g.drawImage(source, p.x, p.y, null);
	     g.dispose();
	}
}
