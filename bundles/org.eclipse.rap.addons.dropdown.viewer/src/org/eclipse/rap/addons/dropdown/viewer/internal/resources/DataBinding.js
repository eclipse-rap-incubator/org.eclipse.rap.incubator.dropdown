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
      onDropDownSelection( model, event.widget.getSelectionIndex(), event );
    break;
  }
}

function handleTextEvent( model, event ) {
  switch( event.type ) {
    case SWT.Modify:
      onTextModify( model, event.widget.getText(), event );
    break;
  }
}

function handleModelEvent( model, type, event ) {
  var textWidget = rap.getObject( model.get( "textWidgetId" ) );
  var dropDown = rap.getObject( model.get( "dropDownWidgetId" ) );
  switch( type ) {
    case "change:text":
      onModelChangeText( textWidget, model.get( "text" ), event );
    break;
    case "change:results":
      onModelChangeResults( dropDown, model.get( "results" ), event );
    break;
  }
}

/////////////////
// Event Handling

function onTextModify( model, text, event ) {
  model.set( "text", text );
}

function onDropDownSelection( model, selectionIndex, event ) {
  model.set( "resultSelection", selectionIndex );
}

function onModelChangeResults( dropDown, results, event ) {
  dropDown.show(); //temporary hack
  dropDown.setItems( results.items );
}

function onModelChangeText( textWidget, text, event ) {
  textWidget.setText( text );
}

