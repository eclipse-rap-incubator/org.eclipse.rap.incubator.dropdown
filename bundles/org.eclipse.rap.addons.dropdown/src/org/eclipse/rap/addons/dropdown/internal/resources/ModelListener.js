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

//@ sourceURL=ModelListener.js

///////////////////
// Event Delegation

function handleEvent( event ) {
  if( event.type === "accept" ) {
    onAcceptSuggestion.apply( event.source, [ event ] );
  } else {
    switch( event.property ) {
      case "elements":
        onChangeElements.apply( event.source, [ event ] );
      break;
      case "userText":
        onChangeUserText.apply( event.source, [ event ] );
      break;
      case "results":
        onChangeResults.apply( event.source, [ event ] );
      break;
      case "resultSelection":
        onChangeResultSelection.apply( event.source, [ event ] );
      break;
      case "suggestion":
        onChangeSuggestion.apply( event.source, [ event ] );
      break;
    }
  }
}

//////////////////
// Event Handling

function onChangeElements( event ) {
  // NOTE: Nothing to do if not visible, but would need to update when it becomes visible.
  //       Currently only onChangeUserText can set resultsVisible to true, which updates implicitly.
  if( this.get( "resultsVisible" ) ) {
    search.apply( this, [ { "action" : "refresh" } ] );
  }
}

function onChangeUserText( event ) {
  this.set( "resultsVisible", true );
  search.apply( this, [ event.options ] );
}

function onChangeResults( event ) {
  var action = event.options.action;
  if( this.get( "autoComplete" ) && ( action === "typing" || action === "refresh" ) ) {
    var items = this.get( "results" ).items;
    var common = commonText( items );
    if( common && common.length > this.get( "userText" ).length ) {
      this.set( "suggestion", common );
    }
  }
}

function onChangeResultSelection( event ) {
  var suggestion = null;
  if( event.value !== -1 ) {
    suggestion = this.get( "results" ).items[ event.value ] || "";
  }
  this.set( "suggestion", suggestion, { "action" : "selection" } );
}

function onChangeSuggestion( event ) {
  if( event.options.action !== "sync" ) {
    var userText = this.get( "userText" ) || "";
    var text = event.value || userText;
    this.set( "text", text );
    if( event.options.action === "selection" ) {
      if( event.value === null ) {
        this.set( "textSelection", [ text.length , text.length ] );
      } else {
        this.set( "textSelection", [ 0, text.length ] );
      }
    } else {
      this.set( "textSelection", [ userText.length, text.length ] );
    }
  }
}

function onAcceptSuggestion( event ) {
  var results = this.get( "results" );
  if( results ) {
    var index = this.get( "resultSelection" );
    if( typeof index === "number" && index > -1 ) {
      this.set( "elementSelection", results.indicies[ index ] );
      this.set( "resultsVisible", false );
    } else if( this.get( "autoComplete" ) && results.indicies.length === 1 ) {
      this.set( "elementSelection", results.indicies[ 0 ] );
      this.set( "resultsVisible", false );
    }
  }
  var text = this.get( "text" ) || "";
  this.set( "textSelection", [ text.length, text.length ] );
}

function search( options ) {
  var userText = this.get( "userText" ) || "";
  this.set( "suggestion", null, { "action" : "sync" } );
  var query = createQuery( userText.toLowerCase() );
  var results = searchItems( this.get( "elements" ), query );
  this.set( "results", results, { "action" : options.action } );
}

/////////
// Helper

function commonText( items ) {
  var result = null;
  if( items.length === 1 ) {
    result = items[ 0 ];
  } else if( items.length > 1 ) {
    var common = commonChars( items );
    if( common.length > 0 ) {
      if( allItemsSplitAt( items, common.length ) ) {
        result = common;
      } else {
        var splitRegExp = /\W/g;
        var matches = common.match( splitRegExp );
        if( matches && matches.length > 0 ) {
          var lastSplitCharOffset = common.lastIndexOf( matches.pop() );
          result = common.slice( 0, lastSplitCharOffset + 1 );
        }
      }
    }
  }
  return result;
}

function commonChars( items ) {
  var testItem = items[ 0 ];
  var result = "";
  var matches = true;
  for( var offset = 0; ( offset < testItem.length ) && matches; offset++ ) {
    var candidate = result + testItem.charAt( offset );
    for( var i = 0; i < items.length; i++ ) {
      if( items[ i ].indexOf( candidate ) !== 0 ) {
        matches = false;
        break;
      }
    }
    if( matches ) {
      result = candidate;
    }
  }
  return result;
}

function allItemsSplitAt( items, offset ) {
  var splitRegExp = /\W/; // not a letter/digit/underscore
  var result = true;
  for( var i = 0; i < items.length; i++ ) {
    if( items[ i ].length !== offset ) {
      var itemChar = items[ i ].charAt( offset );
      if( !splitRegExp.test( itemChar ) ) {
        result = false;
      }
    }
  }
  return result;
}

function searchItems( items, query, limit ) {
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
}

function createQuery( str, caseSensitive, ignorePosition ) {
  var escapedStr = escapeRegExp( str );
  return new RegExp( ( ignorePosition ? "" : "^" ) + escapedStr, caseSensitive ? "" : "i" );
}

function escapeRegExp( str ) {
  return str.replace( /[\-\[\]\/\{\}\(\)\*\+\?\.\\\^\$\|]/g, "\\$&" );
}

function filterArray( arr, func, limit ) {
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
}

