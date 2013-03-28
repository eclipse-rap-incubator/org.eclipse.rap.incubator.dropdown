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
  'use strict';

  rwt.dropdown = {};

  rwt.dropdown.DropDown = function( linkedControl ) {
    this._ = {};
    this._.popup = createPopup();
    this._.viewer = createViewer( this._.popup );
    this._.linkedControl = linkedControl;
    this._.events = createEventsMap();
    this._.viewer.getManager().addEventListener( "changeSelection", onSelection, this );
    linkedControl.addEventListener( "appear", onAppear, this );
    this._.visibility = false;
  };

  rwt.dropdown.DropDown.prototype = {

    setItems : function( items ) {
      this._.viewer.setItems( items );
    },

    setVisibility : function( value ) {
      if( value ) {
        this.show();
      } else {
        this.hide();
      }
    },

    show : function() {
      checkDisposed( this );
      this._.visibility = true;
      if( this._.linkedControl.isCreated() && !this._.popup.isSeeable() ) {
        var yOffset = this._.linkedControl.getHeight();
        this._.popup.positionRelativeTo( this._.linkedControl, 0, yOffset );
        this._.popup.setWidth( this._.linkedControl.getWidth() );
        this._.popup.show();
        this._.viewer.setDimension( this._.popup.getInnerWidth(), this._.popup.getInnerHeight() );
        this._.viewer.setItemDimensions( "100%", 30 ); // testing only
      }
    },

    hide : function() {
      checkDisposed( this );
      this._.visibility = false;
      this._.popup.hide();
    },

    setData : function( key, value ) {
      if( !this._.widgetData ) {
        this._.widgetData = {};
      }
      if( arguments.length === 1 && key instanceof Object ) {
        rwt.util.Objects.mergeWith( this._.widgetData, key );
      } else {
        this._.widgetData[ key ] = value;
      }
    },

    getData : function( key ) {
      if( !this._.widgetData ) {
        return null;
      }
      var data = this._.widgetData[ key ];
      return data === undefined ? null : data;
    },

    addListener : function( type, listener ) {
      if( this._.events[ type ] ) {
        if( this._.events[ type ].indexOf( listener ) === -1 ) {
          this._.events[ type ].push( listener );
        }
      } else {
        throw new Error( "Unkown type " + type );
      }
    },

    removeListener : function( type, listener ) {
      if( this._.events[ type ] ) {
        var index = this._.events[ type ].indexOf( listener );
        rwt.util.Arrays.removeAt( this._.events[ type ], index );
      }
    },

    destroy : function() {
      if( !this.isDisposed() ) {
        this._.popup.destroy();
        for( var key in this._ ) {
          this._[ key ] = null;
        }
        this._ = null;
      }
    },

    isDisposed : function() {
      return this._ === null;
    },

    toString : function() {
      return "DropDown";
    }

  };

  var onAppear = function() {
    if( this._.visibility ) {
      this.show();
    }
  };

  var onSelection = function( event ) {
    var selection = this._.viewer.getSelectedItems();
    var eventProxy = {
      "widget" : this,
      "element" : rwt.util.Encoding.unescape( selection[ 0 ].getLabel() ) // TOOD : use input el.
    };
    notify( this._.events[ "Selection" ], eventProxy );
  };

  var notify = function( listeners, event ) {
    for( var i = 0; i < listeners.length; i++ ) {
      listeners[ i ]( event );
    }
  };

  var createPopup = function() {
    var result = new rwt.widgets.base.Popup();
    result.addToDocument();
    result.setBorder( defaultBorder );
    result.setBackgroundColor( "#ffffff" );
    // just for testing:
    result.setHeight( 150 );
    result.setRestrictToPageOnOpen( false );
    return result;
  };

  var createViewer = function( parent ) {
    var result = new rwt.widgets.base.BasicList( false );
    result.setLocation( 0, 0 );
    result.setParent( parent );
    return result;
  };

  var checkDisposed = function( dropdown ) {
    if( dropdown.isDisposed() ) {
      throw new Error( "DropDown is disposed" );
    }
  };

  var defaultBorder = new rwt.html.Border( 1, "solid", "#000000" );

  var createEventsMap = function() {
    return {
      "Selection" : []
    };
  };

  var bind = function( context, method ) {
    return function() {
      return method.apply( context, arguments );
    };
  };

}());
