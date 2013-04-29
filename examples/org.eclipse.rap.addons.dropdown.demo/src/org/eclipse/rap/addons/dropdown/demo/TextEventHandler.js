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
  }
}

function handleVerify( event ) {
  // See Bug 404896 - [ClientScripting] Verify event keyCode is always zero when replacing txt
  if( event.text !== "" /* && event.keyCode !== 0 */ ) {
    event.widget.setData( "typing", true );
  }
}

function handleModify( event ) {
  var widget = event.widget;
  var text = widget.getText().toLowerCase();
  var dropdown = rap.getObject( widget.getData( "dropdown" ) );
  var data = rap.getObject( dropdown.getData( "data" ) );
  var items = searchItems( data, createQuery( text ), 10 ).items;
  var typing = widget.getData( "typing" );
  var selecting = widget.getData( "selecting" );
  widget.setData( "typing", false );
  widget.setData( "selecting", false );
  if( !selecting ) {
    if( ( text.length >= 2 || ( dropdown.getVisibility() && typing ) ) && items.length > 0 ) {
      dropdown.setItems( items );
      dropdown.show();
      var common = commonText( items );
      if( typing && common && common.length > text.length ) {
        var sel = widget.getSelection();
        var newSel = [ sel[ 0 ], common.length ];
        widget.setText( common );
        widget.setSelection( newSel );
      }
    } else {
      dropdown.hide();
    }
  }
}

function handleKeyDown( event ) {
  var widget = event.widget;
  var dropdown = rap.getObject( widget.getData( "dropdown" ) );
  if( dropdown.getVisibility() ) {
    switch( event.keyCode ) {
      case SWT.CR:
        var sel = widget.getSelection();
        widget.setSelection( [ sel[ 1 ], sel[ 1 ] ] );
      break;
    }
  }
}

function commonText( items ) {
  var result = null;
  if( items.length === 1 ) {
    result = items[ 0 ];
  } else {
    var cont = true;
    var commonTo = 0;
    while( cont ) {
      var next = items[ 0 ].indexOf( " ", commonTo + 1 ); // TODO [tb] : also respect ,"':;
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