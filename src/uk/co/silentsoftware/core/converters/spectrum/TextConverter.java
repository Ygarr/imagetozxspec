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
package uk.co.silentsoftware.core.converters.spectrum;

import static uk.co.silentsoftware.config.SpectrumDefaults.ATTRIBUTE_BLOCK_SIZE;

import java.awt.image.BufferedImage;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import uk.co.silentsoftware.core.converters.image.processors.CharacterConverterImpl;
import uk.co.silentsoftware.core.helpers.ColourHelper;


/**
 * Saves image to text using a predefined map of UTF-8 characters to
 * spectrum font pixel positions.
 */
public class TextConverter {

	/**
	 * The UTF-8 character to 8x8 pixel map, map.
	 */
	private static Map<String, int[]> charSet = CharacterConverterImpl.charSet;
	
	/**
	 * Iterates over the image in 8x8 blocks and finds the matching character
	 * to replace in a text file
	 * 
	 * @param original
	 * @param imagePro
	 * @return
	 */
	public String convert(final BufferedImage original) {
		String text = new String("".getBytes(), Charset.forName("UTF-8"));
		for (int y = 0; y + ATTRIBUTE_BLOCK_SIZE <= original.getHeight(); y += ATTRIBUTE_BLOCK_SIZE) {
			for (int x = 0; x + ATTRIBUTE_BLOCK_SIZE <= original.getWidth() && y + ATTRIBUTE_BLOCK_SIZE <= original.getHeight(); x += ATTRIBUTE_BLOCK_SIZE) {
				int outRgb[] = original.getRGB(x, y, ATTRIBUTE_BLOCK_SIZE, ATTRIBUTE_BLOCK_SIZE, null, 0, ATTRIBUTE_BLOCK_SIZE);
				text += findBestCharacterMatch(outRgb);				
			}
			text+="\n";
		}	
		return text;
	}	
	
	/**
	 * Finds the character string best matching the pixels in the 8x8
	 * pixel sample map
	 * 
	 * @param sample
	 * @return
	 */
	private String findBestCharacterMatch(int[] sample) {
		sample = ColourHelper.getBlackAndWhiteFromMonochrome(sample);
		Map<String, Integer> stats = new HashMap<String, Integer>();
		Set<String> keys = charSet.keySet();
		for (String key : keys) {
			int score = 0;
			int[] character = charSet.get(key);
			for (int i = 0; i < character.length; ++i) {
				if (character[i] == sample[i]) {
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
		return chosenKey;
	}
}
