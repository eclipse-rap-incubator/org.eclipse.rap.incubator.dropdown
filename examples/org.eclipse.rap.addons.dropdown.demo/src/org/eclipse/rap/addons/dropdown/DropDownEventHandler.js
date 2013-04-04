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
  var dropdown = event.widget;
  var text = rap.getObject( dropdown.getData( "text" ) );
  text.setData( "selecting", true );
  text.setText( event.element );
}

function handleDefaultSelection( event ) {
  event.widget.hide();
}

function handleKeyDown( event ) {
  if( event.keyCode === SWT.ESC ) { // make default behavior? (Only effective when focused)
    event.widget.hide();
  }
}
