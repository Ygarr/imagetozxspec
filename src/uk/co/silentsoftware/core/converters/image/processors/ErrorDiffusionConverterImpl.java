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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import uk.co.silentsoftware.config.OptionsObject;
import uk.co.silentsoftware.config.SpectrumDefaults;
import uk.co.silentsoftware.core.colourstrategy.ColourChoiceStrategy;
import uk.co.silentsoftware.core.colourstrategy.FullPaletteStrategy;
import uk.co.silentsoftware.core.colourstrategy.GigaScreenPaletteStrategy;
import uk.co.silentsoftware.core.colourstrategy.MonochromePaletteStrategy;
import uk.co.silentsoftware.core.converters.image.ResultImage;
import uk.co.silentsoftware.core.converters.image.ResultImage.ResultImageType;
import uk.co.silentsoftware.core.converters.image.errordiffusionstrategy.ErrorDiffusionDitherStrategy;
import uk.co.silentsoftware.core.helpers.ColourHelper;
import uk.co.silentsoftware.core.helpers.ImageHelper;

/**
 * An error diffusion dithering converter
 */
public class ErrorDiffusionConverterImpl implements ImageProcessor {

	private ErrorDiffusionDitherStrategy previewModeStrategy = null;

	private ColourChoiceStrategy colourChoiceStrategy;

	private boolean isPreview = false;

	public ErrorDiffusionConverterImpl(ErrorDiffusionDitherStrategy ditherStrategy, boolean isPreview, ColourChoiceStrategy colourMode) {
		this.previewModeStrategy = ditherStrategy;
		this.colourChoiceStrategy = colourMode;
		this.isPreview = isPreview;
	}

	public ErrorDiffusionConverterImpl() {
		this.colourChoiceStrategy = null;
	}

	/*
	 * {@inheritDoc}
	 */
	public ResultImage[] convert(BufferedImage original) {
		BufferedImage output = ImageHelper.copyImage(original);
		OptionsObject oo = OptionsObject.getInstance();
		final ErrorDiffusionDitherStrategy edds = previewModeStrategy != null ? previewModeStrategy : oo.getErrorDiffusionDitherStrategy();
		final ColourChoiceStrategy colourMode = colourChoiceStrategy != null ? colourChoiceStrategy : oo.getColourMode();
		Integer xBound = null;
		Integer yBound = null;
		for (int y = 0; y < output.getHeight(); ++y) {
			if (y%SpectrumDefaults.ATTRIBUTE_BLOCK_SIZE == 0) {
				yBound = y+SpectrumDefaults.ATTRIBUTE_BLOCK_SIZE;
			}
			
			if (oo.getSerpentine() && y % 2 == 0) {		
				for (int x = output.getWidth() - 1; x >= 0; --x) {
					if (x%SpectrumDefaults.ATTRIBUTE_BLOCK_SIZE == 0) {
						xBound = x-SpectrumDefaults.ATTRIBUTE_BLOCK_SIZE;
					}
					processPixel(output, colourMode, edds, x, y, xBound, yBound);
				}
			} else {
				for (int x = 0; x < output.getWidth(); ++x) {
					if (x%SpectrumDefaults.ATTRIBUTE_BLOCK_SIZE == 0) {
						xBound = x+SpectrumDefaults.ATTRIBUTE_BLOCK_SIZE;
					}
					processPixel(output, colourMode, edds, x, y, xBound, yBound);
				}
			}
		}
		// Attribute blocks not needed since already 2 colour across entire image (monochrome) when palette processed above,
		// or will be further processed later (gigascreen)
		if (!(colourMode instanceof MonochromePaletteStrategy) && !(colourMode instanceof GigaScreenPaletteStrategy)) {
			// Just colour all pixels but use the error diffused image
			// as a basis for the colour selection
			output = ColourHelper.colourAttributes(output, original, colourMode);
		}
		// Print the name of the preview strategy
		if (isPreview && previewModeStrategy != null) {
			Graphics g = output.getGraphics();
			g.setFont(g.getFont().deriveFont(Font.BOLD));
			int height = g.getFontMetrics().getHeight();
			int width = g.getFontMetrics().stringWidth(previewModeStrategy.toString());
			g.setColor(Color.WHITE);
			g.fillRect(0, 20 - g.getFontMetrics().getAscent(), width, height);
			g.setColor(Color.BLACK);
			g.drawString(previewModeStrategy.toString(), 0, 20);
			g.dispose();
		}
		return new ResultImage[]{new ResultImage(ResultImageType.FINAL_IMAGE, output)};
	}
	
	public void processPixel(BufferedImage output,  ColourChoiceStrategy colourMode, ErrorDiffusionDitherStrategy edds, int x, int y, Integer boundX, Integer boundY) {
		int oldPixel = output.getRGB(x, y);
		int newPixel;
		if (colourMode instanceof GigaScreenPaletteStrategy) {
			newPixel = ColourHelper.getClosestGigascreenColour(oldPixel);
		} else if (colourMode instanceof FullPaletteStrategy){
			newPixel = ColourHelper.getClosestSpectrumColour(oldPixel);
		// Monochrome
		} else {
			newPixel = ColourHelper.getMonochromeColour(oldPixel, Color.BLACK.getRGB(), Color.WHITE.getRGB());
		}
		output.setRGB(x, y, newPixel);
		edds.distributeError(output, oldPixel, newPixel, x, y, boundX, boundY);
	}
}
