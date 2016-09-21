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

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import uk.co.silentsoftware.core.helpers.ColourHelper;

/**
 * Representation of an 8x8 pixel attribute block across two screens.
 * This class stores 4 colours each representing one of the (derived) 
 * GigaScreen 102 colours as a "GigaScreenColourCombo" inner class. 
 * The GigaScreenColourCombos also hold the base 4 Spectrum colours
 * (2 per screen attribute) used to derive the 4 Gigascreen colours.
 */
public class GigaScreenAtrribute {

	private GigaScreenColourCombo[] combos = new GigaScreenColourCombo[4];
	private int uniqueColourCount;
	private String uniqueHash;
	
	public GigaScreenAtrribute(int inkScreen1, int paperScreen1, int inkScreen2, int paperScreen2) {
		combos[0] = new GigaScreenColourCombo(inkScreen1, inkScreen2);
		combos[1] = new GigaScreenColourCombo(inkScreen1, paperScreen2);
		combos[2] = new GigaScreenColourCombo(paperScreen1, inkScreen2);
		combos[3] = new GigaScreenColourCombo(paperScreen1, paperScreen2);
		
		Set<Integer> uniqueColours = new TreeSet<>();
		uniqueColours.add(combos[0].gigascreenColour);
		uniqueColours.add(combos[1].gigascreenColour);
		uniqueColours.add(combos[2].gigascreenColour);
		uniqueColours.add(combos[3].gigascreenColour);
		uniqueColourCount = uniqueColours.size();		
		
		Iterator<Integer> iter = uniqueColours.iterator();
		while (iter.hasNext()) {
			uniqueHash += iter.next();
		}
	}	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + uniqueHash.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GigaScreenAtrribute other = (GigaScreenAtrribute) obj;
		if (!uniqueHash.equals(other.uniqueHash))
			return false;
		return true;
	}

	public int[] getPalette() {
		return new int[]{combos[0].gigascreenColour, combos[1].gigascreenColour, combos[2].gigascreenColour, combos[3].gigascreenColour};
	}
	
	/**
	 * Calculate how close a match this attribute set of 4 colours
	 * compares to the provided attribute block. Lower is better.
	 */
	public int getScoreForAttributeBlock(int[] attributeBlock) {
		int totalDistance = 0;
		for (int pixel : attributeBlock) {
			int[] components = ColourHelper.intToRgbComponents(pixel);
			int distance = ColourHelper.getClosestColourDistance(components[0], components[1], components[2], new int[]{combos[0].gigascreenColour, combos[1].gigascreenColour, combos[2].gigascreenColour, combos[3].gigascreenColour});
			totalDistance+=distance;
		}
		return totalDistance;
	}

	public int getUniqueColourCount() {
		return uniqueColourCount;
	}

	public GigaScreenColourCombo getColourCombo(int closest) {
		return combos[closest];
	}	
	
	/**
	 * Representation of an attribute block across two screens
	 * i.e. a gigascreen colour and the two base Spectrum colours 
	 * (one colour per screen). 
	 */
	public class GigaScreenColourCombo {
		
		private int gigascreenColour;
		private int screen1Colour;
		private int screen2Colour;
		
		public GigaScreenColourCombo (int screen1Colour, int screen2Colour){
			this.screen1Colour = screen1Colour;
			this.screen2Colour = screen2Colour;

			int[] rgbS1 = ColourHelper.intToRgbComponents(screen1Colour);
			int[] rgbS2 = ColourHelper.intToRgbComponents(screen2Colour);
			gigascreenColour = ColourHelper.intToAlphaRgb((rgbS1[0]+rgbS2[0])/2, (rgbS1[1]+rgbS2[1])/2, (rgbS1[2]+rgbS2[2])/2);		
		}


		public int getGigascreenColour() {
			return gigascreenColour;
		}

		public int getScreen1Colour() {
			return screen1Colour;
		}

		public int getScreen2Colour() {
			return screen2Colour;
		}
	}
}
