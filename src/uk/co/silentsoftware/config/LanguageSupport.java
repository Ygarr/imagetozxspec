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

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Basic text only internationalisation support.
 * Falls back to country if the specific dialect
 * cannot be loaded.
 */
public class LanguageSupport {

	/**
	 * The base file name for the internalisation
	 */
	private final static String MESSAGES_FILE = "Messages";
	
	/**
	 * The captions from the loaded file
	 */
	private static ResourceBundle captions;
	static {
		try {
			// Try full language/country code
			captions = ResourceBundle.getBundle(MESSAGES_FILE, Locale.getDefault(), new UTF8Control());
		} catch (MissingResourceException ignore) {}
		if (captions == null) {
			try {
				// Fallback to English if nothing else suitable
				captions = ResourceBundle.getBundle(MESSAGES_FILE, new Locale("en"), new UTF8Control());
			} catch (MissingResourceException ignore) {}
		}
	}
	
	/**
	 * Retrieves the translation under the specified key
	 * 
	 * @param key
	 * @return
	 */
	public static String getCaption(String key) {
		String translation = null;
		if (captions != null) {
			try {
				translation = captions.getString(key);
			} catch(MissingResourceException ignore){}
		}
		// We're missing a key's value, use the key as the value :(
		if (translation == null || translation.trim().length()==0) {
			translation=key;
		}
		return translation;
	}
}
