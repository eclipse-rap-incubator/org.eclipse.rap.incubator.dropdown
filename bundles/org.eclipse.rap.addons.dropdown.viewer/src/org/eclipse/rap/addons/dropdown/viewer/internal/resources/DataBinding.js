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
    handleWidgetEvent.apply( this, arguments );
  } else {
    handleModelEvent.apply( this, arguments );
  }
}

function handleWidgetEvent( event ) {
  var model = rap.getObject( event.widget.getData( VIEWER_KEY ) );
  if( event.widget.classname === "rwt.dropdown.DropDown" ) {
    var dropDown = rap.getObject( model.get( "textWidgetId" ) );
    switch( event.type ) {
      case SWT.Selection:
        //onDropDownSelection( model, event.widget, dropDown, event );
      break;
    }
  } else {
    var textWidget = rap.getObject( model.get( "textWidgetId" ) );
    switch( event.type ) {
      case SWT.Modify:
        onTextModify( model, textWidget, event.widget, event );
      break;
    }
  }
}

function handleModelEvent( model, type, event ) {
  var textWidget = rap.getObject( model.get( "textWidgetId" ) );
  var dropDown = rap.getObject( model.get( "dropDownWidgetId" ) );
  switch( type ) {
    case "change:text":
      //onModelChangeText( model, textWidget, dropDown, event );
    break;
    case "change:results":
      onModelChangeResults( model, dropDown, event );
    break;
  }
}

/////////////////
// Event Handling

function onTextModify( model, textWidget, event ) {
  model.set( "text", textWidget.getText() );
}

function onModelChangeResults( model, dropDown, event ) {
  //temporary hack:
  dropDown.show();
  dropDown.setItems( model.get( "results" ).items );
}
