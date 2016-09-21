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

import uk.co.silentsoftware.core.converters.image.processors.GigaScreenAtrribute;

/**
 * Represents the screen-attribute distribution strategy by providing a 
 * pre-calculated per-attribute palette of 4 Gigascreen colours.
 * I.e. a GigaScreenAttribute palette entry of 4 colours is intended to
 * represent an attribute block distributed across two screens. 
 */
public interface GigaScreenAttributeStrategy {

	/**
	 * Retrieves an attribute palette based on GigaScreen palette strategy
	 * e.g both screen brights would be one strategy. 
	 */
	public GigaScreenAtrribute[] getPalette();
}
