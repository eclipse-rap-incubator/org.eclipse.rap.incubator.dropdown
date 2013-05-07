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

  var POPUP_BORDER = new rwt.html.Border( 1, "solid", "#000000" );
  var FRAMEWIDTH = 2;
  var PADDING = ( function() {
    var manager = rwt.theme.AppearanceManager.getInstance();
    var stylemap = manager.styleFrom( "list-item", {} );
    return stylemap.padding || [ 5, 5, 5, 5 ];
  }() );


  // Values identical to SWT
  var eventTypes = {
    Selection : 13,
    DefaultSelection : 14,
    Show : 22,
    Hide : 23
  };

  var forwardedKeys = {
    Enter : true,
    Up : true,
    Down : true,
    PageUp : true,
    PageDown : true,
    Escape : true
  };

  /**
   * @public
   * @namespace
   * @name rwt
   */
  /**
   * @public
   * @namespace
   */
  rwt.dropdown = {};

  /**
   * @class Instances of DropDown represent the server-side counterpart of a DropDown widget
   */
  rwt.dropdown.DropDown = function( parent ) {
    this._ = {};
    this._.hideTimer = new rwt.client.Timer( 0 );
    this._.hideTimer.addEventListener( "interval", checkFocus, this );
    this._.popup = createPopup();
    this._.viewer = createViewer( this._.popup );
    this._.visibleItemCount = 5;
    this._.parent = parent;
    this._.items = [];
    this._.events = createEventsMap();
    this._.parent.addEventListener( "keydown", onTextKeyEvent, this );
    this._.parent.addEventListener( "keypress", onTextKeyEvent, this );
    this._.viewer.getManager().addEventListener( "changeSelection", onSelection, this );
    this._.viewer.addEventListener( "keydown", onKeyEvent, this );
    this._.viewer.addEventListener( "dblclick", onDoubleClick, this );
    this._.popup.addEventListener( "appear", onAppear, this );
    this._.popup.addEventListener( "disappear", onDisappear, this );
    this._.popup.getFocusRoot().addEventListener( "changeFocusedChild", onFocusChange, this );
    parent.getFocusRoot().addEventListener( "changeFocusedChild", onFocusChange, this );
    parent.addEventListener( "appear", onTextAppear, this );
    this._.visibility = false;
  };

  rwt.dropdown.DropDown.prototype = {

    setItems : function( items ) {
      this.setSelectionIndex( -1 );
      this._.items = rwt.util.Arrays.copy( items );
      this._.viewer.setItems( items );
      if( this._.visibility ) {
        renderLayout.call( this );
      }
      updateScrollBars.call( this );
    },

    setVisibleItemCount : function( itemCount ) {
      this._.visibleItemCount = itemCount;
      if( this._.visibility ) {
        renderLayout.call( this );
      }
      updateScrollBars.call( this );
    },

    setSelectionIndex : function( index ) {
      if( index < -1 || index >= this.getItemCount() || isNaN( index ) ) {
        throw new Error( "Can not select item: Index " + index + " not valid" );
      }
      this._.viewer.selectItem( index );
      if( index === -1 ) {
        this._.viewer.getManager().setLeadItem( null );
      }
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
      if( !this._.visibility && !rwt.remote.EventUtil.getSuspended() ) {
        rap.getRemoteObject( this ).set( "visibility", true );
      }
      this._.visibility = true;
      if( this._.parent.isCreated() && !this._.popup.isSeeable() ) {
        this._.viewer.setFont( this._.parent.getFont() );
        renderLayout.call( this );
        this._.popup.show();
        if( !hasFocus( this._.parent ) ) {
          this._.viewer.getFocusRoot().setFocusedChild( this._.viewer );
        }
      }
    },

    hide : function() {
      checkDisposed( this );
      if( this._.visibility && !rwt.remote.EventUtil.getSuspended() ) {
        rap.getRemoteObject( this ).set( "visibility", false );
      }
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
      if( this._ && this._.events[ type ] ) {
        var index = this._.events[ type ].indexOf( listener );
        rwt.util.Arrays.removeAt( this._.events[ type ], index );
      }
    },

    destroy : function() {
      if( !this.isDisposed() ) {
        var focusRoot = this._.parent.getFocusRoot();
        focusRoot.removeEventListener( "changeFocusedChild", onFocusChange, this );
        this._.popup.getFocusRoot().removeEventListener( "changeFocusedChild", onFocusChange, this );
        this._.parent.removeEventListener( "appear", onTextAppear, this );
        this._.parent.removeEventListener( "keydown", onTextKeyEvent, this );
        this._.parent.removeEventListener( "keypress", onTextKeyEvent, this );
        this._.popup.destroy();
        this._.hideTimer.dispose();
        if( this._.widgetData ) {
          for( var key in this._.widgetData ) {
            this._.widgetData[ key ] = null;
          }
        }
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

  ////////////
  // "statics"

  rwt.dropdown.DropDown.searchItems = function( items, query, limit ) {
    var resultIndicies = [];
    var filter = function( item, index ) {
      if( query.test( item ) ) {
        resultIndicies.push( index );
        return true;
      } else {
        return false;
      }
    };
    var resultLimit = typeof limit === "number" ? limit : 0;
    var resultItems = filterArray( items, filter, resultLimit );
    return {
      "items" : resultItems,
      "indicies" : resultIndicies,
      "query" : query,
      "limit" : resultLimit
    };
  };

  rwt.dropdown.DropDown.createQuery = function( str, caseSensitive, ignorePosition ) {
    var escapedStr = rwt.dropdown.DropDown.escapeRegExp( str );
    return new RegExp( ( ignorePosition ? "" : "^" ) + escapedStr, caseSensitive ? "" : "i" );
  };

  rwt.dropdown.DropDown.escapeRegExp = function( str ) {
    return str.replace( /[\-\[\]\/\{\}\(\)\*\+\?\.\\\^\$\|]/g, "\\$&" );
  };

  ////////////
  // Internals

  var renderLayout = function() {
    var yOffset = this._.parent.getHeight();
    var font = this._.viewer.getFont();
    // NOTE: Guessing the lineheight to be 1.3
    var itemHeight = Math.floor( font.getSize() * 1.3 ) + PADDING[ 0 ] + PADDING[ 2 ];
    var visibleItems = Math.min( this._.visibleItemCount, this._.viewer.getItemsCount() );
    this._.popup.positionRelativeTo( this._.parent, 0, yOffset );
    this._.popup.setWidth( this._.parent.getWidth() );
    this._.popup.setHeight( visibleItems * itemHeight + FRAMEWIDTH );
    this._.viewer.setDimension( this._.parent.getWidth() - FRAMEWIDTH, visibleItems * itemHeight );
    this._.viewer.setItemDimensions( "100%", itemHeight );
  };

  var onTextAppear = function() {
    if( this._.visibility ) {
      this.show();
    }
  };

  var onTextKeyEvent = function( event ) {
    var key = event.getKeyIdentifier();
    if( this._.visibility && forwardedKeys[ key ] ) {
      event.preventDefault();
      this._.viewer.dispatchEvent( event );
    }
  };

  var onKeyEvent = function( event ) {
    if( event.getKeyIdentifier() === "Enter" ) {
      rwt.client.Timer.once( function() {
        // NOTE : This async call ensures that the key events is processed before the
        //        DefaultSelection event. A better solution would be to do this for all forwarded
        //        key events, but this would be complicated since the event is disposed by the
        //        time dispatch would be called on the viewer.
        fireEvent.call( this, "DefaultSelection" );
      }, this, 0 );
    } else if( event.getKeyIdentifier() === "Escape" ) {
      this.hide();
    }
  };

  var onSelection = function( event ) {
    if( !rwt.remote.EventUtil.getSuspended() ) {
      rap.getRemoteObject( this ).set( "selectionIndex", this.getSelectionIndex() );
    }
    fireEvent.call( this, "Selection" );
  };

  var onDoubleClick = function( event ) {
    fireEvent.call( this, "DefaultSelection" );
  };

  var onAppear = function( event ) {
    fireEvent.call( this, "Show" );
  };

  var onDisappear = function( event ) {
    fireEvent.call( this, "Hide" );
    //this._.parent.setFocused( true );
  };

  var onFocusChange = function( event ) {
    // NOTE : There is no secure way to get the newly focused widget at this point because
    //        it may have another focus root. Therefore we use this timeout and check afterwards:
    this._.hideTimer.start();
  };

  var fireEvent = function( type ) {
    var event = {
      "text" : "",
      "index" : -1
    };
    if( type === "Selection" || type === "DefaultSelection" ) {
      var selection = this._.viewer.getSelectedItems();
      if( selection.length > 0 ) {
        event.index = this.getSelectionIndex();
        event.text = this._.items[ event.index ];
      }
      if( selection.length > 0 || type !== "DefaultSelection" ) {
        notify.apply( this, [ type, event ] );
        if( !rwt.remote.EventUtil.getSuspended() ) { // TODO [tb] : ClientScripting must reset flag
          // TODO : merge multiple changes? How long?
          rap.getRemoteObject( this ).notify( type, event );
        }
      }
    } else {
      notify.apply( this, [ type, event ] );
    }
  };

  var checkFocus = function() {
    this._.hideTimer.stop();
    if( !hasFocus( this._.parent ) && !hasFocus( this._.popup ) && this._.visibility ) {
      this.hide();
    }
  };

  var updateScrollBars = function() {
    var scrollable = this._.visibleItemCount < this.getItemCount();
    // TODO [tb] : Horizontal scrolling would require measuring all items preferred width
    this._.viewer.setScrollBarsVisible( false, scrollable );
  };

  var notify = function( type, event ) {
    var listeners = this._.events[ type ];
    var eventProxy = rwt.util.Objects.merge( {
      "widget" : this,
      "type" : eventTypes[ type ]
    }, event );
    for( var i = 0; i < listeners.length; i++ ) {
      listeners[ i ]( eventProxy );
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

  var filterArray = function( arr, func, limit ) {
    var result = [];
    if( typeof arr.filter === "function" && limit === 0 ) {
      result = arr.filter( func );
    } else {
      for( var i = 0; i < arr.length; i++ ) {
        if( func( arr[ i ], i ) ) {
          result.push( arr[ i ] );
          if( limit !== 0 && result.length === limit ) {
            break;
          }
        }
      }
    }
    return result;
  };


}());
