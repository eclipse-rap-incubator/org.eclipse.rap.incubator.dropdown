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

import java.util.*;
import java.util.List;

import org.eclipse.jface.viewers.*;
import org.eclipse.rap.addons.dropdown.internal.resources.ResourceLoaderUtil;
import org.eclipse.rap.clientscripting.ClientListener;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.client.WidgetDataWhiteList;
import org.eclipse.rap.rwt.internal.client.WidgetDataWhiteListImpl;
import org.eclipse.rap.rwt.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.remote.AbstractOperationHandler;
import org.eclipse.rap.rwt.remote.UniversalRemoteObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;


@SuppressWarnings("restriction")
public class DropDownViewer {

  private static final String ATTR_CLIENT_LISTNER_HOLDER
    = ClientListenerHolder.class.getName() + "#instance";
  private static final String SELECTION_CHANGED = "SelectionChanged";
  private static String VIEWER_LINK = DropDownViewer.class.getName() + "#viewer";
  private static String DROPDOWN_KEY = "dropDown";
  private static String TEXT_KEY = "text";
  private static String ELEMENTS_KEY = "elements";

  private final DropDown dropDown;
  private final Text text;
  private Object input;
  private Object[] elements;
  private final ClientListenerHolder clientListener;
  private final UniversalRemoteObject remoteObject;
  private ILabelProvider labelProvider;
  private final List< SelectionChangedListener > selectionChangedListeners
    = new ArrayList< SelectionChangedListener >();
  private IStructuredContentProvider contentProvider;

  public DropDownViewer( Text text ) {
    this.text = text;
    checkParent();
    dropDown = new DropDown( text );
    clientListener = getClientListenerHolder();
    remoteObject = new UniversalRemoteObject();
    remoteObject.setHandler( new InternalOperationHandler() );
    setClientElements( new String[ 0 ] );
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
      updateClientElements();
    }
  }

  public void setContentProvider( IContentProvider contentProvider ) {
    this.contentProvider = ( IStructuredContentProvider )contentProvider;
    if( input != null ) {
      updateElements();
    }
  }

  public IContentProvider  getContentProvider() {
    return contentProvider;
  }

  public void setInput( Object input ) {
    if( dropDown == null || dropDown.isDisposed() ) {
      String message =   "Need an underlying widget to be able to set the input."
                       + "(Has the widget been disposed?)";
      throw new IllegalStateException( message );
    }
    Object oldInput = this.input;
    this.input = input;
    if( contentProvider != null ) {
      contentProvider.inputChanged( null, oldInput, input );
    }
    updateElements();
  }

  public void addSelectionChangedListener( SelectionChangedListener listener ) {
    if( !selectionChangedListeners.contains( listener ) ) {
      selectionChangedListeners.add( listener );
      remoteObject.listen( SELECTION_CHANGED, true );
    }
  }

  ////////////
  // Internals

  private void checkParent() {
    if( text.getData( VIEWER_LINK ) != null ) {
      throw new IllegalStateException( "Text already has a " + getClass().getSimpleName() );
    }
  }

  private void updateElements() {
    if( contentProvider == null ) {
      String message = "DropDownViewer must have a content provider when input is set.";
      throw new IllegalStateException( message );
    }
    elements = contentProvider.getElements( input );
    updateClientElements();
  }

  private void updateClientElements() {
    if( labelProvider == null ) {
      String message = "DropDownViewer must have a label provider when input is set.";
      throw new IllegalStateException( message );
    }
    String[] clientElements = new String[ elements.length ];
    for( int i = 0; i < elements.length; i++ ) {
      clientElements[ i ] = labelProvider.getText( elements[ i ] );
    }
    setClientElements( clientElements );
  }

  private void setClientElements( String[] elements ) {
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
    getDropDownShowListener().addTo( dropDown, ClientListener.Show );
  }

  private void detachClientListener() {
    getTextModifyListener().removeFrom( text, ClientListener.Modify );
    getTextVerifyListener().removeFrom( text, ClientListener.Verify );
    getTextKeyDownListener().removeFrom( text, ClientListener.KeyDown );
    getDropDownSelectionListener().removeFrom( dropDown, ClientListener.Selection );
    getDropDownDefaultSelectionListener().removeFrom( dropDown, ClientListener.DefaultSelection );
    getDropDownShowListener().removeFrom( dropDown, ClientListener.Show );
  }

  private void linkClientObjects() {
    // NOTE : Order is relevant, DropDown renders immediately!
    addWidgetDataKeys( new String[]{ VIEWER_LINK } );
    text.setData( VIEWER_LINK, remoteObject.getId() );
    dropDown.setData( VIEWER_LINK, remoteObject.getId() );
    remoteObject.set( DROPDOWN_KEY, WidgetUtil.getId( dropDown ) );
    remoteObject.set( TEXT_KEY, WidgetUtil.getId( text ) );
  }

  private void unlinkClientObjects() {
    text.setData( VIEWER_LINK, null );
    dropDown.setData( VIEWER_LINK, null );
  }

  private void attachDisposeListener() {
    dropDown.addListener( SWT.Dispose, new Listener() {
      public void handleEvent( Event event ) {
        unlinkClientObjects();
        detachClientListener();
        remoteObject.destroy();
        if( contentProvider != null ) {
          contentProvider.dispose();
        }
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
    }
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
  }

  public ClientListener getDropDownShowListener() {
    return clientListener.getDropDownShowListener();
  }

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
    Object element = elements[ index ];
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
        fireSelectionChanged( index );
      }
    }

  }

  private static class ClientListenerHolder {

    private final String PREFIX = "org/eclipse/rap/addons/dropdown/internal/resources/";

    private final ClientListener textListener = createListener( "TextEventListener.js" );
    private final ClientListener dropDownListener = createListener( "DropDownEventListener.js" );

    private ClientListener createListener( String name ) {
      return new ClientListener( ResourceLoaderUtil.readTextContent( PREFIX + name ) );
    }

    public ClientListener getDropDownShowListener() {
      return dropDownListener;
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
