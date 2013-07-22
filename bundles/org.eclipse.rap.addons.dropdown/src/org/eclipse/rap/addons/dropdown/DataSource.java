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
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.remote.Connection;
import org.eclipse.rap.rwt.remote.RemoteObject;


public class DataSource {

  private static final String REMOTE_TYPE = "rwt.remote.Model";

  private final RemoteObject remoteObject;
  private DataProvider dataProvider;

  public DataSource() {
    Connection connection = RWT.getUISession().getConnection();
    remoteObject = connection.createRemoteObject( REMOTE_TYPE );
  }

  public String getId() {
    return remoteObject.getId();
  }

  public void setDataProvider( DataProvider dataProvider ) {
    if( dataProvider == null ) {
      throw new NullPointerException( "Parameter must not be null: dataProvider" );
    }
    this.dataProvider = dataProvider;
    setInitialData();
  }

  private void setInitialData() {
    JsonArray array = new JsonArray();
    for( Object element : dataProvider.getSuggestions() ) {
      array.add( dataProvider.getValue( element ) );
    }
    remoteObject.set( "data", array );
  }

}
