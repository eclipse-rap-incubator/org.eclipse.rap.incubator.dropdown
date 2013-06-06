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

  rap.registerTypeHandler( "rwt.remote.Model", {

    factory : function() {
      return new Model();
    },

    isGeneric : true,

    destructor : "destroy"

  } );

  var Model = function() {
    this._ = {
      properties : {},
      listeners : {}
    };
  };

  Model.prototype = {

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

    notify : function() {
      var event = arguments.length === 1 ? arguments[ 0 ].event : arguments[ 0 ];
      var properties = arguments.length === 1 ? arguments[ 0 ].properties : arguments[ 1 ];
      var nosync = arguments.length === 1 ? arguments[ 0 ].nosync : false;
      if( !nosync ) {
        rap.getRemoteObject( this ).notify( event, properties );
      }
      notifyInternal( this, event, properties );
    },

    addListener : function( event, listener ) {
      if( !this._.listeners[ event ] ) {
        this._.listeners[ event ] = [];
      }
      if( this._.listeners[ event ].indexOf( listener ) === -1 ) {
        this._.listeners[ event ].push( listener );
      }
    },

    removeListener : function( event, listener ) {
      if( this._.listeners[ event ] ) {
        var index = this._.listeners[ event ].indexOf( listener );
        rwt.util.Arrays.removeAt( this._.listeners[ event ], index );
      }
    }

  };

  var notifyInternal = function( model, type, properties ) {
    var listeners = model._.listeners[ type ];
    if( listeners instanceof Array ) {
      for( var i = 0; listeners && i < listeners.length; i++ ) {
        listeners[ i ]( model, properties );
      }
    }
  };

}());