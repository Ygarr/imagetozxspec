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
package uk.co.silentsoftware.config;

/**
 * Enum storing screen attribute ordering modes, e.g "Brightness"
 * would group whichever screen's attributes were darker on
 * screen 1. 
 */
public enum GigaScreenHSBOption {
	None("None"), Hue("Hue"), Saturation("Saturation"), Brightness("Brightness"), HueSaturation("Hue and Saturation"), HueBrightness("Hue and Brightness"), SaturationBrightness("Saturation and Brightness");
	private String text;
	GigaScreenHSBOption(String text) {
		this.text = text;
	}
	@Override
	public String toString() {
		return text;
	}
}
