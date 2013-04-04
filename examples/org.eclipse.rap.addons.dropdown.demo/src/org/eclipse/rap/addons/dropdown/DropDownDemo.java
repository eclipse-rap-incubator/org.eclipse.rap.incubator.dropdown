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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;


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
    final DropDown dropdown = createDropDown( text, textBox );
    open.addListener( SWT.Selection, new Listener() {
      public void handleEvent( Event event ) {
        // TODO : This should be a client listener
        dropdown.show();
      }
    } );
    dropdown.setVisibleItemCount( 3 );
    dropdown.setData( "data", getClientData( "Nations" ) );
  }

  private void createMoviesExample( Composite parent ) {
    Text text = createText( parent, SWT.BORDER );
    text.setMessage( "90's Movies" );
    DropDown dropdown = createDropDown( text, text );
    dropdown.setData( "data", getClientData( "Movies" ) );
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
