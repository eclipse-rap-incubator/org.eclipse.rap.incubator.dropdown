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

    isGeneric : true

  } );

  var UniversalRemoteObject = function() {
    this._ = {
      //listener : {},
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
//        if( !options || !options.nosync ) {
        // TODO : perhaps limit to values created by the server?
//          rap.getRemoteObject( this ).set( property, value );
//        }
      }
    },

    get : function( property ) {
      return this._.properties[ property ];
    },

    notify : function( event, properties ) {
      rap.getRemoteObject( this ).notify( event, properties );
//      this._notifyInternal( event, properties );
    }

//    _notifyInternal : function( event, properties ) {
//      var listeners = this._.listener[ event ];
//      for( var i = 0; listeners && i < listeners.length; i++ ) {
//        listeners[ i ]( event );
//      }
//    },
//
//    addListener : function( type, listener ) {
//      if( !this._.listener[ type ] ) {
//        this._.listener[ type ] = [];
//      }
//      if( this._.events[ type ].indexOf( listener ) === -1 ) {
//        this._.events[ type ].push( listener );
//      }
//    },
//
//    removeListener : function( type, listener ) {
//      if( this._.listener[ type ] ) {
//        var index = this._.listener[ type ].indexOf( listener );
//        rwt.util.Arrays.removeAt( this._.listener[ type ], index );
//      }
//    }

  };

}());