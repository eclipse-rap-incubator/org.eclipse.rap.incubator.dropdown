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

//@ sourceURL=AutoSuggest.js

///////////////////
// Event Delegation

function handleEvent( event ) {
  if( event.type === "accept" ) {
    onAcceptSuggestion.apply( event.source, [ event ] );
  } else {
    switch( event.property ) {
      case "dataSourceId":
        onChangeDataSourceId.apply( event.source, [ event ] );
      break;
      case "suggestions":
        onChangeSuggestions.apply( event.source, [ event ] );
      break;
      case "userText":
        onChangeUserText.apply( event.source, [ event ] );
      break;
      case "currentSuggestions":
        onChangeResults.apply( event.source, [ event ] );
      break;
      case "selectedSuggestionIndex":
        onChangeResultSelection.apply( event.source, [ event ] );
      break;
      case "replacementText":
        onChangeSuggestion.apply( event.source, [ event ] );
      break;
    }
  }
}

//////////////////
// Event Handling

function onChangeDataSourceId( event ) {
  this.set( "suggestions", null );
  // TODO : schedule filter?
}

function onChangeSuggestions( event ) {
  // NOTE: Nothing else to do if not visible, but would need to update when it becomes visible.
  //       Currently only onChangeUserText can set resultsVisible to true, which updates implicitly.
  if( this.get( "suggestionsVisible" ) ) {
    filterSuggestions.apply( this, [ { "action" : "refresh" } ] );
  }
}

function onChangeUserText( event ) {
  this.set( "suggestionsVisible", event.value != null && event.value.length > 0  );
  filterSuggestions.apply( this, [ event.options ] );
}

function onChangeResults( event ) {
  var action = event.options.action;
  if( this.get( "autoComplete" ) && ( action === "typing" || action === "refresh" ) ) {
    var currentSuggestions = this.get( "currentSuggestions" );
    var common = commonText( currentSuggestions );
    if( common && common.length > this.get( "userText" ).length ) {
      this.set( "replacementText", common );
    }
  }
}

function onChangeResultSelection( event ) {
  var suggestion = null;
  if( event.value !== -1 ) {
    suggestion = this.get( "currentSuggestions" )[ event.value ] || "";
  }
  this.set( "replacementText", suggestion, { "action" : "selection" } );
}

function onChangeSuggestion( event ) {
  if( event.options.action !== "sync" ) {
    var userText = this.get( "userText" ) || ""; // TODO : could overwrite server set text?
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
  var currentSuggestions = this.get( "currentSuggestions" );
  if( currentSuggestions ) {
    var index = this.get( "selectedSuggestionIndex" );
    var suggestionSelected = typeof index === "number" && index > -1;
    var autoCompleteAccepted = this.get( "autoComplete" ) && currentSuggestions.length === 1;
    if( suggestionSelected || autoCompleteAccepted ) {
      this.notify( "suggestionSelected" );
      this.set( "suggestionsVisible", false );
    }
  }
  var text = this.get( "text" ) || "";
  this.set( "textSelection", [ text.length, text.length ] );
}

var defaultFilter = function( suggestion, userText ) {
  return suggestion.toLowerCase().indexOf( userText.toLowerCase() ) === 0;
};

function filterSuggestions( options ) {
  fetchSuggestions.apply( this );
  var userText = this.get( "userText" ) || "";
  this.set( "replacementText", null, { "action" : "sync" } );
  var filterWrapper = function( suggestion ) {
    return defaultFilter( suggestion, userText );
  }
  var currentSuggestions = filterArray( this.get( "suggestions" ), filterWrapper );
  this.set( "currentSuggestions", currentSuggestions, { "action" : options.action } );
}

function fetchSuggestions() {
  if( this.get( "suggestions" ) == null ) {
    if( this.get( "dataSourceId" ) != null ) {
      var dataSource = rap.getObject( this.get( "dataSourceId" ) );
      this.set( "suggestions", dataSource.get( "data" ) );
    } else {
      this.set( "suggestions", [] );
    }
  }
}

////////////////////////
// Helper - autoComplete

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

//////////////////
// Helper - search

function filterArray( array, filter, limit ) {
  var result = [];
  if( typeof array.filter === "function" && limit > 0 ) {
    result = array.filter( filter );
  } else {
    for( var i = 0; i < array.length; i++ ) {
      if( filter( array[ i ], i ) ) {
        result.push( array[ i ] );
        if( result.length === limit ) {
          break;
        }
      }
    }
  }
  return result;
}

