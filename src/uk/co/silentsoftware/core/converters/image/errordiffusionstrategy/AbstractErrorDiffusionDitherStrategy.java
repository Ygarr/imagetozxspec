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
package uk.co.silentsoftware.core.converters.image.errordiffusionstrategy;

import java.awt.image.BufferedImage;

import uk.co.silentsoftware.config.OptionsObject;
import uk.co.silentsoftware.core.helpers.ColourHelper;

public abstract class AbstractErrorDiffusionDitherStrategy {

	/**
	 * Calculates the error diffusion given by the fraction against
	 * the diffused pixel. The error is taken from the currently 
	 * processed pixel whose original colour and new Spectrum colour 
	 * values are given in original and new pixel respectively.
	 * A percentage of the error is then applied to the diffusePixel
	 * as per the error distribution around the currently processed pixel.
	 * 
	 * @param oldPixel
	 * @param newPixel
	 * @param diffusePixel
	 * @param fraction
	 * @return
	 */
	protected int calculateAdjustedRGB(int oldPixel, int newPixel, int diffusePixel, double fraction) {	
		
		int[] oldRgb = ColourHelper.intToRgbComponents(oldPixel);	
		int[] newRgb = ColourHelper.intToRgbComponents(newPixel);
		int[] diffusedRgb = ColourHelper.intToRgbComponents(diffusePixel);
		
		// Calculate the error (difference) in each channel
		// between the old colour and new colour
		double redError = oldRgb[0] - newRgb[0];
		double greenError = oldRgb[1] - newRgb[1];
		double blueError = oldRgb[2] - newRgb[2];
		
		// Apply the given fraction of the error to the chosen
		// surrounding pixel's RGB value
		int red = Math.round(Math.round((diffusedRgb[0] + (fraction*redError))));
		int green = Math.round(Math.round((diffusedRgb[1] + (fraction*greenError))));
		int blue = Math.round(Math.round((diffusedRgb[2] + (fraction*blueError))));
		
		// Return the new RGB value
		return ColourHelper.intToAlphaRgb(red, green, blue);
	}
	
	/**
	 * Verify the x and y coordinates are within the image's width and height
	 * OR if constrained that the pixel being processed is not on attribute
	 * block boundary (we don't want the error to propagate as much)
	 * 
	 * @param image
	 * @param x
	 * @param y
	 * @return
	 */
	protected boolean isInBounds(BufferedImage image, int x, int y, Integer boundX, Integer boundY) {
		if (OptionsObject.getInstance().getConstrainedErrorDiffusion()) {
			if ((boundX != null && (!OptionsObject.getInstance().getSerpentine() && x >= boundX || (OptionsObject.getInstance().getSerpentine() && x <= boundX))) || boundY != null && y >= boundY) {
				return false;
			}
		}
		return ((x >= 0 && x < image.getWidth()) && (y >= 0 && y < image.getHeight())); 
	}
}
