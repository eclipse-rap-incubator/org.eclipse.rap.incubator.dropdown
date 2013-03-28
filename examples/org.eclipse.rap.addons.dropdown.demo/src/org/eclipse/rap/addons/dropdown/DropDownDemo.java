/*******************************************************************************
 * Copyright (c) 2013 EclipseSource.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

package org.eclipse.rap.addons.dropdown;

import static org.eclipse.rap.addons.dropdown.ResourceLoaderUtil.readTextContent;

import org.eclipse.rap.clientscripting.ClientListener;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.application.AbstractEntryPoint;
import org.eclipse.rap.rwt.client.service.JavaScriptLoader;
import org.eclipse.rap.rwt.internal.client.WidgetDataWhiteList;
import org.eclipse.rap.rwt.internal.remote.RemoteObjectImpl;
import org.eclipse.rap.rwt.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.rap.rwt.service.ResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;


@SuppressWarnings("restriction")
public class DropDownDemo extends AbstractEntryPoint {

  private DropDown dropdown;
  private Text text;

  @Override
  protected void createContents( Composite parent ) {
    getShell().setLayout( new GridLayout( 2, false ) );
    WidgetDataWhiteList list = RWT.getClient().getService( WidgetDataWhiteList.class );
    list.setKeys( new String[]{ "dropdown", "text", "nations" } );
    createText( parent );
    createTestControls( new Composite( parent, SWT.NONE ) );
  }

  private void createText( Composite parent ) {
    text = new Text( parent, SWT.BORDER );
    GridData gridData = new GridData( 200, 23 );
    gridData.verticalAlignment = SWT.TOP;
    text.setLayoutData( gridData );
    dropdown = new DropDown( text );
    text.setData( "dropdown", WidgetUtil.getId( dropdown ) );
    dropdown.setData( "text", WidgetUtil.getId( text ) );
    dropdown.setData( "nations", getClientData( "Nations" ) );
    addTextClientListener( text );
  }

  private void addTextClientListener( Text text ) {
    String script
      = readTextContent( "/org/eclipse/rap/addons/dropdown/TextEventHandler.js" );
     // TODO: should take inputStream or loader + path
    ClientListener listener = new ClientListener( script );
    listener.addTo( text, SWT.Modify );
  }

  private void createTestControls( final Composite parent ) {
    parent.setLayoutData( new GridData( 400, 400 ) );
    parent.setLayout( new RowLayout() );
    createButton( parent, "show", new Listener() {
      public void handleEvent( Event event ) {
        dropdown.show();
      }
    } );
    createButton( parent, "hide", new Listener() {
      public void handleEvent( Event event ) {
        dropdown.hide();
      }
    } );
    createButton( parent, "recreate", new Listener() {
      public void handleEvent( Event event ) {
        text.dispose();
        createText( parent.getParent() );
        text.moveAbove( parent );
        dropdown.show();
        getShell().layout();
      }
    } );
    createButton( parent, "server listener", new Listener() {
      public void handleEvent( Event event ) {
        text.addListener( SWT.Modify, new Listener() {
          public void handleEvent( Event event ) {
            dropdown.show();
          }
        } );
      }
    } );
  }

  private static void createButton( Composite composite, String text, Listener listener ) {
    Button show = new Button( composite, SWT.PUSH );
    show.setText( text );
    show.addListener( SWT.Selection, listener);
  }

  // Experimental: Uses internal API, but allows to use cacheable data without polluting the
  // public namespace.
  private String getClientData( String type ) {
    ResourceManager manager = RWT.getResourceManager();
    String registerFilename = "clientData/" + type + ".js";
    if( !manager.isRegistered( registerFilename ) ) {
      manager.register( registerFilename, getClass().getResourceAsStream( type + ".js" ) );
    }
    JavaScriptLoader jsl = RWT.getClient().getService( JavaScriptLoader.class );
    jsl.require( manager.getLocation( registerFilename ) );
    RemoteObject remoteObject
      = RWT.getUISession().getConnection().createRemoteObject( "clientData." + type );
    return ( ( RemoteObjectImpl )remoteObject ).getId();
  }

}
