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

//TODO : This can get very slow with huge lists. Possible optimizations include caching results,
//       limiting result length (at least until first selection occurs), and virtual rendering
function handleModify( event ) {
  var widget = event.widget;
  var text = widget.getText().toLowerCase();
  var viewer = rap.getObject( widget.getData( VIEWER_KEY ) );
  var dropdown = rap.getObject( viewer.get( "dropDown" ) );
  var data = viewer.get( "elements" );
  var result = searchItems( data, createQuery( text ) );
  var typing = widget.getData( "typing" );
  var selecting = widget.getData( "selecting" );
  widget.setData( "typing", false );
  widget.setData( "selecting", false );
  if( !selecting ) {
    if( ( text.length >= 2 || ( dropdown.getVisibility() && typing ) ) && result.items.length > 0 ) {
      dropdown.setItems( result.items );
      dropdown.setData( "indexMapping", result.indicies );
      dropdown.show();
      var common = commonText( result.items );
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
  var viewer = rap.getObject( widget.getData( VIEWER_KEY ) );
  var dropdown = rap.getObject( viewer.get( "dropDown" ) );
  if( dropdown.getVisibility() ) {
    switch( event.keyCode ) {
      case SWT.ARROW_UP:
        var index = dropdown.getSelectionIndex() - 1;
        if( index >= 0 ) {
          dropdown.setSelectionIndex( index );
        }
        event.doit = false; // prevent the cursor from moving
      break;
      case SWT.ARROW_DOWN:
        var index = dropdown.getSelectionIndex() + 1;
        if( index < dropdown.getItemCount() ) {
          dropdown.setSelectionIndex( index );
        }
        event.doit = false;
      break;
      case SWT.CR:
        var sel = widget.getSelection();
        widget.setSelection( [ sel[ 1 ], sel[ 1 ] ] );
        if( dropdown.getItemCount() === 1 || dropdown.getSelectionIndex() >= 0 ) {
          dropdown.hide();
          // TODO [tb] : do this by triggering default Selection
          var selectionIndex = dropdown.getSelectionIndex();
          var mapping = dropdown.getData( "indexMapping" );
          var elementIndex = mapping[ selectionIndex >= 0 ? selectionIndex : 0 ];
          var viewer = rap.getObject( dropdown.getData( VIEWER_KEY ) );
          viewer.notify( "SelectionChanged", { "index" : elementIndex } );
          dropdown.setSelectionIndex( -1 ); // should this happen automatically?
        }
        event.doit = false;
      break;
      case SWT.ESC: // TODO [tb] : Dropdown itself can not implement this easily, it has no focus
        dropdown.hide();
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

