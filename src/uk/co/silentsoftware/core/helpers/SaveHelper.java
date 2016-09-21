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
package uk.co.silentsoftware.core.helpers;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import uk.co.silentsoftware.config.OptionsObject;

public class SaveHelper {
	
	
	/**
	 * The default file format and pre-suffix ".zx."
	 */
	public static final String FILE_SUFFIX = ".zx.";
	
	/**
	 * Saves the image to a file
	 * 
	 * @param output
	 * @param destFolder
	 * @param fileName
	 */
	public static void saveImage(final BufferedImage output, File destFolder, String fileName) {
		File f = new File(destFolder.getAbsolutePath()+"/"+fileName.substring(0, fileName.lastIndexOf("."))+FILE_SUFFIX+OptionsObject.getInstance().getImageFormat());
		saveImage(output, f);	
	}
	
	/**
	 * Write an image directly to the specified file
	 * 
	 * @param output
	 * @param destFile
	 */
	public static void saveImage(final BufferedImage output, final File destFile) {
		OutputStream out = null;
		try {
			if (destFile.exists())
				destFile.delete();
			out = new BufferedOutputStream(new FileOutputStream(destFile));
			ImageIO.write(output,"gif", out);
			out.close();
		} catch(IOException io) {
			if (out != null) {
				try{out.close();}catch(Throwable t){}
			}
		}
	}
	
	/**
	 * Write an image directly to the specified stream without closing the output stream
	 * 
	 * @param output
	 * @param os
	 */
	public static void saveImage(final BufferedImage output, final OutputStream os) {
		try {
			BufferedOutputStream out = new BufferedOutputStream(os);
			ImageIO.write(output, OptionsObject.getInstance().getImageFormat(), out);
			out.flush();
		} catch(IOException io) {}
	}
	
	/**
	 * Saves raw byte data to the given file
	 * 
	 * @param bytes
	 * @param file
	 */
	public static void saveBytes(byte[] bytes, File file) {
		BufferedOutputStream out = null;
		try {
			if (file.exists())
				file.delete();
			out = new BufferedOutputStream(new FileOutputStream(file));
			out.write(bytes);
			out.close();
		} catch(IOException io) {
			if (out != null) {
				try{out.close();}catch(Throwable t){}
			}
		}
	}

	
	public static void deleteFile( File file) {
		if (file.exists())
			file.delete();
	}
	
	/**
	 * Saves raw byte data to the given file
	 * 
	 * @param bytes
	 * @param file
	 */
	public static void saveBytes(byte[] bytes, File file, boolean append) {
		BufferedOutputStream out = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream(file, append));
			out.write(bytes);
			out.close();
		} catch(IOException io) {
			if (out != null) {
				try{out.close();}catch(Throwable t){}
			}
		}
	}
	
	/**
	 * Saves raw byte data to the chosen output folder, but
	 * uses the given file filename + the given suffix
	 * 
	 * @param b
	 * @param destFolder
	 * @param fileName
	 * @param suffix
	 */
	public static void saveBytes(byte[] b, File destFolder, String fileName, String suffix) {
		OutputStream out = null;
		try {
			File f = new File(destFolder.getAbsolutePath()+"/"+fileName.substring(0, fileName.lastIndexOf("."))+suffix);
			if (f.exists())
				f.delete();
			out = new FileOutputStream(f);
			out.write(b);
			out.close();
		} catch(IOException io) {
			if (out != null) {
				try{out.close();}catch(Throwable t){}
			}
		}
	}
}
