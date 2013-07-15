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
      case "userText":
        onChangeUserText.apply( event.source, [ event ] );
      break;
      case "suggestion":
        onChangeSuggestion.apply( event.source, [ event ] );
      break;
      case "resultSelection":
        onChangeResultSelection.apply( event.source, [ event ] );
      break;
      case "results":
        onChangeResults.apply( event.source, [ event ] );
      break;
    }
  }
}

//////////////////
// Event Handling

function onChangeUserText( event ) {
  this.set( "suggestion", null, { "sourceEvent" : "change:userText" } );
  var query = createQuery( event.value.toLowerCase() );
  var results = searchItems( this.get( "elements" ), query );
  this.set( "results", results );
}

function onChangeResults( event ) {
  if( this.get( "autoComplete" ) ) {
    var items = this.get( "results" ).items;
    var options = { "sourceEvent" : "change:results" };
    if( items.length === 1 ) {
      this.set( "suggestion", items[ 0 ], options );
    } else {
      this.set( "suggestion", null, options );
    }
  }
}

function onChangeResultSelection( event ) {
  var suggestion = null;
  if( event.value !== -1 ) {
    suggestion = this.get( "results" ).items[ event.value ] || "";
  }
  this.set( "suggestion", suggestion, { "sourceEvent" : "change:resultSelection" } );
}

function onChangeSuggestion( event ) {
  if( event.options.sourceEvent !== "change:userText" ) {
    var userText = this.get( "userText" ) || "";
    var text = event.value || userText;
    this.set( "text", text );
    if( event.options.sourceEvent === "change:resultSelection" ) {
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
  var indicies = this.get( "results" ).indicies;
  var index = this.get( "resultSelection" );
  this.set( "elementSelection", indicies[ index ] );
}

/////////
// Helper

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
};

function escapeRegExp( str ) {
  return str.replace( /[\-\[\]\/\{\}\(\)\*\+\?\.\\\^\$\|]/g, "\\$&" );
};

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

