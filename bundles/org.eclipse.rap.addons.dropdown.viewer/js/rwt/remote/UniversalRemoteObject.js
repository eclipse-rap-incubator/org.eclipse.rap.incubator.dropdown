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

 (function() {

  rap.registerTypeHandler( "rwt.remote.UniversalRemoteObject", {

    factory : function() {
      return new UniversalRemoteObject();
    },

    isGeneric : true,

    destructor : "destroy"

  } );

  var UniversalRemoteObject = function() {
    this._ = {
      properties : {}
    };
  };

  UniversalRemoteObject.prototype = {

    set : function() {
      if( arguments[ 0 ] instanceof Object ) {
        var properties = arguments[ 0 ];
        var options = arguments[ 1 ];
        for( var key in properties ) {
          this.set( key, properties[ key ], options );
        }
      } else if( typeof arguments[ 0 ] === "string" ) {
        var property = arguments[ 0 ];
        var value = arguments[ 1 ];
        var options = arguments[ 2 ];
        this._.properties[ property ] = value;

      }
    },

    destroy : function() {
      for( var key in this._.properties ) {
        this._.properties[ key ] = null;
      }
      this._.properties = null;
      this._ = null;
    },

    get : function( property ) {
      return this._.properties[ property ];
    },

    notify : function( event, properties ) {
      rap.getRemoteObject( this ).notify( event, properties );
    }


  };

}());