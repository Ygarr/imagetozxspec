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
package uk.co.silentsoftware.core.converters.image.processors;

import static uk.co.silentsoftware.config.SpectrumDefaults.ATTRIBUTE_BLOCK_SIZE;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import uk.co.silentsoftware.config.GigaScreenHSBOption;
import uk.co.silentsoftware.config.OptionsObject;
import uk.co.silentsoftware.core.attributestrategy.GigaScreenAttributeStrategy;
import uk.co.silentsoftware.core.converters.image.DitherStrategy;
import uk.co.silentsoftware.core.converters.image.ResultImage;
import uk.co.silentsoftware.core.converters.image.ResultImage.ResultImageType;
import uk.co.silentsoftware.core.converters.image.errordiffusionstrategy.ErrorDiffusionDitherStrategy;
import uk.co.silentsoftware.core.converters.image.orderedditherstrategy.OrderedDitherStrategy;
import uk.co.silentsoftware.core.helpers.ColourHelper;
import uk.co.silentsoftware.core.helpers.ImageHelper;

/**
 * A converter that wraps the GigaScreen logic around a base dithering converter.
 */
public class GigaScreenConverterImpl implements ImageProcessor {
	
	private DitherStrategy ditherStrategy;
	
	public GigaScreenConverterImpl(){}
	
	public GigaScreenConverterImpl(DitherStrategy ditherStrategy) {
		this.ditherStrategy = ditherStrategy;
	}
	
	@Override
	public ResultImage[] convert(BufferedImage original) {
		OptionsObject oo = OptionsObject.getInstance();
		BufferedImage output = ImageHelper.copyImage(original);
		final BufferedImage output1 = new BufferedImage(output.getWidth(), output.getHeight(), BufferedImage.TYPE_INT_ARGB);
		final BufferedImage output2 = new BufferedImage(output.getWidth(), output.getHeight(), BufferedImage.TYPE_INT_ARGB);
		
		// Dithers the images to the GigaScreen palette (if/else to switch between preview window images and actual output)
		ResultImage[] resultImage = null;
		if (ditherStrategy != null) {
			if (ditherStrategy instanceof ErrorDiffusionDitherStrategy) {
				resultImage = new ErrorDiffusionConverterImpl((ErrorDiffusionDitherStrategy)ditherStrategy, true, null).convert(output);
			} else {
				resultImage = new OrderedDitherConverterImpl((OrderedDitherStrategy)ditherStrategy).convert(output); 
			}
		} else {
			ditherStrategy = oo.getSelectedDitherStrategy();
			if (ditherStrategy instanceof ErrorDiffusionDitherStrategy) {
				resultImage = new ErrorDiffusionConverterImpl().convert(output);
			} else {
				resultImage = new OrderedDitherConverterImpl().convert(output);
			}
		} 
		ditherStrategy = null;
		output = resultImage[0].getImage();
		
		// Algorithm replaces each pixel with the colour from the closest matching
		// 4 colour GigaScreen attribute block.
		GigaScreenAtrribute[][] quad = getGigaScreenAttribute(output);
		GigaScreenAtrribute combo = null;
		for (int y = 0; y < output.getHeight(); ++y) {		
			for (int x = 0; x < output.getWidth(); ++x) {
				if (x%ATTRIBUTE_BLOCK_SIZE == 0) {
					combo = quad[x/ATTRIBUTE_BLOCK_SIZE][y/ATTRIBUTE_BLOCK_SIZE];
				}
				GigaScreenAtrribute.GigaScreenColourCombo c = ColourHelper.getClosestGigaScreenCombo(output.getRGB(x, y), combo);
				output.setRGB(x, y, c.getGigascreenColour());
				output1.setRGB(x, y, c.getScreen1Colour());
				output2.setRGB(x, y, c.getScreen2Colour());		
			}
		}
		
		// We can only order attributes if each both screens are either bright or half bright, not mixed
		if (!(oo.getGigaScreenAttributeStrategy() instanceof GigaScreenAttributeStrategy) && (oo.getExportTape() || oo.getExportScreen())) {
			orderByGigaScreenHsbOption(output1, output2);
		}
	
		return new ResultImage[]{new ResultImage(ResultImageType.FINAL_IMAGE, output), 
				new ResultImage(ResultImageType.SUPPORTING_IMAGE, output1),
				new ResultImage(ResultImageType.SUPPORTING_IMAGE, output2)};
	}
	
	/**
	 * Creates a map of the 32x24 Spectrum attribute set for two screens.
	 * For each attribute block it finds the closest (best fitting) gigascreen
	 * palette of 4 colours.
	 */
	public GigaScreenAtrribute[][] getGigaScreenAttribute(BufferedImage original) {
		OptionsObject oo = OptionsObject.getInstance();
		GigaScreenAtrribute[] palette = oo.getGigaScreenAttributeStrategy().getPalette();
		GigaScreenAtrribute[][] entries = new GigaScreenAtrribute[original.getWidth()/ATTRIBUTE_BLOCK_SIZE][original.getHeight()/ATTRIBUTE_BLOCK_SIZE];
		for (int y = 0; y + ATTRIBUTE_BLOCK_SIZE <= original.getHeight(); y += ATTRIBUTE_BLOCK_SIZE) {			
			for (int x = 0; x + ATTRIBUTE_BLOCK_SIZE <= original.getWidth() && y + ATTRIBUTE_BLOCK_SIZE <= original.getHeight(); x += ATTRIBUTE_BLOCK_SIZE) {
				int outRgb[] = original.getRGB(x, y, ATTRIBUTE_BLOCK_SIZE, ATTRIBUTE_BLOCK_SIZE, null, 0, ATTRIBUTE_BLOCK_SIZE);				
				long lowest = Long.MAX_VALUE;
				GigaScreenAtrribute chosen = palette[0];
				for (GigaScreenAtrribute combo : palette) {
					int score = combo.getScoreForAttributeBlock(outRgb);
					if (score < lowest) {
						lowest = score;
						chosen = combo;
					}
				}
				entries[x/ATTRIBUTE_BLOCK_SIZE][y/ATTRIBUTE_BLOCK_SIZE] = chosen;
			}			
		}
		return entries;
	}
	
	/**
	 * Reorders the colour between the two screens to minimise the amount of flicker or
	 * other artifacts in actual ZX Spectrum screen output. 
	 */
	private void orderByGigaScreenHsbOption(BufferedImage output1, BufferedImage output2) {
		GigaScreenHSBOption hsbOption = OptionsObject.getInstance().getGigaScreenHsbOption();
		if (GigaScreenHSBOption.None == hsbOption) {
			return;
		} 
		for (int y = 0; y + ATTRIBUTE_BLOCK_SIZE <= output1.getHeight(); y += ATTRIBUTE_BLOCK_SIZE) {			
			for (int x = 0; x + ATTRIBUTE_BLOCK_SIZE <= output1.getWidth() && y + ATTRIBUTE_BLOCK_SIZE <= output1.getHeight(); x += ATTRIBUTE_BLOCK_SIZE) {
				int outRgb1[] = output1.getRGB(x, y, ATTRIBUTE_BLOCK_SIZE, ATTRIBUTE_BLOCK_SIZE, null, 0, ATTRIBUTE_BLOCK_SIZE);				
				int outRgb2[] = output2.getRGB(x, y, ATTRIBUTE_BLOCK_SIZE, ATTRIBUTE_BLOCK_SIZE, null, 0, ATTRIBUTE_BLOCK_SIZE);				
				float totalCount1 = 0;
				float totalCount2 = 0;
				Set<Integer> cols = new HashSet<>();
				for (int rgb : outRgb1) {
					cols.add(rgb);
					if (cols.size() == 4) {
						break;
					}
				}
				for (int rgb : cols) {
					int[] rgbComps = ColourHelper.intToRgbComponents(rgb);
					float[] hsb = Color.RGBtoHSB(rgbComps[0], rgbComps[1], rgbComps[2], null);
					totalCount1+=getGigaScreenHSBCount(hsb, hsbOption);
				}	
				cols = new HashSet<>();
				for (int rgb : outRgb2) {
					cols.add(rgb);
					if (cols.size() == 4) {
						break;
					}
				}
				for (int rgb : cols) {
					int[] rgbComps = ColourHelper.intToRgbComponents(rgb);
					float[] hsb = Color.RGBtoHSB(rgbComps[0], rgbComps[1], rgbComps[2], null);
					totalCount2+=getGigaScreenHSBCount(hsb, hsbOption);
				}
				if (totalCount1 < totalCount2) {
					output1.setRGB(x, y, ATTRIBUTE_BLOCK_SIZE, ATTRIBUTE_BLOCK_SIZE, outRgb2, 0, ATTRIBUTE_BLOCK_SIZE);	
					output2.setRGB(x, y, ATTRIBUTE_BLOCK_SIZE, ATTRIBUTE_BLOCK_SIZE, outRgb1, 0, ATTRIBUTE_BLOCK_SIZE);	
				}
			}			
		}		
	}
	
	/**
	 * Calculates a value for a given hue/saturation/brightness on a given pixel
	 */
	private float getGigaScreenHSBCount(float[] hsb, GigaScreenHSBOption hsbOption) {
		switch(hsbOption) {		
			case Hue:
				return hsb[0];
			case Saturation:
				return hsb[1];
			case Brightness:
				return hsb[2];
			case HueBrightness:
				return hsb[0]+hsb[2];
			case HueSaturation:
				return hsb[0]+hsb[1];
			case SaturationBrightness:
				return hsb[1]+hsb[2];
			default:
				return hsb[2];
		}
	}
}
