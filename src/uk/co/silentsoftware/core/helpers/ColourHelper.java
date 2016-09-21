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

import static uk.co.silentsoftware.config.SpectrumDefaults.ATTRIBUTE_BLOCK_SIZE;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import uk.co.silentsoftware.config.OptionsObject;
import uk.co.silentsoftware.config.SpectrumDefaults;
import uk.co.silentsoftware.core.colourstrategy.ColourChoiceStrategy;
import uk.co.silentsoftware.core.converters.image.processors.GigaScreenAtrribute;

/**
 * Utility class to provide common colour functionality
 */
public final class ColourHelper {

	private static final int MAXIMUM_COMPONENT_VALUE = 255;
	
	private static final int[] WHITE = ColourHelper.intToRgbComponents(Color.WHITE.getRGB());
	
	private static final int[] BLACK = ColourHelper.intToRgbComponents(Color.BLACK.getRGB());

// TODO: Make configurable	
//	private static final int PREFER_DETAIL_COMPONENT_LOWER_LIMIT = 127;//102; //40% 
//
//	private static final int PREFER_DETAIL_COMPONENT_UPPER_LIMIT = 127;//154; //40%
	
	private static final int PREFER_DETAIL_COMPONENT_BOUNDARY = 127;
	
	/**
	 * Retrieves the spectrum colour most like the provided rgb colour
	 * 
	 * @param rgb
	 * @return
	 */
	public static int getClosestSpectrumColour(int rgb) {
		return getClosestColour(rgb, SpectrumDefaults.SPECTRUM_COLOURS_ALL);
	}

	/**
	 * Retrieves the spectrum colour most like the provided rgb colour
	 * 
	 * @param red
	 * @param green
	 * @param blue
	 * @return
	 */
	public static int getClosestSpectrumColour(int red, int green, int blue) {
		return getClosestColour(red, green, blue, SpectrumDefaults.SPECTRUM_COLOURS_ALL, OptionsObject.getInstance().getPreferDetail());
	}

	/**
	 * Retrieves the spectrum bright colour most like the provided rgb colour
	 * 
	 * @param rgb
	 * @return
	 */
	public static int getClosestBrightSpectrumColour(int rgb) {
		return getClosestColour(rgb, SpectrumDefaults.SPECTRUM_COLOURS_BRIGHT);
	}

	/**
	 * Retrieves the spectrum half bright colour most like the provided rgb
	 * colour which is NOT the excluded rgb
	 * 
	 * @param rgb
	 * @return
	 */
	public static int getClosestHalfBrightSpectrumColour(int rgb, int excludedRgb) {
		return getClosestColourWithExclusion(rgb, excludedRgb, SpectrumDefaults.SPECTRUM_COLOURS_HALF_BRIGHT);
	}

	/**
	 * Retrieves the spectrum half bright colour most like the provided rgb
	 * colour
	 * 
	 * @param rgb
	 * @return
	 */
	public static int getClosestHalfBrightSpectrumColour(int rgb) {
		return getClosestColour(rgb, SpectrumDefaults.SPECTRUM_COLOURS_HALF_BRIGHT);
	}

	/**
	 * Retrieves the reduced set spectrum half bright colour most like the
	 * provided rgb colour which is NOT the excluded rgb
	 * 
	 * @param rgb
	 * @return
	 */
	public static int getClosestReducedHalfBrightSpectrumColour(int rgb, int excludedRgb) {
		return getClosestColourWithExclusion(rgb, excludedRgb, SpectrumDefaults.SPECTRUM_COLOURS_REDUCED_HALF_BRIGHT);
	}

	/**
	 * Retrieves the reduced set spectrum half bright colour most like the
	 * provided rgb colour
	 * 
	 * @param rgb
	 * @return
	 */
	public static int getClosestReducedHalfBrightSpectrumColour(int rgb) {
		return getClosestColour(rgb, SpectrumDefaults.SPECTRUM_COLOURS_REDUCED_HALF_BRIGHT);
	}

	/**
	 * Retrieves the Gigascreen colour most like the provided rgb colour
	 * 
	 * @param red
	 * @param green
	 * @param blue
	 * @return
	 */
	public static int getClosestGigascreenColour(int red, int green, int blue) {
		return getClosestColour(red, green, blue, SpectrumDefaults.GIGASCREEN_COLOURS_ALL, OptionsObject.getInstance().getPreferDetail());
	}
	
	/**
	 * Retrieves the Gigascreen colour most like the provided rgb colour
	 * 
	 * @param rgb
	 * @return
	 */
	public static int getClosestGigascreenColour(int rgb) {
		return getClosestColour(rgb, SpectrumDefaults.GIGASCREEN_COLOURS_ALL);
	}

	/**
	 * Retrieves the from the colourSet most like the provided rgb colour
	 * 
	 * @param red
	 * @param green
	 * @param blue
	 * @param colourSet
	 * @return
	 */
	public static int getClosestColour(int red, int green, int blue, int[] colourSet, boolean preferDetail) {

		int bestMatch = Integer.MAX_VALUE;
		Integer closest = null;
		for (int colour : colourSet) {
			final int[] colourSetComps = intToRgbComponents(colour);
			int diff = Math.abs(red - colourSetComps[0]) + Math.abs(green - colourSetComps[1]) + Math.abs(blue - colourSetComps[2]);
			if (diff <= bestMatch) {
				closest = colour;
				bestMatch = diff;
			}
		}
//System.out.println("closest: "+Integer.toHexString(SpectrumDefaults.GIGASCREEN_ARGB.get(closest)[0])+" "+Integer.toHexString(SpectrumDefaults.GIGASCREEN_ARGB.get(closest)[1]));
		/**
		 * If we prefer detail then make more of the darker shades black.
		 */

		if (preferDetail) {
			int white = ColourHelper.getClosestColour(WHITE[0], WHITE[1], WHITE[2], colourSet, false);
			int black = ColourHelper.getClosestColour(BLACK[0], BLACK[1], BLACK[2], colourSet, false);
			if (red < PREFER_DETAIL_COMPONENT_BOUNDARY 
				&& green < PREFER_DETAIL_COMPONENT_BOUNDARY 
				&& blue < PREFER_DETAIL_COMPONENT_BOUNDARY) {
				return black;
			}
			if (red > PREFER_DETAIL_COMPONENT_BOUNDARY 
				&& green > PREFER_DETAIL_COMPONENT_BOUNDARY 
				&& blue > PREFER_DETAIL_COMPONENT_BOUNDARY) {
				return white;
			}
		}
		return closest;
	}

	public static GigaScreenAtrribute.GigaScreenColourCombo getClosestGigaScreenCombo(int rgb, GigaScreenAtrribute colourSet) {
		final int[] comps = ColourHelper.intToRgbComponents(rgb);
		int bestMatch = Integer.MAX_VALUE;
		Integer closestMatchPaletteIndex = null;
		int[] palette = colourSet.getPalette();
		for (int paletteIndex=0; paletteIndex<palette.length; ++paletteIndex) {
			int colour = palette[paletteIndex];
			final int[] colourSetComps = ColourHelper.intToRgbComponents(colour);
			int diff = Math.abs(comps[0] - colourSetComps[0]) + Math.abs(comps[1] - colourSetComps[1]) + Math.abs(comps[2] - colourSetComps[2]);
			if (diff < bestMatch) {
				closestMatchPaletteIndex = paletteIndex;
				bestMatch = diff;
			}
		}
		return colourSet.getColourCombo(closestMatchPaletteIndex);	
	}
	
	public static int getClosestColour(int rgb, int[] colourSet) {
		final int[] comps = intToRgbComponents(rgb);
		return getClosestColour(comps[0], comps[1], comps[2], colourSet, OptionsObject.getInstance().getPreferDetail());
	}

	
	public static int getClosestColourDistance(int red, int green, int blue, int[] colourSet) {

		int bestMatch = Integer.MAX_VALUE;
		for (int colour : colourSet) {
			final int[] colourSetComps = ColourHelper.intToRgbComponents(colour);
			int diff = Math.abs(red - colourSetComps[0]) + Math.abs(green - colourSetComps[1]) + Math.abs(blue - colourSetComps[2]);
			if (diff < bestMatch) {
				bestMatch = diff;
			}
		}
		return bestMatch;
	}
	
	/**
	 * Retrieves the colour from the colourSet most like the provided rgb colour
	 * 
	 * @param rgb
	 * @param colourSet
	 * @return
	 */
	private static int getClosestColourWithExclusion(int rgb, int excludedRgb, int[] colourSet) {

		int original[] = intToRgbComponents(rgb);

		int bestMatch = Integer.MAX_VALUE;
		int closest = colourSet[0];
		for (int colour : colourSet) {
			if (colour == excludedRgb) {
				continue;
			}
			int[] colourSetRgb = intToRgbComponents(colour);

			int diff = Math.abs(original[0] - colourSetRgb[0]) + Math.abs(original[1] - colourSetRgb[1]) + Math.abs(original[2] - colourSetRgb[2]);
			if (diff < bestMatch) {
				closest = colour;
				bestMatch = diff;
			}
		}
		return closest;
	}

	public static BufferedImage colourMonochromeAttributes(BufferedImage output, final BufferedImage original) {
		OptionsObject oo = OptionsObject.getInstance();
		for (int y = 0; y < output.getHeight(); ++y) {
			for (int x = 0; x < output.getWidth(); ++x) {
				int outRGB = output.getRGB(x, y);
				if (outRGB == SpectrumDefaults.BLACK) {
					outRGB = SpectrumDefaults.SPECTRUM_COLOURS_BRIGHT[oo.getMonochromeInkIndex()];
				} else {
					outRGB = SpectrumDefaults.SPECTRUM_COLOURS_BRIGHT[oo.getMonochromePaperIndex()];
				}
				output.setRGB(x, y, outRGB);
			}
		}
		return output;
	}

	/**
	 * Colours an entire image using the given colourstrategy based on the
	 * original and output images. Colours the Spectrum attribute blocks by
	 * selecting xMax by yMax parts of the output image (i.e. usually 8x8
	 * pixels), chooses the most popular two colours. The colour choice strategy
	 * then decides how to colour individual pixels based on these two colours.
	 * 
	 * Note it is expected that this method will be called AFTER the pixels have
	 * been changed to Spectrum colours.
	 * 
	 * @param output
	 * @param colourChoiceStrategy
	 * @return
	 */
	public static BufferedImage colourAttributes(BufferedImage output, final BufferedImage original, ColourChoiceStrategy colourChoiceStrategy) {

		final int maxElements = 102;
		Map<Integer, Integer> map = new HashMap<Integer, Integer>(maxElements);
		
		// Analyse block and choose the two most popular colours in attribute
		// block
		for (int y = 0; y + ATTRIBUTE_BLOCK_SIZE <= output.getHeight(); y += ATTRIBUTE_BLOCK_SIZE) {
			for (int x = 0; x + ATTRIBUTE_BLOCK_SIZE <= output.getWidth() && y + ATTRIBUTE_BLOCK_SIZE <= output.getHeight(); x += ATTRIBUTE_BLOCK_SIZE) {
				int outRgb[] = output.getRGB(x, y, ATTRIBUTE_BLOCK_SIZE, ATTRIBUTE_BLOCK_SIZE, null, 0, ATTRIBUTE_BLOCK_SIZE);
				map.clear();	
				for (int i=0; i<outRgb.length; ++i) {
					
					int value = ColourHelper.getClosestSpectrumColour(outRgb[i]); 					
					int count = 1;
					if (map.containsKey(value)) {
						count = map.get(value)+1;				
					}
					map.put(value, count);			
				}
				
				int mostPopularColour = calculateMostPopularColourWithExclusion(null, map);
				int secondMostPopularColour = calculateMostPopularColourWithExclusion(mostPopularColour, map);
				final int[] mostPopularComps = ColourHelper.intToRgbComponents(mostPopularColour);
				final int[] secondMostPopularComps = ColourHelper.intToRgbComponents(secondMostPopularColour);
									
				// Enforce attribute favouritism rules on the two spectrum
				// attribute colours (fixes the problem that colours could be from both the bright
				// and half bright set).
				int[] correctedAlphaColours = OptionsObject.getInstance().getAttributeMode().enforceAttributeRule(mostPopularComps, secondMostPopularComps);
							
				// Replace all colours in attribute block (which can be any spectrum colours) with the just the popular two
				for (int i = 0; i < outRgb.length; ++i) {
					outRgb[i] = colourChoiceStrategy.colour(outRgb[i], correctedAlphaColours);
				}
				output.setRGB(x, y, ATTRIBUTE_BLOCK_SIZE, ATTRIBUTE_BLOCK_SIZE, outRgb, 0, ATTRIBUTE_BLOCK_SIZE);
			}
		}
		return output;
	}
		
	public static int calculateMostPopularColourWithExclusion(Integer excludedColour, Map<Integer, Integer> colourCountTally) {
		Integer mostPopularColour = null;
		if (excludedColour != null) {
			mostPopularColour = excludedColour;
		}		
		Set<Integer>keySet = colourCountTally.keySet();
		colourCountTally.remove(excludedColour);
		int mostFrequent = -1;

		for (int colour : keySet) {
			int count = colourCountTally.get(colour);
			if (count >= mostFrequent) {
				mostPopularColour = colour;
				mostFrequent = count;
			}
		}
		return mostPopularColour;
	}

	/**
	 * Determines whether the colour is from the Spectrum's bright or half
	 * bright colour set.
	 * 
	 * @param argb
	 * @return
	 */
	public static boolean isBrightSet(int argb) {
		for (int i = 0; i < SpectrumDefaults.SPECTRUM_COLOURS_BRIGHT.length; ++i) {
			int def = SpectrumDefaults.SPECTRUM_COLOURS_BRIGHT[i];
			if (def == argb) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Changes the contrast of an image
	 * 
	 * @param img
	 * @param amount
	 * @return
	 */
	public static BufferedImage changeContrast(BufferedImage img, float amount) {
		if (amount == 1) {
			return img;
		}
		RescaleOp rescaleOp = new RescaleOp(amount, 0, null);
		return rescaleOp.filter(img, null);
	}



	/**
	 * Changes brightness by increasing all pixel values by a given amount
	 * 
	 * @param img
	 * @param amount
	 * @return
	 */
	public static BufferedImage changeBrightness(BufferedImage img, float amount) {
		if (amount == 0) {
			return img;
		}
		RescaleOp rescaleOp = new RescaleOp(1, amount, null);
		return rescaleOp.filter(img, null);
	}

	/**
	 * Changes image saturation by a given amount (0-1 range)
	 * 
	 * @param img
	 * @param amount
	 * @return
	 */
	public static BufferedImage changeSaturation(BufferedImage img, float amount) {
		if (amount == 0) {
			return img;
		}
		for (int y = 0; y < img.getHeight(); ++y) {
			for (int x = 0; x < img.getWidth(); ++x) {
				img.setRGB(x, y, changePixelSaturation(img.getRGB(x, y), amount));
			}
		}
		return img;
	}

	/**
	 * Changes the saturation of an individual pixel by the given amount (0-1
	 * range)
	 * 
	 * @param pixel
	 * @param amount
	 * @return
	 */
	private static int changePixelSaturation(int pixel, float amount) {
		int[] rgb = intToRgbComponents(pixel);
		float[] hsb = Color.RGBtoHSB(rgb[0], rgb[1], rgb[2], null);
		hsb[1] += amount;
		float saturation = correctRange(hsb[1], 0, 1);
		return Color.HSBtoRGB(hsb[0], saturation, hsb[2]);
	}

	/**
	 * Ensures a value is within a given range. If it exceeds or is below it is
	 * set to the high value or low value respectively
	 * 
	 * @param value
	 * @param low
	 * @param high
	 * @return
	 */
	static float correctRange(float value, int low, int high) {
		if (value < low) {
			return low;
		}
		if (value > high) {
			return high;
		}
		return value;
	}

	/**
	 * Convert rgb to its components
	 * 
	 * @param rgb
	 * @return
	 */
	public static int[] intToRgbComponents(int rgb) {
		return new int[] { rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF };
	}

	/**
	 * Convert individual RGB components into a 32 bit ARGB value
	 * 
	 * @param rgb
	 * @return
	 */
	public static int intToAlphaRgb(int[] rgb) {
		return intToAlphaRgb(rgb[0], rgb[1], rgb[2]);
	}

	/**
	 * Convert individual RGB components into a 32 bit ARGB value
	 * 
	 * @param red
	 * @param green
	 * @param blue
	 * @return
	 */
	public static int intToAlphaRgb(int red, int green, int blue) {
		return new Color(correctRange(red), correctRange(green), correctRange(blue)).getRGB();
	}

	/**
	 * Corrects and individual colour channel value's range to 0>channel<255
	 * 
	 * @param channel
	 * @return
	 */
	private static int correctRange(int channel) {
		return (int) ColourHelper.correctRange(channel, 0, MAXIMUM_COMPONENT_VALUE);
	}

	/**
	 * Determines whether a pixel is closer to black (than white)
	 * 
	 * @param red
	 * @param green
	 * @param blue
	 * @return
	 */
	public static boolean isBlack(int red, int green, int blue) {
		int colourTotal = red + green + blue;
		return colourTotal < OptionsObject.getInstance().getBlackThreshold();
	}

	/**
	 * Based on the darkness of the pixel colour determines whether a pixel is
	 * ink or paper and returns that colour. Used for converting colour to
	 * monochrome based on whether a pixel can be considered black using the
	 * isBlack threshold.
	 * 
	 * @param rgb
	 * @param ink
	 * @param paper
	 * @return
	 */
	public static int getMonochromeColour(int rgb, int ink, int paper) {
		int[] comps = intToRgbComponents(rgb);
		return getMonochromeColour(comps[0], comps[1], comps[2], ink, paper);
	}

	/**
	 * Based on the darkness of the pixel colour determines whether a pixel is
	 * ink or paper and returns that colour. Used for converting colour to
	 * monochrome based on whether a pixel can be considered black using the
	 * isBlack threshold.
	 * 
	 * @param red
	 * @param green
	 * @param blue
	 * @param ink
	 * @param paper
	 * @return
	 */

	public static int getMonochromeColour(int red, int green, int blue, int ink, int paper) {
		if (isBlack(red, green, blue))
			return ink;
		return paper;
	}

	/**
	 * Returns an array of black and white colours representing the ink (black)
	 * and paper (white) monochrome colours.
	 * 
	 * Opposite function to getMonochromeFromBlackAndWhite
	 * 
	 * @param original
	 * @return
	 */
	public static int[] getBlackAndWhiteFromMonochrome(int[] original) {
		int[] copy = Arrays.copyOf(original, original.length);
		for (int i = 0; i < copy.length; ++i) {
			if (copy[i] == SpectrumDefaults.SPECTRUM_COLOURS_BRIGHT[OptionsObject.getInstance().getMonochromePaperIndex()]) {
				copy[i] = Color.WHITE.getRGB();
			} else {
				copy[i] = Color.BLACK.getRGB();
			}
		}
		return copy;
	}

	/**
	 * Returns an array of monochrome (chosen ink and paper) colours based on an
	 * input array of black (ink) and white (paper).
	 * 
	 * Opposite function to getBlackAndWhiteFromMonochrome
	 * 
	 * @param original
	 * @return
	 */
	public static int[] getMonochromeFromBlackAndWhite(int[] original) {
		int[] copy = Arrays.copyOf(original, original.length);
		for (int i = 0; i < copy.length; ++i) {
			copy[i] = getMonochromeFromBlackAndWhite(copy[i]);
		}
		return copy;
	}

	public static int getMonochromeFromBlackAndWhite(int original) {
		OptionsObject oo = OptionsObject.getInstance();
		if (original == Color.WHITE.getRGB()) {
			return SpectrumDefaults.SPECTRUM_COLOURS_BRIGHT[oo.getMonochromePaperIndex()];
		}
		return SpectrumDefaults.SPECTRUM_COLOURS_BRIGHT[oo.getMonochromeInkIndex()];
	}
}
