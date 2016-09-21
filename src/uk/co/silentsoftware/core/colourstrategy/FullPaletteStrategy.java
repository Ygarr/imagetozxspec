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
package uk.co.silentsoftware.core.colourstrategy;

import static uk.co.silentsoftware.config.LanguageSupport.getCaption;
import uk.co.silentsoftware.core.helpers.ColourHelper;

/**
 * Colouring strategy to colour all pixels using
 * whichever of the two popular colours is closest
 * to the original.
 */
public class FullPaletteStrategy implements ColourChoiceStrategy {
	
	public String toString() {
		return getCaption("colour_mode_full");
	}
	
	@Override
	public int colour(int originalAlphaRgb, int[] mostPopularRgbColours) {
		// Break the colours into their RGB components
		int[] originalRgbComps = ColourHelper.intToRgbComponents(originalAlphaRgb);
		
		// Work out whether the original colour is closer to the most popular
		// or second most popular colour by looking at the difference of total
		// RGB components
		int diffs[] = new int[mostPopularRgbColours.length];
		for (int i=0; i<mostPopularRgbColours.length; ++i) {
			int[] mostPopRgbComps = ColourHelper.intToRgbComponents(mostPopularRgbColours[i]);
			diffs[i] = Math.abs(originalRgbComps[0] - mostPopRgbComps[0]) 
					+ Math.abs(originalRgbComps[1] - mostPopRgbComps[1]) 
					+ Math.abs(originalRgbComps[2] - mostPopRgbComps[2]);
		}

		int lowest = Integer.MAX_VALUE;
		int index = -1;
		for (int i=0; i<diffs.length; ++i) {
			if (diffs[i] < lowest) {
				lowest = diffs[i];
				index = i;
			}
		}	
		return mostPopularRgbColours[index];
	}
}
