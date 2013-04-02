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
  switch( event.type ) {
    case SWT.Modify:
      handleModify( event );
    break;
    case SWT.Verify:
      handleVerify( event );
    break;
  }
}

function handleVerify( event ) {
  if( event.text === "" ) {
    // the modify event can apparently not be used to determine if text was inserted or deleted
    event.widget.setData( "del", true );
  }
}


function handleModify( event ) {
  var widget = event.widget;
  var text = widget.getText().toLowerCase();
  var dropdown = rap.getObject( widget.getData( "dropdown" ) );
  var nations = rap.getObject( dropdown.getData( "nations" ) );
  var items = itemsStartingWith( nations, text );
  if( text.length >= 2 && items.length > 0 ) {
    dropdown.setItems( items );
    dropdown.show();
    var common = commonText( items );
    if( !widget.getData( "del" ) && common && common.length > text.length ) { // a better way to protects against recursive events?
      var sel = widget.getSelection();
      var newSel = [ sel[ 0 ], common.length ];
      widget.setText( common );
      // See Bug 404615 - [ClientScripting][Text] setSelection and getSelection do not work correctly in Modify event
//      window.setTimeout( function() {
//        widget.setSelection( newSel );
//      }, 0 );
    }
  } else {
    dropdown.hide();
  }
  widget.setData( "del", false );
}

function commonText( items ) {
  var result = null;
  if( items.length === 1 ) {
    result = items[ 0 ];
  } else {
    var cont = true;
    var commonTo = 0;
    while( cont ) {
      var next = items[ 0 ].indexOf( " ", commonTo + 1 );
      if( next !== -1 ) {
        var commons = itemsStartingWith( items, items[ 0 ].slice( 0, next ), true );
        if( items.length === commons.length ) {
          commonTo = next;
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

function itemsStartingWith( items, text, caseSensitive ) {
  var itemFilter;
  if( caseSensitive ) {
    itemFilter = function( item ) { return item.indexOf( text ) === 0; };
  } else {
    itemFilter = function( item ) { return item.toLowerCase().indexOf( text ) === 0; };
  }
  return items.filter( itemFilter );
}
