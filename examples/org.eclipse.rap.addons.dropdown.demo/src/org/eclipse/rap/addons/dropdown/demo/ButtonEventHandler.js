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
  // TODO : should not autoamtically hide if programmatically shown
  dropdown.setVisibility( !dropdown.getVisibility() );
  if( dropdown.getVisibility() ) {
    // TODO : set focus to text or list, currently neither is possible
    dropdown.setItems( rap.getObject( dropdown.getData( "data" ) ) );
  }
}
