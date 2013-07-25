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

var MODEL_KEY = "org.eclipse.rap.addons.autosuggest#Model";

function handleEvent( event ) {
  if( event.widget ) {
    var model = rap.getObject( event.widget.getData( MODEL_KEY ) );
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
    case SWT.Show:
    case SWT.Hide:
      onDropDownChangeVisible( model, event );
    break;
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
    case "suggestionTexts":
      onModelChangeSuggestionTexts( dropDown, model, event );
    break;
    case "suggestionsVisible":
      onModelChangeSuggestionsVisible( dropDown, model, event );
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
  model.set( "text", text, { "action" : "sync" } );
  if( userAction ) {
    model.set( "userText", text, { "action" : userAction } );
  }
}

function onDropDownChangeVisible( model, event ) {
  model.set( "suggestionsVisible", event.widget.getVisible(), { "action" : "sync" }  );
}

function onDropDownSelection( model, event ) {
  model.set( "selectedSuggestionIndex", event.index, { "action" : "sync" }  );
}

function onDropDownDefaultSelection( model, event ) {
  model.notify( "accept", { type : "accept", "source" : model }  );
}

function onModelChangeSuggestionTexts( dropDown, model, event ) {
  dropDown.setItems( model.get( "suggestionTexts" ) );
}

function onModelChangeSuggestionsVisible( dropDown, model, event ) {
  if( event.options.action !== "sync" ) {
    dropDown.setVisible( event.value );
  }
}

function onModelChangeText( textWidget, model, event ) {
  if( event.options.action !== "sync" ) {
    textWidget.setText( event.value );
  }
}

function onModelChangeTextSelection( textWidget, model, event ) {
  textWidget.setSelection( event.value );
}
