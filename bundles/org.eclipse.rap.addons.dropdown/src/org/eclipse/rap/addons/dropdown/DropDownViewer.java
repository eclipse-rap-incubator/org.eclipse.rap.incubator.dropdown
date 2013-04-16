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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.rap.addons.dropdown.internal.resources.ResourceLoaderUtil;
import org.eclipse.rap.clientscripting.ClientListener;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.client.WidgetDataWhiteList;
import org.eclipse.rap.rwt.internal.client.WidgetDataWhiteListImpl;
import org.eclipse.rap.rwt.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.remote.AbstractOperationHandler;
import org.eclipse.rap.rwt.remote.UniversalRemoteObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;


@SuppressWarnings("restriction")
public class DropDownViewer {

  private static final String ATTR_CLIENT_LISTNER_HOLDER
    = ClientListenerHolder.class.getName() + "#instance";
  private static final String SELECTION_CHANGED = "SelectionChanged";
  private static String VIEWER_LINK =
      DropDownViewer.class.getName() + "#viewer";
  private static String DROPDOWN_KEY = "dropDown";
  private static String TEXT_KEY = "text";
  private static String ELEMENTS_KEY = "elements";

  private DropDown dropDown;
  private Text text;
  private Object[] input;
  private ClientListenerHolder clientListener;
  private UniversalRemoteObject remoteObject;
  private ILabelProvider labelProvider;
  private List< SelectionChangedListener > selectionChangedListeners
    = new ArrayList< SelectionChangedListener >();

  public DropDownViewer( Text text ) {
    dropDown = new DropDown( text );
    this.text = text;
    clientListener = getClientListenerHolder();
    remoteObject = new UniversalRemoteObject();
    remoteObject.setHandler( new InternalOperationHandler() );
    attachClientListener();
    linkClientObjects();
    attachDisposeListener();
  }

  public DropDown getDropDown() {
    return dropDown;
  }

  public void setLabelProvider( ILabelProvider provider ) {
    labelProvider = provider;
    if( input != null ) {
      updateElements();
    }
  }

  public void setInput( List<?> input ) {
    this.input = input.toArray();
    updateElements();
  }

  public void addSelectionChangedListener( SelectionChangedListener listener ) {
    selectionChangedListeners.add( listener );
    remoteObject.listen( SELECTION_CHANGED, true );
  }

  ////////////
  // Internals

  private void updateElements() {
    String[] elements = new String[ this.input.length ];
    for( int i = 0; i < elements.length; i++ ) {
      elements[ i ] = labelProvider.getText( this.input[ i ] );
    }
    // TODO : Using a separate client object (e.g. "RemoteList") for the elements might allow
    //        sharing and incremental updates
    remoteObject.set( ELEMENTS_KEY, elements );
  }

  private void attachClientListener() {
    getTextModifyListener().addTo( text, ClientListener.Modify );
    getTextVerifyListener().addTo( text, ClientListener.Verify );
    getTextKeyDownListener().addTo( text, ClientListener.KeyDown );
    getDropDownSelectionListener().addTo( dropDown, ClientListener.Selection );
    getDropDownDefaultSelectionListener().addTo( dropDown, ClientListener.DefaultSelection );
  }

  private void detachClientListener() {
    getTextModifyListener().removeFrom( text, ClientListener.Modify );
    getTextVerifyListener().removeFrom( text, ClientListener.Verify );
    getTextKeyDownListener().removeFrom( text, ClientListener.KeyDown );
    getDropDownSelectionListener().removeFrom( dropDown, ClientListener.Selection );
    getDropDownDefaultSelectionListener().removeFrom( dropDown, ClientListener.DefaultSelection );
  }

  private void linkClientObjects() {
    // NOTE : Order is relevant, DropDown renders immediately!
    addWidgetDataKeys( new String[]{ VIEWER_LINK } );
    text.setData( VIEWER_LINK, remoteObject.getId() );
    dropDown.setData( VIEWER_LINK, remoteObject.getId() );
    remoteObject.set( DROPDOWN_KEY, WidgetUtil.getId( dropDown ) );
    remoteObject.set( TEXT_KEY, WidgetUtil.getId( text ) );
  }

  private void attachDisposeListener() {
    dropDown.addListener( SWT.Dispose, new Listener() {
      public void handleEvent( Event event ) {
        remoteObject.destroy();
        detachClientListener();
      }
    } );
  }

  private static void addWidgetDataKeys( String[] keys ) {
    WidgetDataWhiteListImpl service
      = ( WidgetDataWhiteListImpl )RWT.getClient().getService( WidgetDataWhiteList.class );
    String[] currentKeys = service.getKeys() != null ? service.getKeys() : new String[ 0 ];
    List<String> list = new ArrayList<String>( Arrays.asList( currentKeys ) );
    for( int i = 0; i < keys.length; i++ ) {
      if( !list.contains( keys[ i ] ) ) {
        list.add( keys[ i ] );
      }
    };
    service.setKeys( list.toArray( new String[ list.size() ]) );
  }

  UniversalRemoteObject getRemoteObject() {
    return remoteObject;
  }

  ClientListener getTextModifyListener() {
    return clientListener.getTextModifyListener();
  }

  ClientListener getTextVerifyListener() {
    return clientListener.getTextVerifyListener();
  }

  ClientListener getTextKeyDownListener() {
    return clientListener.getTextKeyDownListener();
  }

  ClientListener getDropDownSelectionListener() {
    return clientListener.getDropDownSelectionListener();
  }

  ClientListener getDropDownDefaultSelectionListener() {
    return clientListener.getDropDownDefaultSelectionListener();
  };

  // TODO : make exchangeable by accepting a class that extends the holder in the constructor
  private ClientListenerHolder getClientListenerHolder() {
    Object result = RWT.getUISession().getAttribute( ATTR_CLIENT_LISTNER_HOLDER );
    if( result == null ) {
      result = new ClientListenerHolder();
      RWT.getUISession().setAttribute( ATTR_CLIENT_LISTNER_HOLDER, result );
    }
    return ( ClientListenerHolder )result;
  }

  private void fireSelectionChanged( int index ) {
    Object element = input[ index ];
    SelectionChangedEvent event = new SelectionChangedEvent( element );
    for( SelectionChangedListener listener : selectionChangedListeners ) {
      listener.selectionChanged( event );
    }
  }

  private class InternalOperationHandler extends AbstractOperationHandler {

    @Override
    public void handleNotify( String event, Map<String, Object> properties ) {
      if( SELECTION_CHANGED.equals( event ) ) {
        int index = ( ( Integer )properties.get( "index" ) ).intValue();
        DropDownViewer.this.fireSelectionChanged( index );
      }
    }

  }

  private class ClientListenerHolder {

    private String PREFIX = "org/eclipse/rap/addons/dropdown/internal/resources/";

    private final ClientListener textListener = createListener( "TextEventListener.js" );
    private final ClientListener dropDownListener = createListener( "DropDownEventListener.js" );

    private ClientListener createListener( String name ) {
      return new ClientListener( ResourceLoaderUtil.readTextContent( PREFIX + name ) );
    }

    public ClientListener getTextModifyListener() {
      return textListener;
    }

    public ClientListener getDropDownDefaultSelectionListener() {
      return dropDownListener;
    }

    public ClientListener getDropDownSelectionListener() {
      return dropDownListener;
    }

    public ClientListener getTextKeyDownListener() {
      return textListener;
    }

    public ClientListener getTextVerifyListener() {
      return textListener;
    }

  }

}
