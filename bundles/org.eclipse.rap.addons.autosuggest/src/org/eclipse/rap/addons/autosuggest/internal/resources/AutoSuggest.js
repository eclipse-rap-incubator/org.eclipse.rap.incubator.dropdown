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

var MODEL_KEY = "org.eclipse.rap.addons.autosuggest#Model";

function handleEvent( event ) {
  if( event.widget ) {
    var model = rap.getObject( event.widget.getData( MODEL_KEY ) );
    if( event.widget.classname === "rwt.dropdown.DropDown" ) {
      handleDropDownEvent( model, event );
    } else {
      handleTextEvent( model, event );
    }
  } else {
    handleModelEvent( event.source, event );
  }
}

function handleModelEvent( model, event ) {
  var textWidget;
  try {
    textWidget = rap.getObject( model.get( "textWidgetId" ) );
  } catch( ex ) {
    // When Text is disposed, AutoSuggest may perform a set operation before it is also disposed
    return;
  }
  var dropDown = rap.getObject( model.get( "dropDownWidgetId" ) );
  if( event.type === "accept" ) {
    onAcceptSuggestion.apply( model, [ event ] );
  } else {
    switch( event.property ) {
      case "dataSourceId":
        onChangeDataSourceId.apply( model, [ event ] );
      break;
      case "suggestions":
        onChangeSuggestions.apply( model, [ event ] );
      break;
      case "userText":
        onChangeUserText.apply( model, [ event ] );
      break;
      case "suggestionsVisible":
        syncModelSuggestionsVisible.apply( model, [ dropDown, event ] );
      break;
      case "currentSuggestions":
        onChangeCurrentSuggestions.apply( model, [ event ] );
      break;
      case "currentSuggestionTexts":
        syncModelCurrentSuggestionTexts.apply( model, [ dropDown, event ] );
      break;
      case "selectedSuggestionIndex":
        onChangeSelectedSuggestionIndex.apply( event.source, [ event ] );
      break;
      case "replacementText":
        onChangeReplacementText.apply( event.source, [ event ] );
      break;
      case "text":
        syncModelText.apply( model, [ textWidget, event ] );
      break;
      case "textSelection":
        syncModelTextSelection.apply( model, [ textWidget, event ] );
      break;
    }
  }
}

function handleDropDownEvent( model, event ) {
  switch( event.type ) {
    case SWT.Show:
    case SWT.Hide:
      syncDropDownVisible.apply( model, [ event.widget, event ] );
    break;
    case SWT.Selection:
      syncDropDownSelection.apply( model, [ event.widget, event ] );
    break;
    case SWT.DefaultSelection:
      forwardDropDownDefaultSelection.apply( model, [ event.widget, event ] );
    break;
  }
}

function handleTextEvent( model, event ) {
  var userAction = getUserAction( event );
  switch( event.type ) {
    case SWT.Modify:
      syncTextText.apply( model, [ event.widget, event, userAction ] );
    break;
  }
  setUserAction( event );
}

////////////////////////////////
// Synchronize Model <-> Widgets

function syncTextText( textWidget, event, userAction ) {
  var text = textWidget.getText();
  this.set( "text", text, { "action" : "sync" } );
  if( userAction ) {
    this.set( "userText", text, { "action" : userAction } );
  }
}

function syncDropDownVisible( dropDown, event ) {
  this.set( "suggestionsVisible", dropDown.getVisible(), { "action" : "sync" }  );
}

function syncModelSuggestionsVisible( dropDown, event ) {
  if( event.options.action !== "sync" ) {
    dropDown.setVisible( event.value );
  }
}

function syncDropDownSelection( dropDown, event ) {
  this.set( "selectedSuggestionIndex", event.index, { "action" : "sync" }  );
}

function forwardDropDownDefaultSelection( dropDown, event ) {
  this.notify( "accept", { type : "accept", "source" : this }  );
}

function syncModelCurrentSuggestionTexts( dropDown, event ) {
  dropDown.setItems( this.get( "currentSuggestionTexts" ) );
}

function syncModelText( textWidget, event ) {
  if( event.options.action !== "sync" ) {
    textWidget.setText( event.value );
  }
}

function syncModelTextSelection( textWidget, event ) {
  textWidget.setSelection( event.value );
}

//////////////////
// Event Handling

function onChangeDataSourceId( event ) {
  this.set( "suggestions", null );
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

function onChangeCurrentSuggestions( event ) {
  var action = event.options.action;
  var currentSuggestions = this.get( "currentSuggestions" );
  if( this.get( "autoComplete" ) && ( action === "typing" || action === "refresh" ) ) {
    var common = commonText( map( currentSuggestions, getReplacementText ) );
    if( common && common.length > this.get( "userText" ).length ) {
      this.set( "replacementText", common );
    }
  }
  ensureTemplate.apply( this );
  var template = this.get( "template" );
  this.set( "currentSuggestionTexts", currentSuggestions.map( template ) );
}

function onChangeSelectedSuggestionIndex( event ) {
  var suggestion = null;
  if( event.value !== -1 ) {
    suggestion = this.get( "currentSuggestions" )[ event.value ] || "";
  }
  var replacementText = getReplacementText( suggestion );
  if( replacementText != null ) {
    this.set( "replacementText", null, { "action" : "sync" } );
  }
  this.set( "replacementText", replacementText, { "action" : "selection" } );
}

function onChangeReplacementText( event ) {
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
      // TODO : not always working?
      this.set( "textSelection", [ userText.length, text.length ] );
    }
  }
}

function onAcceptSuggestion( event ) {
  var currentSuggestions = this.get( "currentSuggestions" );
  if( currentSuggestions ) {
    var index = this.get( "selectedSuggestionIndex" );
    var suggestionSelected = typeof index === "number" && index > -1;
    var autoCompleteAccepted =    this.get( "autoComplete" )
                               && currentSuggestions.length === 1
                               && getReplacementText( currentSuggestions[ 0 ] ) === this.get( "text" );
    if( suggestionSelected || autoCompleteAccepted ) {
      this.notify( "suggestionSelected" );
      this.set( "suggestionsVisible", false );
    }
  }
  var text = this.get( "text" ) || "";
  this.set( "textSelection", [ text.length, text.length ] );
}

function filterSuggestions( options ) {
  fetchSuggestions.apply( this );
  var userText = this.get( "userText" ) || "";
  this.set( "replacementText", null, { "action" : "sync" } );
  ensureFilter.apply( this );
  var filter = this.get( "filter" );
  var filterWrapper = function( suggestion ) {
    return filter( suggestion, userText );
  };
  var currentSuggestions = filterArray( this.get( "suggestions" ), filterWrapper );
  this.set( "currentSuggestions", currentSuggestions, { "action" : options.action } );
}

function fetchSuggestions() {
  if( this.get( "suggestions" ) == null ) {
    var dataSource = getDataSource.apply( this );
    if( dataSource != null ) {
      this.set( "suggestions", dataSource.get( "data" ) );
    } else {
      this.set( "suggestions", [] );
    }
  }
}

var defaultFilter = function( suggestion, userText ) {
  var text = getReplacementText( suggestion );
  return text.toLowerCase().indexOf( userText.toLowerCase() ) === 0;
};

function ensureFilter() {
  if( this.get( "filter" ) == null ) {
    var dataSource = getDataSource.apply( this );
    if( dataSource != null && dataSource.get( "filterScript" ) != null ) {
      try {
        this.set( "filter",
                  secureEval( "var result = " + dataSource.get( "filterScript" ) + "; result;" ) );
      } catch( ex ) {
        throw new Error( "AutoSuggest could not eval filter function: " + ex.message );
      }
    } else {
      this.set( "filter", defaultFilter );
    }
  }
}

// TODO [tb] : there should be default templates for string, array and object
var defaultTemplate = function( suggestion ) {
  return suggestion instanceof Array ? suggestion.slice( 1 ).join( "\t" ) : suggestion;
};

function ensureTemplate() {
  if( this.get( "template" ) == null ) {
    var dataSource = getDataSource.apply( this );
    // TODO [tb] : tempalteScript is never set by the server
    if( dataSource != null && dataSource.get( "templateScript" ) != null ) {
      try {
        this.set( "template",
                  secureEval( "var result = " + dataSource.get( "templateScript" ) + "; result;" ) );
      } catch( ex ) {
        throw new Error( "AutoSuggest could not eval template function: " + ex.message );
      }
    } else {
      this.set( "template", defaultTemplate );
    }
  }
}

function getDataSource() {
  if( this.get( "dataSourceId" ) != null ) {
    return rap.getObject( this.get( "dataSourceId" ) );
  }
  return null;
}

function getReplacementText( suggestion ) {
  return suggestion instanceof Array ? suggestion[ 0 ] : suggestion;
}

////////////////////////
// Helper - autoComplete

function commonText( texts ) {
  var result = null;
  if( texts.length === 1 ) {
    result = texts[ 0 ];
  } else if( texts.length > 1 ) {
    var common = commonChars( texts );
    if( common.length > 0 ) {
      if( allTextsSplitAt( texts, common.length ) ) {
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

function commonChars( texts ) {
  var testItem = texts[ 0 ];
  var result = "";
  var matches = true;
  for( var offset = 0; ( offset < testItem.length ) && matches; offset++ ) {
    var candidate = result + testItem.charAt( offset );
    for( var i = 0; i < texts.length; i++ ) {
      if( texts[ i ].indexOf( candidate ) !== 0 ) {
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

// TODO [tb] : refactor to every( texts, hasSplitCharacterAt )
function allTextsSplitAt( texts, offset ) {
  var splitRegExp = /\W/; // not a letter/digit/underscore
  var result = true;
  for( var i = 0; i < texts.length; i++ ) {
    if( texts[ i ].length !== offset ) {
      var itemChar = texts[ i ].charAt( offset );
      if( !splitRegExp.test( itemChar ) ) {
        result = false;
      }
    }
  }
  return result;
}

/////////
// Helper

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

function secureEval() {
  // TODO : protect against global var access
  return eval( arguments[ 0 ] );
}

function map( array, func ) {
  if( typeof array.map === "function" ) {
    return array.map( func );
  } else {
    var result = [];
    for( var i = 0; i < array.length; i++ ) {
      result[ i ] = func( array[ i ] );
    }
    return result;
  }
}

function setUserAction( event ) {
  if( event.type === SWT.Verify ) {
    // See Bug 404896 - [ClientScripting] Verify event keyCode is always zero when replacing txt
    var action = ( event.text !== "" /* && event.keyCode !== 0 */ ) ? "typing" : "deleting";
    event.widget.setData( "userAction", action );
  }
}

function getUserAction( event ) {
  var action = event.widget.getData( "userAction" );
  event.widget.setData( "userAction", null );
  return action;
}
