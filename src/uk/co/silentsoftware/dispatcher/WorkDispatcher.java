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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import uk.co.silentsoftware.config.OptionsObject;
import uk.co.silentsoftware.config.ScalingObject;
import uk.co.silentsoftware.core.converters.image.DitherStrategy;
import uk.co.silentsoftware.core.converters.image.ResultImage;
import uk.co.silentsoftware.core.helpers.ImageHelper;
import uk.co.silentsoftware.ui.ImageToZxSpec;

/**
 * Central class for the work engine that initialises
 * a fixed thread pool and submits work for preprocessing
 * and processing.
 */
public class WorkDispatcher
{
	/**
	 * The number of preview threads based on CPU count*4. Note that while they may
	 * not run on the individual cores it does give a good performance estimate.
	 */
	public static int THREAD_COUNT = Runtime.getRuntime().availableProcessors()*OptionsObject.getInstance().getThreadsPerCPU();

	/**
	 * The processing thread
	 */
	private static ExecutorService exec = Executors.newFixedThreadPool(THREAD_COUNT);
	
	/**
	 * Shut down this engine and threads
	 */
	public static void shutdownNow() {
		exec.shutdownNow();
	}
	
	/**
	 * Reconfigure the engine
	 * 
	 * WARNING: Calling this will stop any current processing!!
	 * 
	 * @param threads
	 */
	public static void setThreadsPerCPU(int threads) {
		exec.shutdownNow();
		try{exec.awaitTermination(10, TimeUnit.SECONDS);}
		catch (InterruptedException e){}
		THREAD_COUNT = Runtime.getRuntime().availableProcessors()*threads;
		exec = Executors.newFixedThreadPool(THREAD_COUNT);
		ImageToZxSpec.enableInput();
	}
	
	/**
	 * Submit an image to be pre processed and processed by a single thread
	 * 
	 * The resulting Future<WorkContainer> contains the resulting buffered image and
	 * optional SCR bytes.
	 * 
	 * @param original
	 * @param scrRequired
	 * @return
	 */
	public static Future<WorkContainer> submitWork(final Image original, final boolean scrRequired) {
		Future<WorkContainer> future = exec.submit(new Callable<WorkContainer>()
        {
            public WorkContainer call()
            {	
            	OptionsObject oo = OptionsObject.getInstance();
            	
				/* This will improve the WIP preview display */
            	if (oo.getShowWipPreview()) {
            		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
            	}
            	final WorkContainer result = new WorkContainer();
            	if (original == null) {
            		return result;
            	}
            	final WorkProcessor wp = new WorkProcessor();	
            	BufferedImage preProcessed = wp.preProcessImage(original, oo.getScaling());
            	ResultImage[] processed = wp.convertImage(preProcessed);
        		result.setPreprocessedImageResult(preProcessed);
            	result.setResultImage(processed);
            	
            	if (scrRequired) {
        			result.setScrData(wp.convertScreen(ImageHelper.copyImage(original)));
            	}
                return result;
            }
        });
		return future;
	}
	
	/**
	 * Similar to submit work (the same processing procedure is followed) however 
	 * no SCR output is provided, the default resolution of 256x192 is used and
	 * the dither can be specified.
	 * 
	 * @param original
	 * @param dither
	 * @return
	 */
	public static Future<WorkContainer> submitPreviewWork(final Image original, final DitherStrategy dither) {
		Future<WorkContainer> future = exec.submit(new Callable<WorkContainer>()
        {
            public WorkContainer call()
            {
            	OptionsObject oo = OptionsObject.getInstance();
            	ScalingObject so = oo.getZXDefaultScaling();
            	if (oo.getScaling() != oo.getScalings()[0]) {
            		so = oo.getScaling();
            	}
            	final WorkProcessor wp = new WorkProcessor(dither);
            	final WorkContainer result = new WorkContainer();
            	BufferedImage preProcessed = wp.preProcessImage(original, so);
            	ResultImage[] processed = wp.convertImage(preProcessed);
    			result.setResultImage(processed);
        		return result;
            }
        });
		return future;
	}
}
