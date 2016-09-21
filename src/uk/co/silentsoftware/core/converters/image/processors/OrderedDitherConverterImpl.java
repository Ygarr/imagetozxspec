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
import uk.co.silentsoftware.core.colourstrategy.GigaScreenPaletteStrategy;
import uk.co.silentsoftware.core.colourstrategy.MonochromePaletteStrategy;
import uk.co.silentsoftware.core.converters.image.ResultImage;
import uk.co.silentsoftware.core.converters.image.ResultImage.ResultImageType;
import uk.co.silentsoftware.core.converters.image.orderedditherstrategy.OrderedDitherStrategy;
import uk.co.silentsoftware.core.helpers.ColourHelper;
import uk.co.silentsoftware.core.helpers.ImageHelper;

/**
* An ordered dithering converter
 */
public class OrderedDitherConverterImpl implements ImageProcessor {
	
	private OrderedDitherStrategy previewModeStrategy = null;
	
	public OrderedDitherConverterImpl(OrderedDitherStrategy previewModeStrategy) {
		this.previewModeStrategy = previewModeStrategy;
	}
	
	public OrderedDitherConverterImpl() {}
	
	/*
	 * {@inheritDoc}
	 */
	@Override
	public ResultImage[] convert(final BufferedImage original) {
		OptionsObject oo = OptionsObject.getInstance();
		BufferedImage output = ImageHelper.copyImage(original);
		final OrderedDitherStrategy ods = previewModeStrategy !=null?previewModeStrategy:oo.getOrderedDitherStrategy();
		int xMax = ods.getMatrixWidth();
		int yMax = ods.getMatrixHeight();
		for (int y=0; y+yMax<=original.getHeight(); y+=yMax) {
			for (int x=0; x+xMax<=original.getWidth() && y+yMax<=original.getHeight(); x+=xMax) {
				int outRgb[] = original.getRGB(x, y, xMax, yMax, null, 0, xMax);
				outRgb = ods.applyDither(outRgb);
				output.setRGB(x, y, xMax, yMax, outRgb, 0, xMax);	
			}
		}
		
		// Attribute blocks not needed since already 2 colour across entire image (mono) or will be further processed later (giga)
		if (!(oo.getColourMode() instanceof MonochromePaletteStrategy) && !(oo.getColourMode() instanceof GigaScreenPaletteStrategy)) {
			// Just colour all pixels but use the original image
			// as a basis for the colour selection
			output = ColourHelper.colourAttributes(output, original, oo.getColourMode());
		}
		
		// Print the name of the preview strategy
		if (previewModeStrategy != null) {
			Graphics g = output.getGraphics();
			g.setFont(g.getFont().deriveFont(Font.BOLD));
			int height = g.getFontMetrics().getHeight();
			int width = g.getFontMetrics().stringWidth(previewModeStrategy.toString());
			g.setColor(Color.WHITE);
			g.fillRect(0, 20-g.getFontMetrics().getAscent(), width, height);
			g.setColor(Color.BLACK);
			g.drawString(previewModeStrategy.toString(),0,20);
		}
		return new ResultImage[]{new ResultImage(ResultImageType.FINAL_IMAGE, output)};
	}
}
