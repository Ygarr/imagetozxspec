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
package uk.co.silentsoftware.dispatcher;

import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.JOptionPane;

import uk.co.silentsoftware.config.OptionsObject;
import uk.co.silentsoftware.config.ScalingObject;
import uk.co.silentsoftware.core.colourstrategy.GigaScreenPaletteStrategy;
import uk.co.silentsoftware.core.converters.image.DitherStrategy;
import uk.co.silentsoftware.core.converters.image.ResultImage;
import uk.co.silentsoftware.core.converters.image.errordiffusionstrategy.ErrorDiffusionDitherStrategy;
import uk.co.silentsoftware.core.converters.image.orderedditherstrategy.OrderedDitherStrategy;
import uk.co.silentsoftware.core.converters.image.processors.CharacterConverterImpl;
import uk.co.silentsoftware.core.converters.image.processors.ErrorDiffusionConverterImpl;
import uk.co.silentsoftware.core.converters.image.processors.GigaScreenConverterImpl;
import uk.co.silentsoftware.core.converters.image.processors.ImageProcessor;
import uk.co.silentsoftware.core.converters.image.processors.OrderedDitherConverterImpl;
import uk.co.silentsoftware.core.converters.spectrum.ScrConverter;
import uk.co.silentsoftware.core.helpers.ColourHelper;
import uk.co.silentsoftware.core.helpers.ImageHelper;

/**
 * Wrapper class for a work processing unit that
 * contains conversion actioning methods (i.e. the bits
 * that actually call the bits that do work :) ).
 * This class is not entirely static due to the need for
 * separate instances when used by the many dithers
 * preview requiring a different image processor each time.
 */
public class WorkProcessor
{	
	/**
	 * The image processor to use
	 */
	private ImageProcessor imageProcessor;

	/**
	 * The "SCREEN$" converter
	 */
	private static final ScrConverter screenConverter = new ScrConverter();

	
	/**
	 * Main work processor constructor used for actual results
	 */
	public WorkProcessor() {
		OptionsObject oo = OptionsObject.getInstance();
		if (oo.getColourMode() instanceof GigaScreenPaletteStrategy){
			imageProcessor = new GigaScreenConverterImpl();
		} else if (oo.getSelectedDitherStrategy() instanceof ErrorDiffusionDitherStrategy) {
			imageProcessor = new ErrorDiffusionConverterImpl();
		} else if (oo.getSelectedDitherStrategy() instanceof OrderedDitherStrategy){  
			imageProcessor = new OrderedDitherConverterImpl();
		} else {
			imageProcessor = new CharacterConverterImpl(false);
		}
	}
	
	/**
	 * Preview constructor for when we need a result from a given
	 * dither strategy as opposed to that selected in options
	 * 
	 * @param dither
	 */
	public WorkProcessor(DitherStrategy dither) {
		OptionsObject oo = OptionsObject.getInstance();
		if (oo.getColourMode() instanceof GigaScreenPaletteStrategy && dither != null){
			imageProcessor = new GigaScreenConverterImpl(dither);
		} else if (dither instanceof ErrorDiffusionDitherStrategy) {
			imageProcessor = new ErrorDiffusionConverterImpl((ErrorDiffusionDitherStrategy)dither, true, null);
		} else if (dither instanceof OrderedDitherStrategy){ 
			imageProcessor = new OrderedDitherConverterImpl((OrderedDitherStrategy)dither);
		} else {
			imageProcessor = new CharacterConverterImpl(true);
		}
	}
	
	/**
	 * Converts the given image to the SCR (SCREEN) format,
	 * optionally saves the file to disk and wraps any errors
	 * during conversion and shows them as a UI dialog message.
	 * 
	 * @param original
	 * @param isAlreadyImageProcessed
	 * @return
	 */
	byte[] convertScreen(BufferedImage original) {
		try {
			return screenConverter.convert(original, imageProcessor);
		} catch(Exception e) {
			JOptionPane.showMessageDialog(null, "An error has occurred: "+e.getMessage(), "Guru meditation", JOptionPane.OK_OPTION);  
		}
		return null;
	}
	
	/**
	 * Converts the given image to the to a "Spectrumified" format
	 * which is returned as a BufferedImage. Any errors during 
	 * conversion are shown as a UI dialog message.
	 * 
	 * @param original
	 * @return
	 */
	public ResultImage[] convertImage(final BufferedImage original) {
		try {
			return imageProcessor.convert(original);
		} catch(Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "An error has occurred: "+e.getMessage(), "Guru meditation", JOptionPane.OK_OPTION);  
		}
		return null;
	}
	
	/**
	 * Pre processes the given bufferedimage applying the given scaling
	 * and the specified option set contrast, saturation and brightness
	 * 
	 * @param original
	 * @param scaling
	 * @return
	 */
	public BufferedImage preProcessImage(final Image original, ScalingObject scaling) {
		OptionsObject oo = OptionsObject.getInstance();
		BufferedImage origScaled = ImageHelper.quickScaleImage(original, scaling.getWidth(), scaling.getHeight());
		origScaled = ColourHelper.changeContrast(origScaled, oo.getContrast());
		origScaled = ColourHelper.changeSaturation(origScaled, oo.getSaturation());
		origScaled = ColourHelper.changeBrightness(origScaled, oo.getBrightness());
		return origScaled;
	}
}
