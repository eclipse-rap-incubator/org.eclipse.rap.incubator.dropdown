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

import org.eclipse.swt.widgets.Text;


public class AutoSuggest {

  private DropDown dropDown;

  public AutoSuggest( Text text ) {
    if( text == null ) {
      throw new NullPointerException( "Text must not be null" );
    }
    if( text.isDisposed() ) {
      throw new IllegalArgumentException( "Text mus not be disposed" );
    }
    dropDown = new DropDown( text );
  }

  public void setData( String[] data ) {
    if( data == null ) {
      throw new NullPointerException( "Data must not be null" );
    }
  }

  public void setVisibleItemCount( int itemCount ) {
    dropDown.setVisibleItemCount( itemCount );
  }

  public int getVisibleItemCount() {
    return dropDown.getVisibleItemCount();
  }

  public void dispose() {
    dropDown.dispose();
  }

  DropDown getDropDown() {
    return dropDown;
  }

}
