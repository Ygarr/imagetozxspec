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

/**
 * Marker interface for the GigaScreen palette.
 */
public class GigaScreenPaletteStrategy implements ColourChoiceStrategy {

	public String toString() {
		return getCaption("colour_mode_gigascreen");
	}
	
	@Override
	public int colour(int originalAlphaRgb, int[] mostPopularAlphaRgbColours) {
		throw new UnsupportedOperationException("GigaScreen palette colouring cannot be applied to dither image processors - this is a placeholder class to allow identification during colour conversions for the GigaScreenConverter.");
	}
}
