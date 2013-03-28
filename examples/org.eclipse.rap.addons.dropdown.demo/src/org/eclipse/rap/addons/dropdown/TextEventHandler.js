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
  var text = widget.getText().toLowerCase();
  var dropdown = rap.getObject( widget.getData( "dropdown" ) );
  var nations = rap.getObject( dropdown.getData( "nations" ) );
  var itemFilter = function( item ) { return item.toLowerCase().indexOf( text ) === 0; };
  var items = nations.filter( itemFilter );
  if( text.length >= 2 && items.length > 0 ) {
    dropdown.setItems( items );
    dropdown.show();
  } else {
    dropdown.hide();
  }
}