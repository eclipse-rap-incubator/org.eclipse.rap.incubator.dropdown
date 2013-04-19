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

var VIEWER_KEY = "org.eclipse.rap.addons.dropdown.DropDownViewer#viewer";
var searchItems = rwt.dropdown.DropDown.searchItems;
var createQuery = rwt.dropdown.DropDown.createQuery;

function handleEvent( event ) {
  switch( event.type ) {
    case SWT.Selection:
      handleSelection( event );
    break;
    case SWT.DefaultSelection:
      handleDefaultSelection( event );
    break;
    case SWT.Show:
      handleShow( event );
    break;
  }
}

function handleSelection( event ) {
  if( event.item != null ) {
    var dropdown = event.widget;
    var viewer = rap.getObject( dropdown.getData( VIEWER_KEY ) );
    var text = rap.getObject( viewer.get( "text" ) );
    text.setData( "selecting", true );
    text.setText( event.item );
    text.setSelection( [ 0, event.item.length ] );
  }
}

function handleDefaultSelection( event ) {
  var dropdown = event.widget;
  dropdown.hide();
  var selectionIndex = dropdown.getSelectionIndex();
  var mapping = dropdown.getData( "indexMapping" );
  var elementIndex = mapping[ selectionIndex ];
  var viewer = rap.getObject( dropdown.getData( VIEWER_KEY ) );
  viewer.notify( "SelectionChanged", { "index" : elementIndex } );
  dropdown.setSelectionIndex( -1 ); // should this happen automatically?
//  var text = rap.getObject( dropdown.getData( "text" ) );
//  text.forceFocus(); // TODO : currently not possible
}

function handleShow( event ) {
  var dropdown = event.widget;
  var viewer = rap.getObject( dropdown.getData( VIEWER_KEY ) );
  var data = viewer.get( "elements" );
  var text = rap.getObject( viewer.get( "text" ) );
  var str = text.getText();
  var result = searchItems( data, createQuery( str ) );
  dropdown.setItems( result.items );
  dropdown.setData( "indexMapping", result.indicies );
}
