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

function handleEvent( event ) {
  var widget = event.widget;
  var dropdown = rap.getObject( widget.getData( "dropdown" ) );
  dropdown.setVisibility( !dropdown.getVisibility() );
  if( dropdown.getVisibility() ) {
    dropdown.setItems( rap.getObject( dropdown.getData( "data" ) ) );
  }
}

//}
//
//  var data = rap.getObject( dropdown.getData( "data" ) );
//  var items = itemsStartingWith( data, text );
//  if( !widget.getData( "selecting" ) ) {
//    if( text.length >= 2 && items.length > 0 ) {
//      dropdown.setItems( items );
//      dropdown.show();
//      var common = commonText( items );
//      if( widget.getData( "typing" ) && common ) {
//        var sel = widget.getSelection();
//        var newSel = [ sel[ 0 ], common.length ];
//        widget.setText( common );
//        // See Bug 404615 - [ClientScripting][Text] setSelection and getSelection do not work correctly in Modify event
//  //      window.setTimeout( function() {
//  //        widget.setSelection( newSel );
//  //      }, 0 );
//      }
//    } else {
//      dropdown.hide();
//    }
//  }
//  widget.setData( "typing", false );
//  widget.setData( "selecting", false );
//}
//
//function handleKeyDown( event ) {
//  var widget = event.widget;
//  var dropdown = rap.getObject( widget.getData( "dropdown" ) );
//  if( dropdown.getVisibility() ) {
//    switch( event.keyCode ) {
//      case SWT.ARROW_UP:
//        var index = dropdown.getSelectionIndex() - 1;
//        if( index >= 0 ) {
//          dropdown.setSelectionIndex( index );
//        }
//        event.doit = false; // prevent the cursor from moving
//      break;
//      case SWT.ARROW_DOWN:
//        var index = dropdown.getSelectionIndex() + 1;
//        if( index < dropdown.getItemCount() ) {
//          dropdown.setSelectionIndex( index );
//        }
//        event.doit = false;
//      break;
//      case SWT.CR:
//      case SWT.ESC: // TODO [tb] : Dropdown itself can not implement this easily, it has no focus
//        dropdown.hide();
//      break;
//    }
//  }
//}
//
//function commonText( items ) {
//  var result = null;
//  if( items.length === 1 ) {
//    result = items[ 0 ];
//  } else {
//    var cont = true;
//    var commonTo = 0;
//    while( cont ) {
//      var next = items[ 0 ].indexOf( " ", commonTo + 1 );
//      if( next !== -1 ) {
//        var commons = itemsStartingWith( items, items[ 0 ].slice( 0, next ), true );
//        if( items.length === commons.length ) {
//          commonTo = next;
//        } else {
//          cont = false;
//        }
//      } else {
//        cont = false;
//      }
//    }
//    if( commonTo > 0 ) {
//      result = items[ 0 ].slice( 0, commonTo );
//    }
//  }
//  return result;
//}
//
//function itemsStartingWith( items, text, caseSensitive ) {
//  var itemFilter;
//  if( caseSensitive ) {
//    itemFilter = function( item ) { return item.indexOf( text ) === 0; };
//  } else {
//    itemFilter = function( item ) { return item.toLowerCase().indexOf( text ) === 0; };
//  }
//  return items.filter( itemFilter );
//}
