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
package org.eclipse.rap.addons.autosuggest;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.remote.Connection;
import org.eclipse.rap.rwt.remote.RemoteObject;


public class DataSource {

  private static final String REMOTE_TYPE = "rwt.remote.Model";

  private final RemoteObject remoteObject;
  private DataProvider dataProvider;
  private ColumnTemplate template;

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

  public void setFilterScript( String script ) {
    remoteObject.set( "filterScript", script );
  }

  public void setTemplate( ColumnTemplate template ) {
    this.template = template;
  }

  ColumnTemplate getTemplate() {
    return template;
  }

  private void setInitialData() {
    boolean hasColumns = dataProvider instanceof ColumnDataProvider;
    remoteObject.set( "data", hasColumns ? getColumnData() : getStringData() );
  }

  private JsonArray getStringData() {
    JsonArray array = new JsonArray();
    for( Object element : dataProvider.getSuggestions() ) {
      array.add( dataProvider.getValue( element ) );
    }
    return array;
  }

  private JsonArray getColumnData() {
    JsonArray array = new JsonArray();
    ColumnDataProvider columnDataProvider = ( ColumnDataProvider )dataProvider;
    for( Object element : dataProvider.getSuggestions() ) {
      JsonArray row = new JsonArray().add( dataProvider.getValue( element ) );
      String[] texts = columnDataProvider.getTexts( element );
      for( String text : texts ) {
        row.add( text );
      }
      array.add( row );
    }
    return array;
  }

}
