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
import static uk.co.silentsoftware.config.LanguageSupport.getCaption;

import uk.co.silentsoftware.config.OptionsObject;

/**
 * Implementation of the Stucki error diffusion algorithm
 */
public class StuckiDitherStrategy extends AbstractErrorDiffusionDitherStrategy implements ErrorDiffusionDitherStrategy {

	private static final double TWENTY_ONETH = 1d/21d;
	private static final double TWO_TWENTY_ONETHS = 1d/21d;
	private static final double FOUR_TWENTY_ONETHS = 4d/21d;
	private static final double FOURTY_TWOTH = 4d/21d;
	
	/*
	 * {@inheritDoc}
	 */
	public void distributeError(BufferedImage output, int oldPixel, int newPixel, int x, int y, Integer boundX, Integer boundY) {
		int multiplier = 1;
		if (OptionsObject.getInstance().getSerpentine() && y % 2 == 0) {
			multiplier = -1;
		}
		if (isInBounds(output, x+1*multiplier, y, boundX, boundY)) {output.setRGB(x+1*multiplier, y, calculateAdjustedRGB(oldPixel, newPixel, output.getRGB(x+1*multiplier, y), FOUR_TWENTY_ONETHS));}
		if (isInBounds(output, x+2*multiplier, y, boundX, boundY)) {output.setRGB(x+2*multiplier, y, calculateAdjustedRGB(oldPixel, newPixel, output.getRGB(x+2*multiplier, y), TWO_TWENTY_ONETHS));}
		if (isInBounds(output, x-2*multiplier, y+1, boundX, boundY)) {output.setRGB(x-2*multiplier, y+1, calculateAdjustedRGB(oldPixel, newPixel, output.getRGB(x-2*multiplier, y+1), TWENTY_ONETH));}
		if (isInBounds(output, x-1*multiplier, y+1, boundX, boundY)) {output.setRGB(x-1*multiplier, y+1, calculateAdjustedRGB(oldPixel, newPixel, output.getRGB(x-1*multiplier, y+1), TWO_TWENTY_ONETHS));}
		if (isInBounds(output, x, y+1, boundX, boundY)) {output.setRGB(x, y+1, calculateAdjustedRGB(oldPixel, newPixel, output.getRGB(x, y+1), FOUR_TWENTY_ONETHS));}
		if (isInBounds(output, x+1*multiplier, y+1, boundX, boundY)) {output.setRGB(x+1*multiplier, y+1, calculateAdjustedRGB(oldPixel, newPixel, output.getRGB(x+1*multiplier, y+1), TWO_TWENTY_ONETHS));}
		if (isInBounds(output, x+2*multiplier, y+1, boundX, boundY)) {output.setRGB(x+2*multiplier, y+1, calculateAdjustedRGB(oldPixel, newPixel, output.getRGB(x+2*multiplier, y+1), TWENTY_ONETH));}
		if (isInBounds(output, x-2*multiplier, y+2, boundX, boundY)) {output.setRGB(x-2*multiplier, y+2, calculateAdjustedRGB(oldPixel, newPixel, output.getRGB(x-2*multiplier, y+2), FOURTY_TWOTH));}
		if (isInBounds(output, x-1*multiplier, y+2, boundX, boundY)) {output.setRGB(x-1*multiplier, y+2, calculateAdjustedRGB(oldPixel, newPixel, output.getRGB(x-1*multiplier, y+2), TWENTY_ONETH));}
		if (isInBounds(output, x, y+2, boundX, boundY)) {output.setRGB(x, y+2, calculateAdjustedRGB(oldPixel, newPixel, output.getRGB(x, y+2), TWO_TWENTY_ONETHS));}
		if (isInBounds(output, x+1*multiplier, y+2, boundX, boundY)) {output.setRGB(x+1*multiplier, y+2, calculateAdjustedRGB(oldPixel, newPixel, output.getRGB(x+1*multiplier, y+2), TWENTY_ONETH));}
		if (isInBounds(output, x+2*multiplier, y+2, boundX, boundY)) {output.setRGB(x+2*multiplier, y+2, calculateAdjustedRGB(oldPixel, newPixel, output.getRGB(x+2*multiplier, y+2), FOURTY_TWOTH));}		
	}
	
	@Override
	public String toString() {
		return "Stucki ("+getCaption("error_diffusion")+")";
	}
}
