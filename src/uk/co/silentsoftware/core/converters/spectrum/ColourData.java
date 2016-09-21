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


/**
 * Small bean to associate RGB values
 * used for Spectrum ink and paper
 */
public class ColourData {
	
	/**
	 * The colour for ink
	 */
	private Integer inkRGB = null;
	
	/**
	 * The colour for paper
	 */
	private Integer paperRGB = null;
	
	/**
	 * Whether this colour belongs to the Spectrum bright colour set
	 */
	private boolean isBrightSet = true;
	
	public void setBrightSet(boolean isBrightSet) {
		this.isBrightSet = isBrightSet;
	}
	
	public boolean isBrightSet() {
		return isBrightSet;
	}
	
	public Integer getInkRGB() {
		return inkRGB;
	}
	public void setInkRGB(Integer inkRGB) {
		this.inkRGB = inkRGB;
	}
	public Integer getPaperRGB() {
		return paperRGB;
	}
	public void setPaperRGB(Integer paperRGB) {
		this.paperRGB = paperRGB;
	}

	@Override
	public String toString() {
		return "Ink:"+Integer.toHexString(inkRGB)+" Paper:"+Integer.toHexString(paperRGB)+" Bright:"+isBrightSet;
	}
	
	
}
