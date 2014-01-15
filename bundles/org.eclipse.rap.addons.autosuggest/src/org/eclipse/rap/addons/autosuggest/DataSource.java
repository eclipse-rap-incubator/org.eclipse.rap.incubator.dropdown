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

/**
 * Instances of this class represents a set of suggestions that can be used by an
 * {@link AutoSuggest} instance.
 *
 * <p>
 *   A single instance can be used by multiple <code>AutoSuggest</code> instances simultaneously.
 *   Each new DataSource is linked to the lifecycle of the UISession,
 *   therefore no duplicates should be created.
 * </p>
 *
 * <p>
 *   In addition to the raw suggestions data, the dataSource also controls the presentation and
 *   filter mechanism used by AutoSuggest.
 * </p>
 *
 * <p>
 *   Once an instance of <code>DataSource</code> has been set on an <code>AutoSuggest</code>,
 *   it should no longer be modified. Otherwise the changes may not be reflected on the
 *   <code>AutoSuggest</code> instances it is already attached to.
 * </p>
 */
public class DataSource {

  private static final String REMOTE_TYPE = "rwt.remote.Model";

  private final RemoteObject remoteObject;
  private DataProvider dataProvider;
  private ColumnTemplate template;

  /**
   * Constructs a new instance of this class. A {@link DataProvider} has to be set before it can be
   * used.
   *
   * @see DataSource#setDataProvider(DataProvider)
   **/
  public DataSource() {
    Connection connection = RWT.getUISession().getConnection();
    remoteObject = connection.createRemoteObject( REMOTE_TYPE );
  }

  /**
   * Sets the <code>DataProvider</code> to be used to collect the suggestions data. The
   * data is collected from <code>DataProvider</code> only once.
   *
   * The type of DataProvider set also determines which <code>Template</code> types can be used
   * with the same <code>DataSource</code> instance. (i.e. a {@link ColumnDataProvider} can be
   * used with a {@link ColumnTemplate}.) It also changes how the format of the suggestion
   * given to a filterScript.
   *
   * @param dataProvider the DataProvider instance (may not be null)
   *
   * @exception NullPointerException when dataProvider is null
   *
   * @see DataSource#setTemplate(ColumnTemplate)
   * @see DataSource#setFilterScript(String)
   */
  public void setDataProvider( DataProvider dataProvider ) {
    if( dataProvider == null ) {
      throw new NullPointerException( "Parameter must not be null: dataProvider" );
    }
    this.dataProvider = dataProvider;
    setInitialData();
  }

  /**
   * Sets a simple script (JavaScript function returning a boolean)
   * used to determine if a given suggestion matches a text typed by the user.
   *
   * <p>The Script has to be in the following format (example assumes suggestion is given as string):</p>
   * <pre>function( suggestion, userText ) {
   *  return suggestion.indexOf( userText ) !== -1;"
   *}</pre>
   * <p>
   *   The default script is not case-sensitive and can handle suggestions provided by
   *   {@link DataProvider} and {@link ColumnDataProvider} interfaces. In case of
   *   <code>ColumnDataProvider</code> only the first column is queried.
   * </p>
   *
   * @param script the filterScript (may be null)
   *
   * @see DataSource#setDataProvider(DataProvider)
   */
  public void setFilterScript( String script ) {
    remoteObject.set( "filterScript", script );
  }

  /**
   * Sets a template that determines how suggestions are presented in the dropDown list.
   *
   * <p>
   *   The template has to be able to process the format of suggestions provided by the type of
   *   dataProvider {@link DataSource#setDataProvider(DataProvider) attached} to the receiver.
   * </p>
   * <p>
   *   No template is required for the default {@link DataProvider}.
   * </p>
   *
   * @param template the template (may be null)
   *
   * @see DataSource#setDataProvider(DataProvider)
   */
  public void setTemplate( ColumnTemplate template ) {
    this.template = template;
  }

  String getId() {
    return remoteObject.getId();
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
