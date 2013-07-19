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


//@ sourceURL=DropDownEventListener.js

var VIEWER_KEY = "org.eclipse.rap.addons.dropdown.viewer.DropDownViewer#viewer";
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
    case SWT.Hide:
      handleHide( event );
    break;
  }
}

function handleShow( event ) {
  var dropdown = event.widget;
  var viewer = rap.getObject( dropdown.getData( VIEWER_KEY ) );
  var text = rap.getObject( viewer.get( "text" ) );
  if( !text.getData( "typing" ) ) {
    var data = viewer.get( "elements" );
    var str = text.getText();
    var result = searchItems( data, createQuery( str ) );
    if( result.items.length === 0 || ( result.items.length === 1 && str === result.items[ 0 ] ) ) {
      result = searchItems( data, /.*/ );
    }
    dropdown.setItems( result.items );
    dropdown.setData( "indexMapping", result.indicies );
  }
}

function handleHide( event ) {
  if( event.widget.getSelectionIndex() !== -1 ) {
    handleDefaultSelection( event );
  }
}

function handleSelection( event ) {
  var dropdown = event.widget;
  var viewer = rap.getObject( dropdown.getData( VIEWER_KEY ) );
  var text = rap.getObject( viewer.get( "text" ) );
  if( !text.getData( "selecting" ) ) {
    text.setData( "selecting", true );
    if( event.text.length > 0 ) {
      text.setText( event.text );
      text.setSelection( [ 0, event.text.length ] );
      if( event.widget.getItemCount() === 1 ) {
        handleDefaultSelection( event );
        dropdown.hide();
      }
    } else if( !text.getData( "typing" ) && !text.getData( "deleting" ) ) {
      var userText = dropdown.getData( "userText" );
      if( userText !== null && userText !== undefined ) {
        text.setText( userText );
        text.setSelection( [ userText.length, userText.length ] );
      }
    }
    text.setData( "selecting", false );
  }
}

function handleDefaultSelection( event ) {
  var dropdown = event.widget;
  var selectionIndex = dropdown.getSelectionIndex();
  var mapping = dropdown.getData( "indexMapping" );
  var elementIndex = mapping[ selectionIndex ];
  var viewer = rap.getObject( dropdown.getData( VIEWER_KEY ) );
  var text = rap.getObject( viewer.get( "text" ) );
  dropdown.setData( "userText", text.getText() );
  if( viewer.get( "selection" ) !== elementIndex ) {
    viewer.notify( "SelectionChanged", { "index" : elementIndex } );
    viewer.set( "selection", elementIndex );
  }
  text.setData( "selecting", true );
  dropdown.setSelectionIndex( -1 ); // should this happen automatically?
  text.setData( "selecting", false );
}
