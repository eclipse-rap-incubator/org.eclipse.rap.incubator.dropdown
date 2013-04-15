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

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.rap.clientscripting.ClientListener;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.client.WidgetDataWhiteList;
import org.eclipse.rap.rwt.internal.client.WidgetDataWhiteListImpl;
import org.eclipse.rap.rwt.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.remote.UniversalRemoteObject;
import org.eclipse.swt.widgets.Text;


@SuppressWarnings("restriction")
public class DropDownViewer {

  private static final String ATTR_CLIENT_LISTNER_HOLDER
    = ClientListenerHolder.class.getName() + "#instance";

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



  public DropDownViewer( Text text ) {
    dropDown = new DropDown( text );
    this.text = text;
    clientListener = getClientListenerHolder();
    remoteObject = new UniversalRemoteObject();
    attachClientListener();
    linkClientObjects();
    addWidgetDataKeys( new String[]{ VIEWER_LINK } );
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

  private void updateElements() {
    String[] elements = new String[ this.input.length ];
    for( int i = 0; i < elements.length; i++ ) {
      elements[ i ] = labelProvider.getText( this.input[ i ] );
    }
    remoteObject.set( ELEMENTS_KEY, elements );
  }

  // TODO : detach on destroy
  private void attachClientListener() {
    getTextModifyListener().addTo( text, ClientListener.Modify );
    getTextVerifyListener().addTo( text, ClientListener.Verify );
    getTextKeyDownListener().addTo( text, ClientListener.KeyDown );
    getDropDownSelectionListener().addTo( dropDown, ClientListener.Selection );
    getDropDownDefaultSelectionListener().addTo( dropDown, ClientListener.DefaultSelection );
  }

  private void linkClientObjects() {
    text.setData( VIEWER_LINK, remoteObject.getId() );
    dropDown.setData( VIEWER_LINK, remoteObject.getId() );
    remoteObject.set( DROPDOWN_KEY, WidgetUtil.getId( dropDown ) );
    remoteObject.set( TEXT_KEY, WidgetUtil.getId( text ) );
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

  private class ClientListenerHolder {

    private ClientListener textModifyListener = new ClientListener( "" );
    private ClientListener textVerifyListener = new ClientListener( "" );
    private ClientListener textKeyDownListener = new ClientListener( "" );
    private ClientListener dropDownSelectionListener = new ClientListener( "" );
    private ClientListener dropDownDefaultSelectionListener = new ClientListener( "" );

    ClientListener getTextModifyListener() {
      return textModifyListener;
    }
    public ClientListener getDropDownDefaultSelectionListener() {
      return dropDownDefaultSelectionListener;
    }
    public ClientListener getDropDownSelectionListener() {
      return dropDownSelectionListener;
    }
    public ClientListener getTextKeyDownListener() {
      return textKeyDownListener;
    }
    public ClientListener getTextVerifyListener() {
      return textVerifyListener;
    }

  }

}
