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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.media.NoPlayerException;
import javax.swing.JWindow;

import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.silentsoftware.ui.ImageToZxSpec;

/**
 * VLCJ wrapper for platform native video decoding
 */
public class VLCVideoImportEngine implements VideoImportEngine {

	/**
	 * Amount of time as a minimum before we can take a single image from the
	 * video in seconds
	 */
	private static final int MINIMUM_INTRO_WAIT = (int)TimeUnit.SECONDS.toMillis(2);

	/**
	 * Amount of (up to) random time we add to MINIMUM_INTRO_WAIT before we can
	 * take a single image from the video in seconds.
	 */
	private static final int RANDOM_INTRO_WAIT = (int)TimeUnit.SECONDS.toMillis(4);
	
	/**
	 * Converts the specified video file to a series of images and adds them to
	 * the given shared queue.
	 * 
	 * @param f
	 * @param singleImage
	 * @param sharedQueue
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws NoPlayerException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public void convertVideoToImages(File f, boolean singleImage, final Queue<Image> sharedQueue) throws IOException, InterruptedException {
		//final float mediaTimeIncrement = (float) (1000 / OptionsObject.getVideoFramesPerSecond());
		EmbeddedMediaPlayerComponent mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
		//ImageToZxSpec.p.add(mediaPlayerComponent);
		JWindow frame = new JWindow();
		frame.setBounds(0,0,1,1);
		frame.add(mediaPlayerComponent);
		frame.setVisible(true);
		final MediaPlayer player = mediaPlayerComponent.getMediaPlayer();
		player.addMediaPlayerEventListener(new MediaPlayerEventAdapter() {

			@Override
			public void snapshotTaken(MediaPlayer mp, String filePath) {

				try {
					File f = new File(filePath);
					f.deleteOnExit();
					BufferedImage img = ImageIO.read(f);
					sharedQueue.add(img);
				} catch (Throwable t) {}
			}

		});
		player.mute();
		player.startMedia(f.getAbsolutePath());
		long len = player.getLength();
		int singleImageSelectionTime = -1;
		if (singleImage) {
			singleImageSelectionTime = new Random().nextInt(RANDOM_INTRO_WAIT) + MINIMUM_INTRO_WAIT;
			if (singleImageSelectionTime > len) {
				singleImageSelectionTime = (int) len-1;
			}
		}
		frame.setVisible(false);
		while (player.getTime() < len) {
			// Always add images if we don't want a single preview
			// image or when the time exceeds the point we want a
			// single image from
			if (singleImageSelectionTime == -1 || player.getTime() >= singleImageSelectionTime) {
				File imgFile = File.createTempFile("img-", null);
				player.saveSnapshot(imgFile);
				

				// We only want one image if single image selection
				// is turned on
				if (singleImageSelectionTime != -1 && player.getTime() >= singleImageSelectionTime) {
					break;
				}
			}
			if (ImageToZxSpec.isCancelled()) {
				break;
			}
		}
		player.stop();
		player.release();
		mediaPlayerComponent.release();
	}
	
	@Override 
	public String toString() {
		return getCaption("VLC");
	}
}
