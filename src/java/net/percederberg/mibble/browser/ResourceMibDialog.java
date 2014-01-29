/*
 * ResourceMibDialog.java
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
 * Copyright (c) 2009 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble.browser;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Enumeration;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JDialog;
import javax.swing.JTree;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.Box;
import javax.swing.tree.TreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeModel;
import javax.swing.tree.DefaultTreeModel;

/**
 * A window for selection of MIBs from the resources.
 *
 * @author   Tex Clayton <tex at dartware dot com>
 * @version  1.0
 * @since    2.9.3
 */
public class ResourceMibDialog extends JDialog {
    /**
     * The MIB tree component.
     */
    private JTree mibTree = null;
	
    /**
     * The confirmation button.
     */
    private JButton okButton = new JButton( "OK" );

    /**
     * The cancellation button.
     */
    private JButton cancelButton = new JButton( "Cancel" );
	
	ResourceMibDialog( Frame parent, Map mibs )
	{
		super( parent, "Choose a MIB", true );
		
		Container contentPane = getContentPane();
		contentPane.setLayout( new BorderLayout( 0, 4 ) );
		
		DefaultTreeModel treeModel = new DefaultTreeModel(null);
		Iterator keys = mibs.keySet().iterator();
		Iterator values;
		Object key;
		String mibDir;
		String[] path;
		String mibName;
		while ( keys.hasNext() ) {
			key = keys.next();
			if ( key != null ) {
				mibDir = key.toString();
				path = mibDir.split( java.io.File.separator );
				if ( path.length > 0 ) {
					// add all files for this resourceDir
					values = ((List) mibs.get( key )).iterator();
					while ( values.hasNext() ) {
						getTreeNode( treeModel, values.next().toString().split( java.io.File.separator ) );
					}
				}
			}
		}
		
		mibTree = new JTree( treeModel );
		mibTree.setRootVisible( false );
		mibTree.setShowsRootHandles( true );
		//expand the top groups in the tree
		for ( int i = mibTree.getRowCount()-1; i >= 0; i-- ) {
			mibTree.expandRow( i );
		}
		contentPane.add( new JScrollPane( mibTree ), BorderLayout.CENTER );
		
		Container buttonRow = Box.createHorizontalBox();
		buttonRow.add( Box.createHorizontalGlue() );
		buttonRow.add( cancelButton );
		buttonRow.add( Box.createHorizontalStrut( 8 ) );
		buttonRow.add( okButton );
		contentPane.add( buttonRow, BorderLayout.SOUTH );
		
		cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancel();
            }
        });
		okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                confirm();
            }
        });
		
		pack();
	}
	
	public String[] getSelectedMIBs()
	{
		// get tree selection
		// get names of each MIB selected
		// return array of MIB names
		
		TreePath[] selectedPaths = mibTree.getSelectionPaths();
		if ( selectedPaths == null ) {
			selectedPaths = new TreePath[0];
		}
		ArrayList mibs = new ArrayList( selectedPaths.length );
		Object node;
		
		for ( int i = 0; i < selectedPaths.length; i++ ) {
			node = selectedPaths[i].getLastPathComponent();
			mibs.add( node.toString() );
		}
		
		return (String[]) mibs.toArray( new String[ mibs.size() ] );
	}
	
	private void confirm()
	{
		setVisible( false );
	}
	
	private void cancel()
	{
		if ( mibTree != null ) {
			//clear the selection
			mibTree.removeSelectionRows( mibTree.getSelectionRows() );
		}
		setVisible( false );
	}
	
	private static MutableTreeNode getTreeNode( TreeModel model, String[] path )
	{
		MutableTreeNode node = null;
		if ( (path != null) && (path.length > 0) ) {
			MutableTreeNode root = (MutableTreeNode) model.getRoot();
			if ( root == null ) {
				root = node = new DefaultMutableTreeNode( path[0] );
				if ( model instanceof DefaultTreeModel ) {
					((DefaultTreeModel) model).setRoot( root );
				}
			}
			if ( root instanceof DefaultMutableTreeNode ) {
				if ( path[0].equals( ((DefaultMutableTreeNode) root).getUserObject() ) ) {
					String[] tmp = new String[ path.length - 1 ];
					System.arraycopy( path, 1, tmp, 0, tmp.length );
					path = tmp;
				}
			}
			if ( path.length > 0 ) {
				node = getTreeNode( root, path );
			}
		}
		return node;
	}
	
	private static MutableTreeNode getTreeNode( MutableTreeNode node, String[] path )
	{
		MutableTreeNode foundNode = null;
		String name = path[0];
		foundNode = getTreeNode( node, name );
		if ( (foundNode != null) && (path.length > 1) ) {
			String[] tmp = new String[ path.length - 1 ];
			System.arraycopy( path, 1, tmp, 0, tmp.length );
			foundNode = getTreeNode( foundNode, tmp );
		}
		return foundNode;
	}
	
	private static MutableTreeNode getTreeNode( MutableTreeNode parent, String child )
	{
		Enumeration children = parent.children();
		Object childNode;
		while ( children.hasMoreElements() ) {
			childNode = children.nextElement();
			if ( childNode instanceof DefaultMutableTreeNode ) {
				if ( child.equals( ((DefaultMutableTreeNode) childNode).getUserObject() ) ) {
					return (MutableTreeNode) childNode;
				}
			}
		}
		//child not found in parent, so create it.
		MutableTreeNode newNode = new DefaultMutableTreeNode( child );
		parent.insert( newNode, parent.getChildCount() );
		return newNode;
	}
}
