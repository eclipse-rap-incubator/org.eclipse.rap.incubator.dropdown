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

 //@ sourceURL=DataBinding.js

///////////////////
// Event Delegation

 var VIEWER_KEY = "org.eclipse.rap.addons.dropdown.viewer.DropDownViewer#viewer";

function handleEvent( event ) {
  if( event.widget  ) {
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
      onDropDownSelection( model, event );
    break;
    case SWT.DefaultSelection:
      onDropDownDefaultSelection( model, event );
    break;
  }
}

function handleTextEvent( model, event ) {
  var userAction = getUserAction( event );
  switch( event.type ) {
    case SWT.Modify:
      onTextModify( model, event, userAction );
    break;
  }
  setUserAction( event );
}

function handleModelEvent( event ) {
  var model = event.source;
  var textWidget = rap.getObject( model.get( "textWidgetId" ) );
  var dropDown = rap.getObject( model.get( "dropDownWidgetId" ) );
  switch( event.property ) {
    case "text":
      onModelChangeText( textWidget, model, event );
    break;
    case "textSelection":
      onModelChangeTextSelection( textWidget, model, event );
    break;
    case "results":
      onModelChangeResults( dropDown, model, event );
    break;
  }
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

/////////////////
// Event Handling

function onTextModify( model, event, userAction ) {
  var text = event.widget.getText();
  model.set( "text", text, { "source" : "Text" } );
  if( userAction ) {
    model.set( "userText", text, { "userAction" : userAction } );
  }
}

function onDropDownSelection( model, event ) {
  model.set( "resultSelection", event.index, { "source" : "DropDown" } );
}

function onDropDownDefaultSelection( model, event ) {
  model.notify( "accept", { type : "accept", "source" : model }  );
}

function onModelChangeResults( dropDown, model, event ) {
  dropDown.show(); //temporary hack
  var results = model.get( "results" );
  dropDown.setItems( results.items );
}

function onModelChangeText( textWidget, model, event ) {
  if( event.source !== "Text" ) {
    textWidget.setText( model.get( "text" ) );
  }
}

function onModelChangeTextSelection( textWidget, model, event ) {
  textWidget.setSelection( event.value );
}

