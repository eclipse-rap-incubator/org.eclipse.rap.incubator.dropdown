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

var LINKED_CONTROL_KEY = "org.eclipse.rap.addons.dropdown.viewer.linkedControl";
var INPUT_KEY = "org.eclipse.rap.addons.dropdown.viewer.input";
var VIEWER_KEY = "org.eclipse.rap.addons.dropdown.viewer";

function handleEvent( event ) {
  switch( event.type ) {
    case SWT.Selection:
      handleSelection( event );
    break;
    case SWT.DefaultSelection:
      handleDefaultSelection( event );
    break;
//    case SWT.KeyDown:
//      handleKeyDown( event ); // not supported (yet?)
//    break;
  }
}

function handleSelection( event ) {
  if( event.element != null ) {
    var dropdown = event.widget;
    var text = rap.getObject( dropdown.getData( LINKED_CONTROL_KEY ) );
    text.setData( "selecting", true );
    text.setText( event.element );
    text.setSelection( [ 0, event.element.length ] );
  }
}

function handleDefaultSelection( event ) {
  var dropdown = event.widget;
  dropdown.hide();
  var selectionIndex = dropdown.getSelectionIndex();
  var mapping = dropdown.getData( "indexMapping" );
  var elementIndex = mapping[ selectionIndex ];
  var viewer = rap.getObject( dropdown.getData( VIEWER_KEY ) );
  viewer.notifySelectionChanged( elementIndex );
//  var text = rap.getObject( dropdown.getData( "text" ) );
//  text.forceFocus(); // TODO : currently not possible
}

function handleKeyDown( event ) {
  if( event.keyCode === SWT.ESC ) { // make default behavior? (Only effective when focused)
    event.widget.hide();
  }
}
