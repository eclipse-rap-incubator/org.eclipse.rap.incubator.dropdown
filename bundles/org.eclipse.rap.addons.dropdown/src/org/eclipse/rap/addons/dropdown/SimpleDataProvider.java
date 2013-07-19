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

import org.eclipse.rap.json.JsonArray;


public class SimpleDataProvider extends AbstractDataProvider {

  public SimpleDataProvider( String[] data ) {
    setData( createJsonArray( data ) );
  }

  private static JsonArray createJsonArray( String[] data ) {
    JsonArray array = new JsonArray();
    for( int i = 0; i < data.length; i++ ) {
      array.add( data[ i ] );
    }
    return array;
  }

}
