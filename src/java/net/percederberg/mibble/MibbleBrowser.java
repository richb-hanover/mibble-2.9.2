/*
 * MibbleBrowser.java
 *
 * This work is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * This work is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 *
 * Copyright (c) 2004-2009 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.prefs.Preferences;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;

import javax.swing.UIManager;

import net.percederberg.mibble.browser.BrowserFrame;
import net.percederberg.mibble.browser.MibTreeBuilder;

/**
 * A program for browsing MIB files in a GUI.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.9
 * @since    2.3
 */
public class MibbleBrowser {

    /**
     * The command-line help output.
     */
    private static final String COMMAND_HELP =
        "A graphical SNMP MIB file browser. This program comes with\n" +
        "ABSOLUTELY NO WARRANTY; for details see the LICENSE.txt file.\n" +
        "\n" +
        "Syntax: MibbleBrowser [<file(s) or URL(s)>]";

    /**
     * The internal error message.
     */
    private static final String INTERNAL_ERROR =
        "INTERNAL ERROR: An internal error has been found. Please report\n" +
        "    this error to the maintainers (see the web site for\n" +
        "    instructions). Be sure to include the version number, as\n" +
        "    well as the text below:\n";

    /**
     * The application build information properties.
     */
    private Properties buildInfo;

    /**
     * The preferences for this application.
     */
    private Preferences prefs;

    /**
     * The MIB loader to use.
     */
    private MibLoader loader = new MibLoader();

    /**
     * The application main entry point.
     *
     * @param args           the command-line parameters
     */
    public static void main(String args[]) {
        new MibbleBrowser().start(args);
    }

    /**
     * Creates a new browser application.
     */
    public MibbleBrowser() {
        prefs = Preferences.userNodeForPackage(getClass());
    }

    /**
     * Starts this application.
     *
     * @param args           the command-line arguments
     */
    public void start(String[] args) {
        final BrowserFrame  frame;
        ArrayList     list;
        String        str;

        // Check command-line arguments
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-")) {
                printHelp("No option '" + args[i] + "' exist");
                System.exit(1);
            }
        }

        // Load application build information
        buildInfo = new Properties();
        try {
            buildInfo.load(getClass().getResourceAsStream("build.properties"));
        } catch (IOException ignore) {
            buildInfo.setProperty("build.title", "Mibble");
        }

        // Open browser frame
        try {
            str = "com.apple.mrj.application.apple.menu.about.name";
            System.setProperty(str, buildInfo.getProperty("build.title"));
            str = UIManager.getSystemLookAndFeelClassName();
            UIManager.setLookAndFeel(str);
        } catch (Exception e) {
            printInternalError(e);
        }
        frame = new BrowserFrame(this);
		// Add a Shutdown Hook to ensure that quit() is invoked no matter how the program exits.
        frame.setVisible(true);
		
        // Load command-line & preference MIBs
        frame.setBlocked(true);
        list = getFilePrefs();
		List cachedMIBs = getCachedMIBPrefs();
		list.addAll( cachedMIBs );
        for (int i = 0; i < args.length; i++) {
            list.add(args[i]);
        }
        for (int i = 0; i < list.size(); i++) {
            frame.loadMib(list.get(i).toString());
        }
        if (list.size() <= 0) {
			//load default MIBs if none were specified in the prefs or command-line arguments
            frame.loadMib("RFC1213-MIB");
            frame.loadMib("HOST-RESOURCES-MIB");
			//richb wants the following to also be loaded by default.  -TEX 12-17-09
			frame.loadMib("RFC1155-SMI");
			frame.loadMib("SNMPv2-SMI");
			frame.loadMib("SNMPv2-TC");
        }
        frame.refreshTree();
        frame.setBlocked(false);
    }

    /**
     * Prints command-line help information.
     *
     * @param error          an optional error message, or null
     */
    private void printHelp(String error) {
        System.err.println(COMMAND_HELP);
        System.err.println();
        if (error != null) {
            printError(error);
        }
    }

    /**
     * Prints an internal error message. This type of error should
     * only be reported when run-time exceptions occur, such as null
     * pointer and the likes. All these error should be reported as
     * bugs to the program maintainers.
     *
     * @param e              the exception to be reported
     */
    private void printInternalError(Exception e) {
        System.err.println(INTERNAL_ERROR);
        e.printStackTrace();
    }

    /**
     * Prints an error message.
     *
     * @param message        the error message
     */
    private void printError(String message) {
        System.err.print("Error: ");
        System.err.println(message);
    }

    /**
     * Returns the application build information.
     *
     * @return the application build information
     */
    public Properties getBuildInfo() {
        return buildInfo;
    }
	
    public Mib getMib(String src) {
        MibTreeBuilder  mb = MibTreeBuilder.getInstance();
        File            file = new File(src);
        Mib             mib = null;
		
        if (file.exists()) {
            mib = loader.getMib(file);
        }
		if ( mib == null ) {
            mib = loader.getMib(src);
        }
        return mib;
    }
	
	public URL getURL( Mib m )
	{
		return loader.getURL( m );
	}
	
    /**
     * Loads MIB file or URL.
     *
     * @param src            the MIB file or URL
     *
     * @throws IOException if the MIB file couldn't be found in the
     *             MIB search path
     * @throws MibLoaderException if the MIB file couldn't be loaded
     *             correctly
     */
    public void loadMib(String src) throws IOException, MibLoaderException {
        MibTreeBuilder  mb = MibTreeBuilder.getInstance();
        File            file = new File(src);
        Mib             mib = null;
		
		removeFilePref( file );		//If there is an error while loading the MIB from the file, then don't keep it in the prefs.
        if (file.exists()) {
            if (loader.getMib(file) != null) {
                return;
            }
            if (!loader.hasDir(file.getParentFile())) {
                loader.removeAllDirs();
                loader.addDir(file.getParentFile());
            }
            mib = loader.load(file);
            addFilePref(file);
        } else {
            mib = loader.load(src);
			if ( mib != null ) {
				addCachedMIBPref( src );
			}
        }
        mb.addMib(mib);
    }

    /**
     * Unloads a named MIB.
     *
     * @param name           the MIB name
     */
    public void unloadMib(String name) {
        Mib  mib = loader.getMib(name);

        if (mib != null) {
            removeFilePref(mib.getFile());
			removeCachedMIBPref( mib.getName() );
            try {
                loader.unload(name);
            } catch (MibLoaderException ignore) {
                // MIB loader unloading is best-attempt only
            }
            MibTreeBuilder.getInstance().unloadMib(name);
        }
    }

    /**
     * Unloads all loaded MIB files.
     *
     * @since 2.9
     */
    public void unloadAllMibs() {
        removeFilePrefs();
		removeCachedMIBPrefs();
        loader.unloadAll();
        MibTreeBuilder.getInstance().unloadAllMibs();
    }
	
    /**
     * Returns all MIBs found in the resources.
     *
     * @return a map containing the MIBs from the resources.  Each key of the Map is a 'resourceDir', and each value is a List of MIB files in that directory.
     */
	public Map getResourceMIBs()
	{
		ClassLoader classloader = getClass().getClassLoader();
		String[] dirs = loader.getResourceDirs();
		URL url;
		java.util.regex.Matcher m = java.util.regex.Pattern.compile( ".*file:(.*\\.jar)!/?(.+).*" ).matcher("");
		Map mibPaths = new HashMap(4);
		String mibDir;
		List mibFiles;
		Enumeration mibEnum;
		JarFile jarFile;
		JarEntry jarEntry;
		
		for ( int i = 0; i < dirs.length; i++ ) {
			url = classloader.getResource( dirs[i] );
			if ( url != null ) {
				m.reset(url.getFile());
				if ( m.matches() && (m.groupCount() > 1) ) {
					try {
						jarFile = new JarFile( m.group(1) );
						mibFiles = new LinkedList();
						mibDir = m.group(2);
						mibPaths.put( mibDir, mibFiles );
						mibEnum = jarFile.entries();
						while ( mibEnum.hasMoreElements() ) {
							jarEntry = (JarEntry) mibEnum.nextElement();
							if ( jarEntry.getName().startsWith( mibDir ) && !jarEntry.isDirectory() ) {
								mibFiles.add( jarEntry.getName() );
							}
						}
					} catch (IOException ioerr) {
						printInternalError(ioerr);
					}
				}
			}
		}
		
		//Each key in mibPaths is now a 'resourceDir' and its value is a list of MIBS the resourceDir contains.
		
		//sort the lists
		Iterator mibLists = mibPaths.values().iterator();
		while ( mibLists.hasNext() ) {
			Collections.sort( (List) mibLists.next() );
		}
		
		return mibPaths;
	}

    /**
     * Adds a specified cached MIB preference.
     *
     * @param name           the MIB to add
     */
    private void addCachedMIBPref(String name) {
        ArrayList  list = getCachedMIBPrefs();
		
        if (!list.contains(name)) {
            prefs.put("MIB" + list.size(), name);
        }
    }
	
    /**
     * Removes a specified cached MIB preference.
     *
     * @param name           the MIB to remove
     */
    private void removeCachedMIBPref(String name) {
        ArrayList  list = getCachedMIBPrefs();
		
        removeCachedMIBPrefs();
        list.remove(name);
        for (int i = 0; i < list.size(); i++) {
            prefs.put("MIB" + i, list.get(i).toString());
        }
    }
	
    /**
     * Returns the application cached MIB preferences.
     *
     * @return the list of MIBs to load
     */
    private ArrayList getCachedMIBPrefs() {
        ArrayList  list = new ArrayList();
		
        for (int i = 0; i < 1000; i++) {
            String str = prefs.get("MIB" + i, null);
            if (str != null) {
                list.add(str);
            }
        }
        return list;
    }
	
    /**
     * Removes all application cached-MIB preferences.
     */
    private void removeCachedMIBPrefs() {
        for (int i = 0; i < 1000; i++) {
            prefs.remove("MIB" + i);
        }
    }
	
    /**
     * Adds a specified MIB file preference.
     *
     * @param file           the MIB file to add
     */
    private void addFilePref(File file) {
        ArrayList  list = getFilePrefs();
		
		if (!list.contains(file.getAbsolutePath())) {
            prefs.put("file" + list.size(), file.getAbsolutePath());
        }
    }

    /**
     * Removes a specified MIB file preference.
     *
     * @param file           the MIB file to remove
     */
    private void removeFilePref(File file) {
        ArrayList  list = getFilePrefs();

        removeFilePrefs();
        list.remove(file.getAbsolutePath());
        for (int i = 0; i < list.size(); i++) {
            prefs.put("file" + i, list.get(i).toString());
        }
    }

    /**
     * Returns the application MIB file preferences.
     *
     * @return the list of MIB files to load
     */
    private ArrayList getFilePrefs() {
        ArrayList  list = new ArrayList();

        for (int i = 0; i < 1000; i++) {
            String str = prefs.get("file" + i, null);
            if (str != null) {
                list.add(str);
            }
        }
        return list;
    }

    /**
     * Removes all application MIB file preferences.
     */
    private void removeFilePrefs() {
        for (int i = 0; i < 1000; i++) {
            prefs.remove("file" + i);
        }
    }
}
