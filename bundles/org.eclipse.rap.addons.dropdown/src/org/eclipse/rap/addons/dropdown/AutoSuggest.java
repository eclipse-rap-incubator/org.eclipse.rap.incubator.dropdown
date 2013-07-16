/*******************************************************************************
 * Copyright (c) 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.addons.dropdown;

import static org.eclipse.rap.rwt.lifecycle.WidgetUtil.getId;

import org.eclipse.rap.addons.dropdown.internal.Model;
import org.eclipse.rap.clientscripting.ClientListener;
import org.eclipse.rap.json.JsonArray;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;


public class AutoSuggest {

  private DropDown dropDown;
  private Model model;
  private ClientListener clientListener;
  private Text text;
  private boolean isDisposed;

  public AutoSuggest( Text text ) {
    if( text == null ) {
      throw new NullPointerException( "Text must not be null" );
    }
    if( text.isDisposed() ) {
      throw new IllegalArgumentException( "Text mus not be disposed" );
    }
    this.text = text;
    dropDown = new DropDown( text );
    model = new Model();
    model.set( "textWidgetId", getId( text ) );
    model.set( "dropDownWidgetId", getId( dropDown ) );
    text.addListener( SWT.Dispose, new Listener() {
      public void handleEvent( Event event ) {
        dispose();
      }
    } );
    attachClientListeners( );
  }

  public void setData( String[] data ) {
    if( data == null ) {
      throw new NullPointerException( "Data must not be null" );
    }
    model.set( "elements", createArray( data ) );
  }

  public void setVisibleItemCount( int itemCount ) {
    dropDown.setVisibleItemCount( itemCount );
  }

  public int getVisibleItemCount() {
    return dropDown.getVisibleItemCount();
  }

  public void dispose() {
    isDisposed = true;
    dropDown.dispose();
    model.dispose();
    text.removeListener( SWT.Verify, clientListener );
    text.removeListener( SWT.Modify, clientListener );
  }

  public boolean isDisposed() {
    return isDisposed;
  }

  DropDown getDropDown() {
    return dropDown;
  }

  private void attachClientListeners() {
    clientListener = new ClientListener( "" );
    text.addListener( SWT.Modify, clientListener );
    text.addListener( SWT.Verify, clientListener );
    dropDown.addListener( SWT.Show, clientListener );
    dropDown.addListener( SWT.Hide, clientListener );
    dropDown.addListener( SWT.Selection, clientListener );
    dropDown.addListener( SWT.DefaultSelection, clientListener );
  }

  private static JsonArray createArray( String[] data ) {
    JsonArray array = new JsonArray();
    for( int i = 0; i < data.length; i++ ) {
      array.add( data[ i ] );
    }
    return array;
  }

}
