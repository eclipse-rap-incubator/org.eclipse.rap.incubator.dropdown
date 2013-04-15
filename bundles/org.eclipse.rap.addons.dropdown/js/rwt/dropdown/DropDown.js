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

  // TODO [tb] : take values from theming or parent, or introduce setter
  var ITEM_HEIGHT = 20;
  var ITEM_FONT = rwt.html.Font.fromArray( [ [ "Arial" ], 12, false, false ] );
  var POPUP_BORDER = new rwt.html.Border( 1, "solid", "#000000" );
  var FRAMEWIDTH = 2;

  var eventTypes = {
    Selection : 13,
    DefaultSelection : 14
  };

  rwt.dropdown = {};

  rwt.dropdown.DropDown = function( parent ) {
    this._ = {};
    this._.hideTimer = new rwt.client.Timer( 0 );
    this._.hideTimer.addEventListener( "interval", checkFocus, this );
    this._.popup = createPopup();
    this._.viewer = createViewer( this._.popup );
    this._.visibleItemCount = 5;
    this._.parent = parent;
    this._.events = createEventsMap();
    this._.viewer.getManager().addEventListener( "changeSelection", onSelection, this );
    this._.viewer.addEventListener( "keypress", onKeyPress, this );
    this._.viewer.addEventListener( "dblclick", onDoubleClick, this );
    this._.popup.getFocusRoot().addEventListener( "changeFocusedChild", onFocusChange, this );
    parent.getFocusRoot().addEventListener( "changeFocusedChild", onFocusChange, this );
    parent.addEventListener( "appear", onAppear, this );
    this._.visibility = false;
  };

  rwt.dropdown.DropDown.prototype = {

    setItems : function( items ) {
      this._.viewer.setItems( items );
      updateScrollBars.call( this );
    },

    setVisibleItemCount : function( itemCount ) {
      this._.visibleItemCount = itemCount;
      updateScrollBars.call( this );
    },

    setSelectionIndex : function( index ) {
      if( index < -1 || index >= this.getItemCount() || isNaN( index ) ) {
        throw new Error( "Can not select item: Index " + index + " not valid" );
      }
      this._.viewer.selectItem( index );
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
      if( this._.parent.isCreated() && !this._.popup.isSeeable() ) {
        var yOffset = this._.parent.getHeight();
        var control = this._.parent;
        this._.popup.positionRelativeTo( control, 0, yOffset );
        this._.popup.setWidth( control.getWidth() );
        this._.popup.setHeight( this._.visibleItemCount * ITEM_HEIGHT + FRAMEWIDTH );
        this._.popup.show();
        this._.viewer.setDimension( this._.popup.getInnerWidth(), this._.popup.getInnerHeight() );
        this._.viewer.setItemDimensions( "100%", ITEM_HEIGHT );
        if( !hasFocus( control ) ) {
          this._.viewer.getFocusRoot().setFocusedChild( this._.viewer );
        }
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
        var focusRoot = this._.parent.getFocusRoot();
        focusRoot.removeEventListener( "changeFocusedChild", onFocusChange, this );
        this._.parent.removeEventListener( "appear", onAppear, this );
        this._.popup.destroy();
        this._.hideTimer.dispose();
        for( var key in this._ ) {
          this._[ key ] = null;
        }
        this._ = null;
      }
    },

    getSelectionIndex : function() {
      return this._.viewer.getItems().indexOf( this._.viewer.getSelectedItem() );
    },

    getItemCount : function() {
      return this._.viewer.getItemsCount();
    },

    getVisibility : function() {
      return this._.popup.getVisibility();
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

  var onKeyPress = function( event ) {
    if( event.getKeyIdentifier() === "Enter" ) {
      fireSelectionEvent.call( this, "DefaultSelection" );
    }
  };

  var onSelection = function( event ) {
    fireSelectionEvent.call( this, "Selection" );
  };

  var onDoubleClick = function( event ) {
    fireSelectionEvent.call( this, "DefaultSelection" );
  };

  var onFocusChange = function( event ) {
    // NOTE : There is no secure way to get the newly focused widget at this point because
    //        it may have another focus root. Therefore we use this timeout and check afterwards:
    this._.hideTimer.start();
  };

  var fireSelectionEvent = function( type ) {
    var selection = this._.viewer.getSelectedItems();
    var eventProxy = {
      "widget" : this,
      "element" : null,
      "type" : eventTypes[ type ]
    };
    if( selection.length > 0 ) {
      // TOOD : use input element instead of label
      eventProxy.element = rwt.util.Encoding.unescape( selection[ 0 ].getLabel() );
    }
    notify( this._.events[ type ], eventProxy );
  };

  var checkFocus = function() {
    this._.hideTimer.stop();
    if( !hasFocus( this._.parent ) && !hasFocus( this._.popup ) ) {
      this.hide();
    }
  };

  var updateScrollBars = function() {
    var scrollable = this._.visibleItemCount < this.getItemCount();
    // TODO [tb] : Horizontal scrolling would require measuring all items preferred width
    this._.viewer.setScrollBarsVisible( false, scrollable );
  };

  var notify = function( listeners, event ) {
    for( var i = 0; i < listeners.length; i++ ) {
      listeners[ i ]( event );
    }
  };

  var createPopup = function() {
    var result = new rwt.widgets.base.Popup();
    result.addToDocument();
    result.setBorder( POPUP_BORDER );
    result.setBackgroundColor( "#ffffff" );
    result.setVisibility( false );
    result.setRestrictToPageOnOpen( false );
    result.setAutoHide( false );
    return result;
  };

  var createViewer = function( parent ) {
    var result = new rwt.widgets.base.BasicList( false );
    result.setLocation( 0, 0 );
    result.setParent( parent );
    result.setFont( ITEM_FONT );
    result.setScrollBarsVisible( false, false );
    return result;
  };

  var checkDisposed = function( dropdown ) {
    if( dropdown.isDisposed() ) {
      throw new Error( "DropDown is disposed" );
    }
  };

  var createEventsMap = function() {
    var result = {};
    for( var key in eventTypes ) {
      result[ key ] = [];
    }
    return result;
  };

  var bind = function( context, method ) {
    return function() {
      return method.apply( context, arguments );
    };
  };

  var hasFocus = function( control ) {
    var root = control.getFocusRoot();
    var result =    control.getFocused()
                 || ( control.contains && control.contains( root.getFocusedChild() ) );
    return result;
  };


}());
