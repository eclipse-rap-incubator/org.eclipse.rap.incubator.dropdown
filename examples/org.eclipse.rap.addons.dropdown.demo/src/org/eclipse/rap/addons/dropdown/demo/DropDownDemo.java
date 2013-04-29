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

import org.eclipse.jface.viewers.*;
import org.eclipse.rap.addons.dropdown.*;
import org.eclipse.rap.addons.dropdown.viewer.*;
import org.eclipse.rap.addons.dropdown.viewer.SelectionChangedEvent;
import org.eclipse.rap.clientscripting.ClientListener;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.application.AbstractEntryPoint;
import org.eclipse.rap.rwt.client.service.JavaScriptLoader;
import org.eclipse.rap.rwt.internal.client.WidgetDataWhiteList;
import org.eclipse.rap.rwt.internal.remote.RemoteObjectImpl;
import org.eclipse.rap.rwt.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.rap.rwt.service.ResourceManager;
import org.eclipse.rap.rwt.widgets.DialogUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;


@SuppressWarnings("restriction")
public class DropDownDemo extends AbstractEntryPoint {

  private final String PATH_PREFIX = "/org/eclipse/rap/addons/dropdown/demo/";
  DropDownViewer viewer = null;

  @Override
  protected void createContents( Composite parent ) {
    getShell().setLayout( new GridLayout( 1, false ) );
    WidgetDataWhiteList list = RWT.getClient().getService( WidgetDataWhiteList.class );
    list.setKeys( new String[]{ "dropdown", "text", "data" } );
    createKFZExample( parent );
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
    final DropDown dropdown = createDropDown( text, textBox );
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
    DropDown dropdown = createDropDown( text, text );
    dropdown.setData( "data", getClientData( "Movies" ) );
  }

  private void create80sMoviesExample( Composite parent ) {
    Group group = new Group( parent, SWT.NONE );
    group.setText( "DropDown only, 760 entries, no ClientScripting" );
    group.setLayout( new GridLayout( 1, true ) );
    final Text text = new Text( group, SWT.BORDER );
    GridData gridData = new GridData( 200, 23 );
    gridData.verticalAlignment = SWT.TOP;
    text.setLayoutData( gridData );
    text.setMessage( "80's Movies" );
    final DropDown dropdown = new DropDown( text );
    text.addListener( SWT.Modify, new Listener() {
      public void handleEvent( Event event ) {
        if( text.getData( "selecting" ) != Boolean.TRUE ) {
          if( text.getText().length() >= 3 ) {
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
  }

  private void createKFZExample( Composite parent ) {
    Group group = new Group( parent, SWT.NONE );
    group.setText( "DropDownViewer + server-side expand button, dynamic input (586 entries max)" );
    group.setLayout( new GridLayout( 2, false) );
    Composite textBox = new Composite( group, SWT.BORDER );
    GridLayout layout = new GridLayout( 2, false );
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    textBox.setLayoutData( new GridData( SWT.CENTER, SWT.CENTER, false, false) );
    textBox.setLayout( layout );
    Text text = new Text( textBox, SWT.NONE );
    GridData gridData = new GridData( 200, 23 );
    gridData.verticalAlignment = SWT.TOP;
    text.setLayoutData( gridData );
    text.setMessage( "City" );
    Button open = new Button( textBox, SWT.ARROW | SWT.DOWN );
    open.setLayoutData( new GridData( SWT.RIGHT, SWT.FILL, false, true ) );
    open.addListener( SWT.Selection, new Listener() {
      public void handleEvent( Event event ) {
        viewer.getDropDown().show();
      }
    } );
    viewer = new DropDownViewer( text );
    viewer.setLabelProvider( new LabelProvider() {
      @Override
      public String getText( Object object ) {
        String[] entry = ( ( String[] )object );
        if( entry.length == 4 ) {
          return entry[ 2 ]; // germany
        }
        return entry[ 1 ]; // austria
      }
    } );
    viewer.setContentProvider( new IStructuredContentProvider() {
      public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
      }

      public void dispose() {
      }

      public Object[] getElements( Object inputElement ) {
        return ( Object[] )inputElement;
      }
    } );
    viewer.addSelectionChangedListener( new SelectionChangedListener() {
      public void selectionChanged( SelectionChangedEvent event ) {
        String[] city = ( String[] )event.item;
        MessageBox box = new MessageBox( getShell() );
        box.setMessage(
           "Your area identifier is "
          + city[ 0 ]
          + ", which is "
          + city[ 1 ]
        );
        DialogUtil.open( box, null );
      }
    } );
    Group location = new Group( group, SWT.NONE );
    location.setText( "Location" );
    location.setLayoutData( new GridData( SWT.TOP, SWT.CENTER, false, true ) );
    location.setLayout( new GridLayout( 1, true ) );
    final Button germany = new Button( location, SWT.RADIO );
    germany.setText( "Germany" );
    germany.addListener( SWT.Selection, new Listener() {
      public void handleEvent( Event event ) {
        if( germany.getSelection() ) {
          loadGermany();
        }
      }
    } );
    final Button austria = new Button( location, SWT.RADIO );
    austria.setText( "Austria" );
    austria.addListener( SWT.Selection, new Listener() {
      public void handleEvent( Event event ) {
        if( austria.getSelection() ) {
          loadAustria();
        }
      }
    } );
  }

  void loadGermany() {
    viewer.setInput( KFZ.DE );
  }

  void loadAustria() {
    viewer.setInput( KFZ.AT );
  }

  private Text createText( Composite parent, int style ) {
    Text text = new Text( parent, style );
    GridData gridData = new GridData( 200, 23 );
    gridData.verticalAlignment = SWT.TOP;
    text.setLayoutData( gridData );
    addTextClientListener( text );
    return text;
  }

  private DropDown createDropDown( Text text, Control parent ) {
    DropDown dropdown = new DropDown( text );
    dropdown.setData( "text", WidgetUtil.getId( text ) );
    text.setData( "dropdown", WidgetUtil.getId( dropdown ) );
    addDropDownClientListener( dropdown );
    return dropdown;
  }

  private void addDropDownClientListener( DropDown dropdown ) {
    String script = ResourceLoaderUtil.readTextContent( PATH_PREFIX + "DropDownEventHandler.js" );
    ClientListener listener = new ClientListener( script );
    listener.addTo( dropdown, SWT.Selection );
    listener.addTo( dropdown, SWT.DefaultSelection );
  }

  private void addButtonClientListener( Button button ) {
    String script = ResourceLoaderUtil.readTextContent( PATH_PREFIX + "ButtonEventHandler.js" );
    ClientListener listener = new ClientListener( script );
    listener.addTo( button, SWT.MouseDown );
  }

  private void addTextClientListener( Text text ) {
    String script = ResourceLoaderUtil.readTextContent( PATH_PREFIX + "TextEventHandler.js" );
     // TODO: should take inputStream or loader + path
    ClientListener listener = new ClientListener( script );
    listener.addTo( text, SWT.Modify );
    listener.addTo( text, SWT.Verify );
    listener.addTo( text, SWT.KeyDown );
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

  private String[] filter( String[] values, String text, int limit ) {
    List<String> result = new ArrayList<String>( limit );
    for( int i = 0; result.size() < limit && i < values.length; i++ ) {
      if( values[ i ].toLowerCase().startsWith( text ) ) {
        result.add( values[ i ] );
      }
    }
    return result.toArray( new String[ result.size() ] );
  }

}
