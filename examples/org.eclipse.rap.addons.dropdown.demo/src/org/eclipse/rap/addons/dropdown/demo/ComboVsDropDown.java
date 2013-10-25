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
package org.eclipse.rap.addons.dropdown.demo;

import org.eclipse.rap.addons.dropdown.DropDown;
import org.eclipse.rap.rwt.application.AbstractEntryPoint;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;


public class ComboVsDropDown extends AbstractEntryPoint {

  @Override
  protected void createContents( Composite parent ) {
    createCombo( parent );
    createTextDropDown( parent );
  }

  public void createTextDropDown( Composite parent ) {
    final Text text = new Text( parent, SWT.BORDER );
    text.setEditable( false );
    text.setCursor( text.getDisplay().getSystemCursor( SWT.CURSOR_ARROW ) );
    final DropDown dropDown = new DropDown( text );
    dropDown.setItems( getItems() );
    dropDown.addListener( SWT.Selection, new Listener() {
      public void handleEvent( Event event ) {
        text.setText( event.text );
        text.setText( event.text );
        dropDown.hide();
      }
    } );
    text.addListener( SWT.MouseDown, new Listener() {
      public void handleEvent( Event event ) {
        dropDown.show();
      }
    } );

  }

  public void createCombo( Composite parent ) {
    Combo combo = new Combo( parent, SWT.READ_ONLY );
    combo.setItems( getItems() );
  }

  private static String[] getItems() {
    String[] result = new String[ 1000 ];
    for( int i = 0; i < result.length; i++ ) {
      result[ i ] = "Item " + i;
    }
    return result;
  }
}
