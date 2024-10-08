/*******************************************************************************
 * Copyright (c) 2013, 2014 EclipseSource.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

//@ sourceURL=EventDelegator.js

///////////////////
// Event Delegation

var MODEL_KEY = "org.eclipse.rap.addons.autosuggest#Model";

function handleEvent( event ) {
  withObject( event.widget.getData( MODEL_KEY ), function( model ) {
    var autoSuggestListener
      = rwt.remote.ObjectRegistry.getObject( model.get( "autoSuggestListenerId" ) );
    autoSuggestListener( event );
  } );
}

function withObject( id, callback ) {
  // compensating for the server creating the objects in the wrong order:
  if( rap.getObject( id ) ) {
    callback( rap.getObject( id ) );
  } else {
    var wrapper = function() {
      callback( rap.getObject( id ) );
      rap.off( "render", wrapper );
    };
    rap.on( "render", wrapper );
  }
}
