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

///////////////////
// Event Delegation

function handleEvent( model, type, event ) {
  if( type === "acceptSuggestion" ) {
    onAcceptSuggestion.apply( model, [ event ] );
  } else {
    switch( event.property ) {
      case "userText":
        onChangeText.apply( model, [ event ] );
      break;
      case "resultSelection":
        onChangeResultSelection.apply( model, [ event ] );
      break;
    }
  }
}

//////////////////
// Event Handling

function onChangeText( options ) {
  var query = createQuery( options.value.toLowerCase() );
  var results = searchItems( this.get( "elements" ), query );
  this.set( "results", results );
}

function onChangeResultSelection( options ) {
  var text = this.get( "results" ).items[ options.value ] || "";
  this.set( "text", text, { "resultSelection" : true } );
}

function onAcceptSuggestion( options ) {
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

