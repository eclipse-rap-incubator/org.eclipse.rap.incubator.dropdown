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
package org.eclipse.rap.addons.dropdown.demo;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rap.addons.dropdown.DropDown;
import org.eclipse.rap.clientscripting.ClientListener;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.application.AbstractEntryPoint;
import org.eclipse.rap.rwt.client.service.JavaScriptLoader;
import org.eclipse.rap.rwt.internal.client.WidgetDataWhiteList;
import org.eclipse.rap.rwt.internal.client.WidgetDataWhiteListImpl;
import org.eclipse.rap.rwt.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.rap.rwt.service.ResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;


@SuppressWarnings("restriction")
public class DropDownDemo extends AbstractEntryPoint {

  private final String PATH_PREFIX = "/org/eclipse/rap/addons/dropdown/demo/";

  @Override
  protected void createContents( Composite parent ) {
    getShell().setLayout( new GridLayout( 1, false ) );
    WidgetDataWhiteListImpl list
      = ( WidgetDataWhiteListImpl )RWT.getClient().getService( WidgetDataWhiteList.class );
    list.setKeys( new String[]{ "dropdown", "text", "data" } );
    createNationsExample( parent );
    create90sMoviesExample( parent );
    create80sMoviesExample( parent );
  }

  private void createNationsExample( Composite parent ) {
    // TODO : Use CSS Theming on textBox / open button
    Group group = new Group( parent, SWT.NONE );
    group.setText( "DropDown + client expand button, 192 entries cached" );
    group.setLayout( new GridLayout( 1, true ) );
    Composite textBox = new Composite( group, SWT.BORDER );
    GridLayout layout = new GridLayout( 2, false );
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    textBox.setLayout( layout );
    Text text = createText( textBox, SWT.NONE );
    text.setMessage( "Nations" );
    Button open = new Button( textBox, SWT.ARROW | SWT.DOWN );
    open.setLayoutData( new GridData( SWT.RIGHT, SWT.FILL, false, true ) );
    addButtonClientListener( open );
    final DropDown dropdown = createDropDown( text );
    open.setData( "dropdown", WidgetUtil.getId( dropdown ) );
    dropdown.setVisibleItemCount( 3 );
    dropdown.setData( "data", getClientData( "Nations" ) );
  }

  private void create90sMoviesExample( Composite parent ) {
    Group group = new Group( parent, SWT.NONE );
    group.setText( "DropDown only, 1157 entries cached" );
    group.setLayout( new GridLayout( 1, true ) );
    Text text = createText( group, SWT.BORDER );
    text.setMessage( "90's Movies" );
    DropDown dropdown = createDropDown( text );
    dropdown.setData( "data", getClientData( "Movies" ) );
  }

  private static void create80sMoviesExample( Composite parent ) {
    Group group = new Group( parent, SWT.NONE );
    group.setText( "DropDown only, 760 entries, no ClientScripting, markup" );
    group.setLayout( new GridLayout( 1, true ) );
    final Text text = new Text( group, SWT.BORDER );
    GridData gridData = new GridData( 200, 23 );
    gridData.verticalAlignment = SWT.TOP;
    text.setLayoutData( gridData );
    text.setMessage( "80's Movies" );
    final DropDown dropdown = new DropDown( text );
    dropdown.setData( RWT.MARKUP_ENABLED, Boolean.TRUE );
    text.addListener( SWT.Modify, new Listener() {
      public void handleEvent( Event event ) {
        if( text.getData( "selecting" ) != Boolean.TRUE ) {
          if( text.getText().length() >= 2 ) {
            dropdown.show();
            dropdown.setItems( filter( Movies.VALUES, text.getText().toLowerCase(), 10 ) );
          } else {
            dropdown.hide();
          }
        }
      }
    } );
    dropdown.addListener( SWT.Selection, new Listener() {
      public void handleEvent( Event event ) {
        if( event.text.length() > 0 ) {
          text.setData( "selecting", Boolean.TRUE );
          text.setText( event.text );
          text.setData( "selecting", Boolean.FALSE );
          text.selectAll();
        }
      }
    } );
    dropdown.addListener( SWT.DefaultSelection, new Listener() {
      public void handleEvent( Event event ) {
        text.setText( event.text );
        text.setSelection( event.text.length() );
        dropdown.hide();
      }
    } );
    dropdown.setData( "columns", new int[] { 300, 60 } );
  }

  private Text createText( Composite parent, int style ) {
    Text text = new Text( parent, style );
    GridData gridData = new GridData( 200, 23 );
    gridData.verticalAlignment = SWT.TOP;
    text.setLayoutData( gridData );
    addTextClientListener( text );
    return text;
  }

  private DropDown createDropDown( Text text ) {
    DropDown dropdown = new DropDown( text );
    dropdown.setData( "text", WidgetUtil.getId( text ) );
    text.setData( "dropdown", WidgetUtil.getId( dropdown ) );
    addDropDownClientListener( dropdown );
    return dropdown;
  }

  private void addDropDownClientListener( DropDown dropdown ) {
    String script = ResourceLoaderUtil.readTextContent( PATH_PREFIX + "DropDownEventHandler.js" );
    ClientListener listener = new ClientListener( script );
    dropdown.addListener( SWT.Selection, listener );
    dropdown.addListener( SWT.DefaultSelection, listener );
  }

  private void addButtonClientListener( Button button ) {
    String script = ResourceLoaderUtil.readTextContent( PATH_PREFIX + "ButtonEventHandler.js" );
    ClientListener listener = new ClientListener( script );
    button.addListener( SWT.MouseDown, listener );
  }

  private void addTextClientListener( Text text ) {
    String script = ResourceLoaderUtil.readTextContent( PATH_PREFIX + "TextEventHandler.js" );
     // TODO: should take inputStream or loader + path
    ClientListener listener = new ClientListener( script );
    text.addListener( SWT.Modify, listener );
    text.addListener( SWT.Verify, listener );
    text.addListener( SWT.KeyDown, listener );
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
    return remoteObject.getId();
  }

  private static String[] filter( String[] values, String text, int limit ) {
    List<String> result = new ArrayList<String>( limit );
    for( int i = 0; result.size() < limit && i < values.length; i++ ) {
      String item = values[ i ];
      if( item.toLowerCase().startsWith( text ) ) {
        int length = text.length();
        int rating = ( int )( Math.random() * 10 );
        result.add(
          "<b>" + item.substring( 0, length ) + "</b>" + item.substring( length ) + "\t" + rating + "/10"
        );
      }
    }
    return result.toArray( new String[ result.size() ] );
  }

}
