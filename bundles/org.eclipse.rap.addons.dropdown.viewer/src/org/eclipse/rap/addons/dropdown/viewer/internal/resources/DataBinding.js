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

///////////////////
// Event Delegation

 var VIEWER_KEY = "org.eclipse.rap.addons.dropdown.viewer.DropDownViewer#viewer";

function handleEvent() {
  if( arguments.length === 1 ) {
    var event = arguments[ 0 ];
    var model = rap.getObject( event.widget.getData( VIEWER_KEY ) );
    if( event.widget.classname === "rwt.dropdown.DropDown" ) {
      handleDropDownEvent( model, event );
    } else {
      handleTextEvent( model, event );
    }
  } else {
    handleModelEvent.apply( this, arguments );
  }
}

function handleDropDownEvent( model, event ) {
  switch( event.type ) {
    case SWT.Selection:
      onDropDownSelection( model, event.widget.getSelectionIndex() );
    break;
    case SWT.DefaultSelection:
      onDropDownDefaultSelection( model );
    break;
  }
}

function handleTextEvent( model, event ) {
  var userAction = getUserAction( event );
  switch( event.type ) {
    case SWT.Modify:
      onTextModify( model, event.widget.getText(), userAction );
    break;
  }
  setUserAction( event );
}

function setUserAction( event ) {
  if( event.type === SWT.Verify ) {
    // See Bug 404896 - [ClientScripting] Verify event keyCode is always zero when replacing txt
    var action = ( event.text !== "" /* && event.keyCode !== 0 */ ) ? "typing" : "deleting";
    event.widget.setData( "userAction", action );
  }
}

function getUserAction( event ) {
  var action = event.widget.getData( "userAction" );
  event.widget.setData( "userAction", null );
  return action;
}

function handleModelEvent( model, type, event ) {
  var textWidget = rap.getObject( model.get( "textWidgetId" ) );
  var dropDown = rap.getObject( model.get( "dropDownWidgetId" ) );
  switch( event.property ) {
    case "text":
      onModelChangeText( textWidget, event.value );
    break;
    case "results":
      onModelChangeResults( dropDown, event.value );
    break;
  }
}

/////////////////
// Event Handling

function onTextModify( model, text, userAction ) {
  if( userAction ) {
    model.set( "userText", text );
  }
}

function onDropDownSelection( model, selectionIndex ) {
  model.set( "resultSelection", selectionIndex );
}

function onDropDownDefaultSelection( model ) {
  model.notify( "accept" );
}

function onModelChangeResults( dropDown, results ) {
  dropDown.show(); //temporary hack
  dropDown.setItems( results.items );
}

function onModelChangeText( textWidget, text ) {
  textWidget.setText( text );
}

