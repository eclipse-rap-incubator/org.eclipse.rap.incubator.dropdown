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

import org.eclipse.rap.clientscripting.ClientListener;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.widgets.Text;


public class DropDownViewer {

  private static final String ATTR_CLIENT_LISTNER_HOLDER
    = ClientListenerHolder.class.getName() + "#instance";

  private DropDown dropDown;
  private Text text;
  private ClientListenerHolder clientListener;


  public DropDownViewer( Text text ) {
    dropDown = new DropDown( text );
    this.text = text;
    clientListener = getClientListenerHolder();
    attachClientListener();
  }

  public DropDown getDropDown() {
    return dropDown;
  }

  // TODO : detach on destroy
  private void attachClientListener() {
    getTextModifyListener().addTo( text, ClientListener.Modify );
    getTextVerifyListener().addTo( text, ClientListener.Verify );
    getTextKeyDownListener().addTo( text, ClientListener.KeyDown );
    getDropDownSelectionListener().addTo( dropDown, ClientListener.Selection );
    getDropDownDefaultSelectionListener().addTo( dropDown, ClientListener.DefaultSelection );
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
