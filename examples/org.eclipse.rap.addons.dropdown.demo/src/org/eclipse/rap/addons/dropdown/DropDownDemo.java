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

import static org.eclipse.rap.addons.dropdown.viewer.old.ResourceLoaderUtil.readTextContent;

import java.util.Arrays;

import org.eclipse.jface.viewers.LabelProvider;
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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
//import org.eclipse.rap.addons.dropdown.viewer.old.DropDownViewer;
//import org.eclipse.rap.addons.dropdown.viewer.old.LabelProvider;
//import org.eclipse.rap.addons.dropdown.viewer.old.SelectionChangedEvent;
//import org.eclipse.rap.addons.dropdown.viewer.old.SelectionChangedListener;


@SuppressWarnings("restriction")
public class DropDownDemo extends AbstractEntryPoint {

  private String PATH_PREFIX = "/org/eclipse/rap/addons/dropdown/";

  @Override
  protected void createContents( Composite parent ) {
    getShell().setLayout( new GridLayout( 2, false ) );
    WidgetDataWhiteList list = RWT.getClient().getService( WidgetDataWhiteList.class );
    list.setKeys( new String[]{ "dropdown", "text", "data" } );
    createNationsExample( parent );
    createMoviesExample( parent );
    createKFZExample( parent );
  }

  private void createNationsExample( Composite parent ) {
    // TODO : Use CSS Theming on textBox / open button
    Composite textBox = new Composite( parent, SWT.BORDER );
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

  private void createMoviesExample( Composite parent ) {
    Text text = createText( parent, SWT.BORDER );
    text.setMessage( "90's Movies" );
    DropDown dropdown = createDropDown( text, text );
    dropdown.setData( "data", getClientData( "Movies" ) );
  }

  private void createKFZExample( Composite parent ) {
    Text text = new Text( parent, SWT.BORDER );
    GridData gridData = new GridData( 200, 23 );
    gridData.verticalAlignment = SWT.TOP;
    text.setLayoutData( gridData );
    text.setMessage( "German City" );
    DropDownViewer dropdown = new DropDownViewer( text );
    dropdown.setLabelProvider( new LabelProvider() {
      @Override
      public String getText( Object object ) {
        return ( ( String[] )object )[ 2 ];
      }
    } );
    dropdown.setInput( Arrays.asList( KFZ.VALUES ) );
    dropdown.addSelectionChangedListener( new SelectionChangedListener() {
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
    DropDown dropdown = new DropDown( parent );
    dropdown.setData( "text", WidgetUtil.getId( text ) );
    text.setData( "dropdown", WidgetUtil.getId( dropdown ) );
    addDropDownClientListener( dropdown );
    return dropdown;
  }

  private void addDropDownClientListener( DropDown dropdown ) {
    String script = readTextContent( PATH_PREFIX + "DropDownEventHandler.js" );
    ClientListener listener = new ClientListener( script );
    listener.addTo( dropdown, SWT.Selection );
    listener.addTo( dropdown, SWT.DefaultSelection );
    //listener.addTo( dropdown, SWT.KeyDown ); // currently not supported, implement?
  }

  private void addButtonClientListener( Button button ) {
    String script = readTextContent( PATH_PREFIX + "ButtonEventHandler.js" );
    ClientListener listener = new ClientListener( script );
    listener.addTo( button, SWT.MouseDown );
  }

  private void addTextClientListener( Text text ) {
    String script = readTextContent( PATH_PREFIX + "TextEventHandler.js" );
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

}
