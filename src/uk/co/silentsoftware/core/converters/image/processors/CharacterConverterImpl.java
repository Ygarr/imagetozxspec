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

import static uk.co.silentsoftware.config.LanguageSupport.getCaption;
import static uk.co.silentsoftware.config.SpectrumDefaults.ATTRIBUTE_BLOCK_SIZE;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.beans.XMLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import uk.co.silentsoftware.core.colourstrategy.MonochromePaletteStrategy;
import uk.co.silentsoftware.core.converters.image.ResultImage;
import uk.co.silentsoftware.core.converters.image.ResultImage.ResultImageType;
import uk.co.silentsoftware.core.converters.image.errordiffusionstrategy.StuckiDitherStrategy;
import uk.co.silentsoftware.core.helpers.ColourHelper;

/**
 * Converts images to the ZX spectrum character set,
 * requires that the image be in monochrome and
 * dithered by another processor first (hardcoded Stucki).
 */
@SuppressWarnings("unchecked")
public class CharacterConverterImpl implements ImageProcessor {
	
	private boolean isPreview = false;
	
	/**
	 * The UTF-8 character to 8x8 pixel map, map.
	 */
	public static Map<String, int[]> charSet = null;
	static {
		XMLDecoder d = null;
		try {
			d = new XMLDecoder(new GZIPInputStream(Thread.currentThread().getContextClassLoader().getResourceAsStream("characters.xml.gz")));
			charSet = (Map<String, int[]>) d.readObject();
		} catch (Exception e) {
			// If this fails exceptions will be thrown by the thread which are caught
		} finally {
			if (d != null) {
				d.close();
			}
		}
	}
	
	public CharacterConverterImpl(boolean isPreview) {
		this.isPreview = isPreview;
	}

	@Override
	public ResultImage[] convert(BufferedImage original) {
		
		// Dither the image beforehand to improve monochrome conversion
		ImageProcessor preDither = new ErrorDiffusionConverterImpl(new StuckiDitherStrategy(), false, new MonochromePaletteStrategy());
		ResultImage[] resultImages = preDither.convert(original);
		BufferedImage output = null;
		for (ResultImage ri : resultImages) {
			if (ResultImageType.FINAL_IMAGE == ri.getResultImageType()) {
				output = ri.getImage();
				break;
			}
		}
		//BufferedImage output = ImageHelper.copyImage(preDitheredImage);
		
		for (int y = 0; y + ATTRIBUTE_BLOCK_SIZE <= output.getHeight(); y += ATTRIBUTE_BLOCK_SIZE) {
			for (int x = 0; x + ATTRIBUTE_BLOCK_SIZE <= output.getWidth() && y + ATTRIBUTE_BLOCK_SIZE <= output.getHeight(); x += ATTRIBUTE_BLOCK_SIZE) {
				int outRgb[] = output.getRGB(x, y, ATTRIBUTE_BLOCK_SIZE, ATTRIBUTE_BLOCK_SIZE, null, 0, ATTRIBUTE_BLOCK_SIZE);
				outRgb = findBestCharacterMatch(outRgb);
				output.setRGB(x, y, ATTRIBUTE_BLOCK_SIZE, ATTRIBUTE_BLOCK_SIZE, outRgb, 0, ATTRIBUTE_BLOCK_SIZE);
			}
		}
		// Print the name of the preview strategy
		if (isPreview) {
			Graphics g = output.getGraphics();
			g.setFont(g.getFont().deriveFont(Font.BOLD));
			int height = g.getFontMetrics().getHeight();
			int width = g.getFontMetrics().stringWidth(getCaption("character_converter"));
			g.setColor(Color.WHITE);
			g.fillRect(0, 20 - g.getFontMetrics().getAscent(), width, height);
			g.setColor(Color.BLACK);
			g.drawString(getCaption("character_converter"), 0, 20);
			g.dispose();
		}
		
		return new ResultImage[]{new ResultImage(ResultImageType.FINAL_IMAGE, output)};
	}

	/**
	 * Iterates through the character-pixel map to find a character
	 * that has the most similarly positioned number of pixels which
	 * it accumulates as a score. Note that the character-pixel map
	 * is black-white based and we need to map to the monochrome
	 * colours in use during counting.
	 * 
	 * @param sample
	 * @return
	 */
	public int[] findBestCharacterMatch(int[] sample) {
		Map<String, Integer> stats = new HashMap<String, Integer>();
		Set<String> keys = charSet.keySet();
		for (String key : keys) {
			int score = 0;
			int[] character = charSet.get(key);
			for (int i = 0; i < character.length; ++i) {
				int monochromeMappedColour = ColourHelper.getMonochromeFromBlackAndWhite(character[i]);
				if (monochromeMappedColour == sample[i]) {
					score++;
				}
			}
			stats.put(key, score);
		}
		int bestScore = Integer.MIN_VALUE;
		String chosenKey = null;
		for (String key : keys) {
			int score = stats.get(key);
			if (score > bestScore) {
				bestScore = score;
				chosenKey = key;
			}
		}
		return ColourHelper.getMonochromeFromBlackAndWhite(charSet.get(chosenKey));
	}
}
