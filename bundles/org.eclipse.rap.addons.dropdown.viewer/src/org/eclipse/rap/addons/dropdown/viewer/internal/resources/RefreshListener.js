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

//@ sourceURL=RefreshLIstener.js

var searchItems = rwt.dropdown.DropDown.searchItems;
var createQuery = rwt.dropdown.DropDown.createQuery;

function handleEvent( viewer ) {
  var dropdown = rap.getObject( viewer.get( "dropDown" ) );
  var data = viewer.get( "elements" );
  try {
    var text = rap.getObject( viewer.get( "text" ) );
  } catch( ex ) { // widget may already be disposed
    return;
  }
  var str = text.getText();
  var result = searchItems( data, createQuery( str ) );
  if( result.items.length === 0 || ( result.items.length === 1 && str === result.items[ 0 ] ) ) {
    result = searchItems( data, /.*/ );
  }
  dropdown.setData( "refreshing", true );
  dropdown.setItems( result.items );
  dropdown.setData( "refreshing", false );
  dropdown.setData( "indexMapping", result.indicies );
  text.setText( "" );
  text.setText( str );
}
