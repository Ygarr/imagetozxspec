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
package uk.co.silentsoftware.core.converters.video;

import static uk.co.silentsoftware.config.LanguageSupport.getCaption;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.media.Buffer;
import javax.media.Codec;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.Manager;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.media.PlugInManager;
import javax.media.PrefetchCompleteEvent;
import javax.media.Time;
import javax.media.control.FrameGrabbingControl;
import javax.media.format.VideoFormat;
import javax.media.util.BufferToImage;

import uk.co.silentsoftware.config.OptionsObject;
import uk.co.silentsoftware.ui.ImageToZxSpec;

/**
 * Simple JMF wrapper to import video at a given frame sampling rate. Note JMF
 * does not always throw exceptions when it cannot decode a file.
 */
public class JMFVideoImportEngine implements VideoImportEngine {

	/**
	 * Listener for pre-processing the incoming video to separate images
	 */
	private static final VideoControllerListener vcl = new JMFVideoImportEngine.VideoControllerListener();

	/**
	 * Video seeking timeout before giving up
	 */
	private static final int SEEK_TIMEOUT = 10;
	
	/**
	 * Amount of time as a minimum before we can take a single image from the
	 * video in seconds
	 */
	private static final int MINIMUM_INTRO_WAIT = 2;

	/**
	 * Amount of (up to) random time we add to MINIMUM_INTRO_WAIT before we can
	 * take a single image from the video in seconds.
	 */
	private static final int RANDOM_INTRO_WAIT = 7;

	private static PrintStream oldErrPs = System.err;
	private static PrintStream oldOutPs = System.out;
	private static NullPrintStream ps = NullPrintStream.createInstance();
	private static final ExecutorService exec = Executors.newCachedThreadPool();

	
	/**
	 * Initialise jffmpeg cross platform video decoding for jmf
	 */
	static {
		try {
			String JFFMPEG_VIDEO = "net.sourceforge.jffmpeg.VideoDecoder";
			Codec video = (Codec) Class.forName(JFFMPEG_VIDEO).newInstance();
			PlugInManager.addPlugIn(JFFMPEG_VIDEO, video.getSupportedInputFormats(), video.getSupportedOutputFormats(null), PlugInManager.CODEC);
		} catch (Throwable t) {
		}
	}

	/**
	 * Converts the specified video file to a series of images and adds
	 * them to the given shared queue.
	 * 
	 * @param f
	 * @param singleImage
	 * @param sharedQueue
	 * @throws IOException
	 * @throws NoPlayerException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public void convertVideoToImages(File f, boolean singleImage, final Queue<Image> sharedQueue) throws IOException, NoPlayerException,
			InstantiationException, IllegalAccessException, ClassNotFoundException {

		try {
			System.setErr(ps);
			System.setOut(ps);
			final Player player = Manager.createPlayer(f.toURI().toURL());
			vcl.player = player;
			vcl.images = sharedQueue;
			vcl.singleImage = singleImage;
			player.addControllerListener(vcl);
			player.prefetch();
		} catch (Throwable t) {
			// Ignored
		}
	}

	/**
	 * Null printstream for ignoring API output errors that would ordinarily
	 * only go to the console.
	 */
	public static class NullPrintStream extends PrintStream {

		public static NullPrintStream createInstance() {
			return new NullPrintStream(new NullOutputStream());
		}

		private NullPrintStream(NullOutputStream nos) {
			super(nos);
		}

		private static class NullOutputStream extends OutputStream {
			@Override
			public void write(int b) {
			}
		}
	}

	/**
	 * Video processing listener grabs frames at the specified optional
	 * interval, scales the images to spectrum defaults and adds the image to an
	 * internally referenced list which is used by the calling code.
	 */
	private static class VideoControllerListener implements ControllerListener {

		Player player;
		Queue<Image> images;

		VideoControllerListener() {
		}

		boolean singleImage;

		@Override
		public void controllerUpdate(ControllerEvent ce) {
			if (ce instanceof PrefetchCompleteEvent) {
				try {
					final FrameGrabbingControl frame = (FrameGrabbingControl) player.getControl("javax.media.control.FrameGrabbingControl");
					final double totalSeconds = player.getDuration().getSeconds();
					final double mediaTimeIncrement = 1d / OptionsObject.getInstance().getVideoFramesPerSecond();
					Buffer frameBuf = frame.grabFrame();
					BufferToImage bti = new BufferToImage((VideoFormat) frameBuf.getFormat());
					Image bufImage;

					int singleImageSelectionTime = -1;
					if (singleImage) {
						singleImageSelectionTime = new Random().nextInt(RANDOM_INTRO_WAIT) + MINIMUM_INTRO_WAIT;
						if (singleImageSelectionTime > totalSeconds) {
							singleImageSelectionTime = (int) totalSeconds-1;
						}
					}
					player.setMediaTime(new Time(0));
					Future<?> seeker = nextSeek(player, mediaTimeIncrement);				
					
					while (player.getMediaTime().getSeconds() < totalSeconds) {
						try {
							try {
								seeker.get(SEEK_TIMEOUT, TimeUnit.SECONDS);
							} catch (TimeoutException te) {
								// Problem with stream just give up
								return;
							}
							frameBuf = frame.grabFrame();
							
							// Video seeking can be slow and holds up the current thread thus we can add
							// the last frame to the queue whilst attempting to seek to the next.
							seeker = nextSeek(player, mediaTimeIncrement);
							bufImage = bti.createImage(frameBuf);
	
							// Always add images if we don't want a single preview
							// image or when the time exceeds the point we want a
							// single image from
							if (singleImageSelectionTime == -1 || player.getMediaTime().getSeconds() >= singleImageSelectionTime) {
								images.add(bufImage);
	
								// We only want one image if single image selection
								// is turned on
								if (singleImageSelectionTime != -1 && player.getMediaTime().getSeconds() >= singleImageSelectionTime) {
									break;
								}
							}
							if (ImageToZxSpec.isCancelled()) {
								break;
							}
						} catch (Throwable t) {	
							t.printStackTrace();
							// I really don't want to know, I'll just deal with
							// any problems further up with null images.
						}
					}
				} finally {
					if (oldErrPs != null) {
						System.setErr(oldErrPs);
					}
					if (oldOutPs != null) {
						System.setOut(oldOutPs);
					}
					if (ps != null) {
						try {
							ps.close();
						} catch (Throwable t) {
						}
					}
					player.close();
				}
			}
		}
		
		/**
		 * Seeking in a non native decoder can be really slow so this 
		 * is in it's own thread so we can do different things
		 * 
		 * @param player
		 * @param mediaTimeIncrement
		 * @return
		 */
		private Future<?> nextSeek(final Player player, final double mediaTimeIncrement) {
			Future<?> future = exec.submit(new Runnable() {
				@Override
				public void run() {
					player.setMediaTime(new Time(player.getMediaTime().getSeconds() + mediaTimeIncrement));
				}
			});
			return future;
		}
	}
	
	@Override 
	public String toString() {
		return getCaption("JMF");
	}
}
