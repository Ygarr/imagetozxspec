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

import uk.co.silentsoftware.core.helpers.ColourHelper;


/**
 * Interface to enforce an attribute choice strategy
 */
public interface AttributeStrategy {

	/**
	 * Enforce the rule by modifying the rgb component objects
	 * and changing the attribute colour set they are from *if
	 * necessary* (i.e. bright or half bright).
	 * 
	 * @param mostPopularColour
	 * @param secondMostPopularColour
	 */
	public int[] enforceAttributeRule(int[] mostPopularColour, int[] secondMostPopularColour);

	/**
	 * Similar to ColorHelper.isBrightSet but uses the strategy
	 * implementation to determine whether *both* colours should
	 * be in the bright or half bright set.
	 * @see ColourHelper#isBrightSet(int)
	 * 
	 * @param mostPopularColour
	 * @param secondMostPopularColour
	 * @return
	 */
	public boolean isBrightSet(int mostPopularColour, int secondMostPopularColour);
}
