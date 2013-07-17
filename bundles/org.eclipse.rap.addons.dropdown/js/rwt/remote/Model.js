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
        var options = arguments[ 2 ] || {};
        if( this._.properties[ property ] !== value ) {
          var event = {
            "value" : value,
            "type" : "change:" + property,
            "property" : property,
            "options" : options,
            "source" : this
          };
          this._.properties[ property ] = value;
          this.notify( {
            "event" : event.type,
            "properties" : event,
            "nosync" : options.nosync === true
          } );
          event.type = "change";
          this.notify( event.type, event );
        }
      }
    },

    get : function( property ) {
      return this._.properties[ property ];
    },

    notify : function() {
      var useMap = arguments[ 0 ] instanceof Object;
      var event = useMap ? arguments[ 0 ].event : arguments[ 0 ];
      var properties = useMap ? arguments[ 0 ].properties : arguments[ 1 ];
      var nosync = useMap ? arguments[ 0 ].nosync : false;
      if( !nosync ) {
        notifyRemote( this, event, properties );
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
      if( this._ && this._.listeners[ event ] ) {
        var index = this._.listeners[ event ].indexOf( listener );
        rwt.util.Arrays.removeAt( this._.listeners[ event ], index );
      }
    },

    destroy : function() {
      if( this._ ) {
        for( var key in this._ ) {
          this._[ key ] = null;
        }
        this._.properties = null;
        this._ = null;
      }
    }

  };

  var notifyInternal = function( model, type, properties ) {
    if( model._ ) {
      var listeners = model._.listeners[ type ];
      var args = [ properties ];
      if( listeners instanceof Array ) {
        for( var i = 0; listeners && i < listeners.length; i++ ) {
          listeners[ i ].apply( listeners[ i ], args );
        }
      }
    }
  };

  var notifyRemote = function( model, type, properties ) {
    var propCopy = {};
    for( var key in properties ) {
      if( properties[ key ] && properties[ key ]._rwtId ) {
        propCopy[ key ] = properties[ key ]._rwtId;
      } else {
        propCopy[ key ] = properties[ key ];
      }
    }
    rap.getRemoteObject( model ).notify( type, propCopy );
  };

}());
