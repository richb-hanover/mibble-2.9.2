#README for Dartware mods to Mibble

In January 2010, Dartware contributed a number of changes to Mibble to improve the user experience of the MibbleBrowser.jar program. These changes added several useful features to MibbleBrowser:

* Click the MIB name in the left pane to display the full text of the MIB in the right pane. 
* Keyboard shortcut (Ctl-O) for load/open a MIB file from the hundreds that are built into MibbleBrowser itself. 
* Keyboard shortcut (Ctl-I) to import other MIB files from the customer's hard drive. 
* Mibble now retains a list of the MIBs that were loaded when it quits. 
* Mibble reloads that list of MIBs when it restarts. (If no MIBs are present, it loads a default set of MIBs. Unloading all MIB files is a convenient way to restore the default set of MIBs.) 

For various reasons these changes were never merged into the mainline, but these mods were quite useful, and I would hate to see them lost as the product evolves in other ways.

Toward that end, I have created a github repository that shows the Dartware changes applied to the Mibble 2.9.2 base of 4Apr2009. There were only five files changed, and it is my hope that these could be merged back into the mainline. (I have neither the skills nor the tools to do this work myself.)

See the following links: 

* [Mibble MIB browser available](http://forums.intermapper.com/viewtopic.php?t=1049for) the initial announcement on the InterMapper-Talk mailing list
* [Using the Mibble MIB Browser](http://forums.intermapper.com/viewtopic.php?t=1702) for an overview of the software from the InterMapper Forums
* [Download](http://download.intermapper.com/thirdparty/mibble-2.9.3-dartware.zip) the modified sources as a .zip archive
* [github repository](https://github.com/richb-hanover/mibble-2.9.2) that shows the differences between the original 2.9.2 version and the "Dartware" version.

As stated in the announcement and other postings, Dartware contributed all these changes to Per Cederberg with the full knowledge that they would become GPL licensed. 