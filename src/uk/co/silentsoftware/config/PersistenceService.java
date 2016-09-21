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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * Simple XStream wrapper for saving the preferences
 * via persisting (and restoring) the OptionObject
 */
public class PersistenceService {

	public final static String PREFS_FILE = System.getProperty("user.home")+"/imagetozxspec.prefs";
	
	public static void save(Object object, String filePath) {
		XStream xstream = new XStream(new StaxDriver());
		try {
			xstream.toXML(object, new FileOutputStream(filePath));
		} catch (FileNotFoundException e) {
			// We don't care about this, it may fail in webstart
		}
	}
	
	public static Object load(String filePath) {
		XStream xstream = new XStream(new StaxDriver());
		try {
			return xstream.fromXML(new FileInputStream(filePath));
		} catch (FileNotFoundException e) {
			// We don't care about this, it may fail in webstart
			// or if it's a new install.
			e.printStackTrace();
		}
		return null;
	}
}
