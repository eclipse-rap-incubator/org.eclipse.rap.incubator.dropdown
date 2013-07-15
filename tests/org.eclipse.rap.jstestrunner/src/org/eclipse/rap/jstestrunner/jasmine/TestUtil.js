/*******************************************************************************
 * Copyright (c) 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

(function(){

  var resources = {};

  var xhrGet = function( url ) {
    var xhr = new XMLHttpRequest();
    xhr.open( "GET", url, false );
    xhr.send();
    return xhr.responseText;
  };

  TestUtil = {

    loadResourceFromClassLoader : function( name, loader, path ) {
      throw new Error( "Not supported in browser environment" );
    },

    loadResourceFromURL : function( name, url ) {
      resources[ name ] = xhrGet( url );
    },

    getResource : function( name ) {
      return resources[ name ];
    }

  };

}());
