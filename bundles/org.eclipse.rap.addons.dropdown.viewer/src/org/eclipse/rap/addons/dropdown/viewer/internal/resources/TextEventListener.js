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

 //@ sourceURL=TextEventListener.js

var VIEWER_KEY = "org.eclipse.rap.addons.dropdown.viewer.DropDownViewer#viewer";
var searchItems = rwt.dropdown.DropDown.searchItems;
var createQuery = rwt.dropdown.DropDown.createQuery;

function handleEvent( event ) {
  switch( event.type ) {
    case SWT.Modify:
      handleModify( event );
    break;
    case SWT.Verify:
      handleVerify( event );
    break;
    case SWT.KeyDown:
      handleKeyDown( event );
    break;
    case SWT.MouseDown:
      handleMouseDown( event );
    break;
  }
}

function handleVerify( event ) {
  // See Bug 404896 - [ClientScripting] Verify event keyCode is always zero when replacing txt
  if( event.text !== "" /* && event.keyCode !== 0 */ ) {
    event.widget.setData( "typing", true );
  } else {
    event.widget.setData( "deleting", true );
  }
}

function handleModify( event ) {
  var widget = event.widget;
  var text = widget.getText().toLowerCase();
  var viewer = rap.getObject( widget.getData( VIEWER_KEY ) );
  var dropdown = rap.getObject( viewer.get( "dropDown" ) );
  var data = viewer.get( "elements" );
  var result = searchItems( data, createQuery( text ) );
  if( result.items.length === 0 ) {
    result = searchItems( data, /.*/ );
    if( text.length >= 1 ) {
      showError( viewer, true )
    } else {
      showError( viewer, false )
    }
  } else {
    showError( viewer, false )
  }
  if( !widget.getData( "selecting" ) ) {
    if( widget.getData( "typing" ) || widget.getData( "deleting" ) ) {
      dropdown.setData( "userText", widget.getText() );
    }
    if( result.items.length > 0 ) {
      dropdown.setData( "indexMapping", result.indicies );
      dropdown.setItems( result.items );
      dropdown.show();
      var sel = widget.getSelection();
      var common = commonText( result.items );
      if( widget.getData( "typing" ) && result.items.length === 1 ) {
        dropdown.setSelectionIndex( 0 );
      }
      if( widget.getData( "typing" ) && common && common.length > text.length ) {
        var newSel = [ sel[ 0 ], common.length ];
        widget.setText( common );
        widget.setSelection( newSel );
      }
    } else {
      dropdown.hide();
    }
    if( widget.getData( "typing" ) || widget.getData( "deleting" ) ) {
      widget.setData( "typing", false );
      widget.setData( "deleting", false );
    }
  }
}

function handleKeyDown( event ) {
  var widget = event.widget;
  var viewer = rap.getObject( widget.getData( VIEWER_KEY ) );
  var dropdown = rap.getObject( viewer.get( "dropDown" ) );
  switch( event.keyCode ) {
    case SWT.CR:
      var sel = widget.getSelection();
      widget.setSelection( [ sel[ 1 ], sel[ 1 ] ] );
    break;
  }
}

function handleMouseDown( event ) {
  var widget = event.widget;
  var viewer = rap.getObject( widget.getData( VIEWER_KEY ) );
  var dropdown = rap.getObject( viewer.get( "dropDown" ) );
  dropdown.setVisible( !dropdown.getVisible() );
}

function commonText( items ) {
  var result = null;
  if( items.length === 1 ) {
    result = items[ 0 ];
  } else {
    var cont = true;
    var commonTo = 0;
    while( cont ) {
      var next = items[ 0 ].indexOf( " ", commonTo + 1 ); // TODO [tb] : also respect ,"':;-!?
      if( next !== -1 ) {
        var testString = items[ 0 ].slice( 0, next + 1 );
        var commons = searchItems( items, createQuery( testString, true ) ).items;
        if( items.length === commons.length ) {
          commonTo = next + 1;
        } else {
          cont = false;
        }
      } else {
        cont = false;
      }
    }
    if( commonTo > 0 ) {
      result = items[ 0 ].slice( 0, commonTo );
    }
  }
  return result;
}

function showError( viewer, value ) {
  var text = rap.getObject( viewer.get( "text" ) );
  var decorator = rap.getObject( viewer.get( "decorator" ) );
  text.setBackground( value ? [ 255, 255, 128 ] : null );
  decorator.setVisible( value );
}
