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
package uk.co.silentsoftware.core.converters.image;

import java.awt.image.BufferedImage;

/**
 * Processed output buffered image result with the type of image, 
 * such as supporting (e.g. gigascreen image 1 of 2) or final image
 * (e.g. combined gigascreen image).
 */
public class ResultImage {

	private ResultImageType resultImageType;
	private BufferedImage image;
		
	public ResultImage(ResultImageType resultImageType, BufferedImage image) {
		this.resultImageType = resultImageType;
		this.image = image;
	}
	public ResultImageType getResultImageType() {
		return resultImageType;
	}
	public void setResultImageType(ResultImageType resultImageType) {
		this.resultImageType = resultImageType;
	}
	public BufferedImage getImage() {
		return image;
	}
	public void setImage(BufferedImage image) {
		this.image = image;
	}
	
	public enum ResultImageType {
		FINAL_IMAGE, SUPPORTING_IMAGE
	}
	
	public static ResultImage getFinalImage(ResultImage[] images) {
		for (ResultImage ri : images) {
    		if (ResultImageType.FINAL_IMAGE == ri.getResultImageType()) {
    			return ri;
    		}
    	}
		return null;
	}
}
