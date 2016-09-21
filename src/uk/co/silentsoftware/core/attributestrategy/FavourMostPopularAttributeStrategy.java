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
package uk.co.silentsoftware.core.attributestrategy;

import static uk.co.silentsoftware.config.LanguageSupport.getCaption;
import uk.co.silentsoftware.core.helpers.ColourHelper;

/**
 * If either colour is from a different set the most
 * popular colour determines which colour set (bright/half bright)
 * the second most popular colour should be in
 */
public class FavourMostPopularAttributeStrategy implements AttributeStrategy {

	/*
	 * {@inheritDoc}
	 */
	@Override
	public int[] enforceAttributeRule(int[] mostPopularColour,
			int[] secondMostPopularColour) {	
		int mostPopRgb = ColourHelper.intToAlphaRgb(mostPopularColour);
		int secMostPopRgb = ColourHelper.intToAlphaRgb(secondMostPopularColour);
		boolean popIsBright = ColourHelper.isBrightSet(mostPopRgb);
		boolean secIsBright = ColourHelper.isBrightSet(secMostPopRgb);
		
		// Attributes are different
		if (popIsBright != secIsBright) {
			// Not black (identical in both sets - no need to do anything)
			if (mostPopularColour != secondMostPopularColour) {
				// Popular colour is bright so force secondary to be in the bright set
				if (popIsBright) {
					secMostPopRgb = ColourHelper.getClosestBrightSpectrumColour(secMostPopRgb);
				// Primary is actually dark (secondary bright), so make the secondary dark
				} else {
					secMostPopRgb = ColourHelper.getClosestHalfBrightSpectrumColour(secMostPopRgb);
				}
			}
		}
		return new int[]{mostPopRgb, secMostPopRgb};
	}
	
	/*
	 * {@inheritDoc}
	 */
	@Override
	public boolean isBrightSet(int mostPopularColour,
			int secondMostPopularColour) {
		boolean popIsBright = ColourHelper.isBrightSet(mostPopularColour);
		boolean secIsBright = ColourHelper.isBrightSet(secondMostPopularColour);
		// Attributes are different
		if (popIsBright != secIsBright) {
			// Not black (identical in both sets - no need to do anything)
			if (mostPopularColour != secondMostPopularColour) {
				return popIsBright;
			}
		}
		return popIsBright && secIsBright;		
	}
	
	@Override 
	public String toString() {
		return getCaption("attr_fav_most_popular");
	}
}
