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

 describe( "AutoSuggest", function() {

  var createClientListener = function( name ) {
    // NOTE : Using + "" to convert Java string to JavaScript string. Alternatives?
    var listenerScript = TestUtil.getResource( name ) + "";
    var listener = rwt.scripting.FunctionFactory.createFunction( listenerScript, "handleEvent" );
    return listener;
  };

  var getVarFromScript = function( scriptName, functionName ) {
    var script = TestUtil.getResource( scriptName ) + "";
    var result;
    try {
      result = eval( script + "\n" + functionName + ";" );
    } catch( ex ) {
      throw new Error( "Could not evaluate script " + scriptName + ": " + ex );
    }
    return result;
  };

  describe( "secureEval", function() {

    var secureEval;

    beforeEach( function() {
      if( !secureEval ) {
        secureEval = getVarFromScript( "AutoSuggest.js", "secureEval" );
      }
    } );

    it( "evals code", function() {
      expect( secureEval( "1+2;" ) ).toBe( 3 );
    } );

    it( "can not access local variables", function() {
      var foo = 1;
      expect( secureEval( "typeof foo;" ) ).toBe( "undefined" );
    } );

  } );

  describe( "commonText", function() {

    var commonText;

    beforeEach( function() {
      if( !commonText ) {
        commonText = getVarFromScript( "AutoSuggest.js", "commonText" );
      }
    } );

    it( "returns the only item", function() {
      expect( commonText( [ "foo" ] ) ).toBe( "foo" );
    } );

    it( "returns null for empty list", function() {
      expect( commonText( [ ] ) ).toBeNull();
    } );

    it( "returns null for no common text", function() {
      expect( commonText( [ "foo", "bar", "lalafoobar" ] ) ).toBeNull();
    } );

    it( "returns null for no common text before space", function() {
      expect( commonText( [ "foobar", "foola", "foo doo" ] ) ).toBeNull();
    } );

    it( "returns common text until space", function() {
      expect( commonText( [ "foo bar", "foo la", "foo doo" ] ) ).toBe( "foo " );
    } );

    it( "returns common text until dot", function() {
      expect( commonText( [ "foo.bar", "foo.la", "foo.doo" ] ) ).toBe( "foo." );
    } );

    it( "returns common text until comma", function() {
      expect( commonText( [ "foo,bar", "foo,la", "foo,doo" ] ) ).toBe( "foo," );
    } );

    it( "returns common text after space until dot", function() {
      var items = [ "banana foo.bar", "banana foo.la", "banana foo.doo" ];
      expect( commonText( items ) ).toBe( "banana foo." );
    } );

    it( "returns null for common until underscore", function() {
      expect( commonText( [ "foo_bar", "foo_la", "foo_doo" ] ) ).toBeNull();
    } );

    it( "returns common text with first item that equals common text", function() {
      expect( commonText( [ "foo bar", "foo bar la", "foo bar doo" ] ) ).toBe( "foo bar" );
    } );

    it( "returns common text with last item that equals common text", function() {
      expect( commonText( [ "foo bar la", "foo bar doo", "foo bar" ] ) ).toBe( "foo bar" );
    } );

  } );

  describe( "filterArray", function() {

    var filterArray;

    beforeEach( function() {
      if( !filterArray ) {
        filterArray = getVarFromScript( "AutoSuggest.js", "filterArray" );
      }
    } );

    it( "returns some", function() {
      var items = [ "afoo", "bar", "food", "abc" ];

      var results = filterArray( items, function( suggestion ) {
        return suggestion.indexOf( "foo" ) !== -1;
      } );

      expect( results ).toEqual( [ "afoo", "food" ] );
    } );

    it( "returns multiple items with limit", function() {
      var items = [ "afoo", "bfoo", "x", "cfoo", "foor", "four" ];

      var results = filterArray( items, function(){ return true; }, 3 );

      expect( [ "afoo", "bfoo", "x" ], results );
    } );

  } );

  describe( "listener", function() {

    var model;
    var logger;
    var log;

    beforeEach( function() {
      rap = new RapMock();
      spyOn( rap, "on" );
      spyOn( rap, "off" );
      model = rap.typeHandler[ "rwt.remote.Model" ].factory(); // make model "public"
      model.set( "suggestions", [ "foo", "bar", "foobar", "banana", "apple", "cherry" ] );
      log = [];
      logger = function() {
        log.push( arguments );
      };
    } );

    afterEach( function() {
      model.destroy();
    } );

    describe( "change:dataSourceId", function() {

      it( "does nothing before the  next render event", function() {
        model.addListener( "change:dataSourceId", createClientListener( "AutoSuggest.js" ) );
        model.set( "suggestions", [] );

        model.set( "dataSourceId", "fooId" );

        expect( model.get( "suggestions" ) ).not.toBeNull();
      } );

      it( "sets suggestions to null on the next render event", function() {
        model.addListener( "change:dataSourceId", createClientListener( "AutoSuggest.js" ) );
        model.set( "suggestions", [] );

        model.set( "dataSourceId", "fooId" );
        expect( rap.on ).toHaveBeenCalledWith( "render", jasmine.any( Function ) );
        var listener = rap.on.argsForCall[ 0 ][ 1 ];
        listener();

        expect( model.get( "suggestions" ) ).toBeNull();
      } );

      it( "sets suggestions to null only once", function() {
        model.addListener( "change:dataSourceId", createClientListener( "AutoSuggest.js" ) );
        model.set( "suggestions", [] );

        model.set( "dataSourceId", "fooId" );
        var listener = rap.on.argsForCall[ 0 ][ 1 ];
        listener();

        expect( rap.off ).toHaveBeenCalledWith( "render", listener );
      } );

    } );

    describe( "change:userText", function() {

      it( "clears replacementText", function() {
        model.addListener( "change:userText", createClientListener( "AutoSuggest.js" ) );
        model.set( "replacementText", "banana" );

        model.set( "userText", "ba" );

        expect( model.get( "replacementText" ) ).toBeNull();
      } );

      it( "shows suggestions", function() {
        model.addListener( "change:userText", createClientListener( "AutoSuggest.js" ) );

        model.set( "userText", "ba" );

        expect( model.get( "suggestionsVisible" ) ).toBe( true );
      } );

      it( "hides results if text length is zero", function() {
        model.addListener( "change:userText", createClientListener( "AutoSuggest.js" ) );
        model.set( "suggestionsVisible", true );

        model.set( "userText", "" );

        expect( model.get( "suggestionsVisible" ) ).toBe( false );
      } );

      it( "updates currentSuggestions", function() {
        model.addListener( "change:userText", createClientListener( "AutoSuggest.js" ) );

        model.set( "userText", "ba" );

        expect( model.get( "currentSuggestions" ) ).toEqual( [ "bar", "banana" ] );
      } );

      it( "updates currentSuggestions of type array", function() {
        model.addListener( "change:userText", createClientListener( "AutoSuggest.js" ) );
        model.set( "suggestions", [ [ "foo" ], [ "bar" ] ] );

        model.set( "userText", "ba" );

        expect( model.get( "currentSuggestions" ) ).toEqual( [ [ "bar" ] ] );
      } );

      it( "gets suggestions from data source if suggestions are not set", function() {
        model.addListener( "change:userText", createClientListener( "AutoSuggest.js" ) );
        model.set( "suggestions", null );
        var dataSource = rap.typeHandler[ "rwt.remote.Model" ].factory();
        dataSource.set( "data", [ "foo", "bar", "foobar", "banana", "apple", "cherry" ] );
        spyOn( rap, "getObject" ).andReturn( dataSource );
        model.set( "dataSourceId", "fooId" );

        model.set( "userText", "ba" );

        expect( model.get( "currentSuggestions" ) ).toEqual( [ "bar", "banana" ] );
      } );

      it( "sets empty currentSuggestions array if no data source is set", function() {
        model.addListener( "change:userText", createClientListener( "AutoSuggest.js" ) );
        model.set( "suggestions", null );

        model.set( "userText", "ba" );

        expect( model.get( "currentSuggestions" ) ).toEqual( [] );
      } );

      it( "forwards action option", function() {
        model.addListener( "change:userText", createClientListener( "AutoSuggest.js" ) );
        model.addListener( "change:currentSuggestions", logger );

        model.set( "userText", "ba", { "action" : "foo" } );

        expect( log[ 0 ][ 0 ].options.action ).toBe( "foo" );
      } );

      it( "uses custom filter from dataSource if present", function() {
        model.addListener( "change:userText", createClientListener( "AutoSuggest.js" ) );
        model.set( "suggestions", null );
        var dataSource = rap.typeHandler[ "rwt.remote.Model" ].factory();
        dataSource.set( "data", [ "foo", "bar" ] );
        spyOn( rap, "getObject" ).andReturn( dataSource );
        model.set( "dataSourceId", "fooId" );
        dataSource.set( "filterScript",
            "function( suggestion, userText ) { "
          + "  return suggestion.indexOf( userText ) !== -1;"
          + "}"
        );

        model.set( "userText", "a" );

        expect( model.get( "currentSuggestions" ) ).toEqual( [ "bar" ] );
      } );

      it( "caches evaluated filter function", function() {
        model.addListener( "change:userText", createClientListener( "AutoSuggest.js" ) );
        model.set( "suggestions", null );
        var dataSource = rap.typeHandler[ "rwt.remote.Model" ].factory();
        dataSource.set( "data", [ "foo" ] );
        spyOn( rap, "getObject" ).andReturn( dataSource );
        model.set( "dataSourceId", "fooId" );
        dataSource.set( "filterScript", "function() { return false; }" );
        model.set( "userText", "a" );

        dataSource.set( "filterScript", "function() { return true; }" );
        model.set( "userText", "b" );

        expect( model.get( "currentSuggestions" ) ).toEqual( [] );
      } );

      it( "throws custom exception when filterScript not parse", function() {
        model.addListener( "change:userText", createClientListener( "AutoSuggest.js" ) );
        model.set( "suggestions", null );
        var dataSource = rap.typeHandler[ "rwt.remote.Model" ].factory();
        dataSource.set( "data", [ "foo", "bar" ] );
        spyOn( rap, "getObject" ).andReturn( dataSource );
        model.set( "dataSourceId", "fooId" );
        dataSource.set( "filterScript", "funasdfction() { }" );
        var error;

        try {
          model.set( "userText", "a" );
        } catch( ex ) {
          error = ex;
        }

        expect( error.message ).toContain( "AutoSuggest" );
      } );

    } );

    describe( "change:suggestions", function() {

      it( "clears suggestion", function() {
        model.addListener( "change:suggestions", createClientListener( "AutoSuggest.js" ) );
        model.set( "replacementText", "banana" );
        model.set( "suggestionsVisible", true );
        model.addListener( "change:replacementText", logger );

        model.set( "suggestions", [] );

        expect( model.get( "replacementText" ) ).toBeNull();
      } );

      it( "updates suggestions", function() {
        model.addListener( "change:suggestions", createClientListener( "AutoSuggest.js" ) );
        model.set( "userText", "ba" );
        model.set( "suggestionsVisible", true );

        model.set( "suggestions", [ "foo", "bar" ] );

        expect( model.get( "currentSuggestions" ) ).toEqual( [ "bar" ] );
      } );

      it( "does not update suggestions if not visible", function() {
        model.addListener( "change:suggestions", createClientListener( "AutoSuggest.js" ) );
        model.set( "suggestionsVisible", false );
        model.addListener( "change:currentSuggestions", logger );

        model.set( "suggestions", [ "foo", "bar" ] );

        expect( log.length ).toBe( 0 );
      } );

      it( "sets suggestions with refresh option", function() {
        model.addListener( "change:suggestions", createClientListener( "AutoSuggest.js" ) );
        model.addListener( "change:currentSuggestions", logger );
        model.set( "suggestionsVisible", true );
        model.set( "userText", "ba" );

        model.set( "suggestions", [ "foo", "bar" ] );

        expect( log[ 0 ][ 0 ].options.action ).toBe( "refresh" );
      } );

    } );

    describe( "change:selectedSuggestionIndex", function() {

      it( "sets replacementText to selected suggestion", function() {
        model.addListener( "change:selectedSuggestionIndex", createClientListener( "AutoSuggest.js" ) );
        model.set( "currentSuggestions", [ "bar", "banana" ] );

        model.set( "selectedSuggestionIndex", 1 );

        expect( model.get( "replacementText" ) ).toEqual( "banana" );
      } );

      it( "sets replacementText for array suggestion", function() {
        model.addListener( "change:selectedSuggestionIndex", createClientListener( "AutoSuggest.js" ) );
        model.set( "currentSuggestions", [ [ "a", "b", "c" ], [ "x", "y", "z" ] ] );

        model.set( "selectedSuggestionIndex", 1 );

        expect( model.get( "replacementText" ) ).toEqual( "x" );
      } );

      it( "resets suggestion when selection index is -1", function() {
        model.addListener( "change:selectedSuggestionIndex", createClientListener( "AutoSuggest.js" ) );
        model.set( "currentSuggestions", [ "bar", "banana" ] );
        model.set( "replacementText", "banana" );

        model.set( "selectedSuggestionIndex", -1 );

        expect( model.get( "replacementText" ) ).toBeNull();
      } );

      it( "sets action option", function() {
        model.addListener( "change:selectedSuggestionIndex", createClientListener( "AutoSuggest.js" ) );
        model.addListener( "change:replacementText", logger );
        model.set( "currentSuggestions", [ "bar", "banana" ] );

        model.set( "selectedSuggestionIndex", 1 );

        expect( log[ log.length - 1 ][ 0 ].options.action ).toBe( "selection" );
      } );

      it( "fires replacementText change event even if value is unchanged", function() {
        model.addListener( "change:selectedSuggestionIndex", createClientListener( "AutoSuggest.js" ) );
        model.set( "currentSuggestions", [ "bar", "banana" ] );
        model.set( "replacementText", "banana" );
        model.addListener( "change:replacementText", logger );

        model.set( "selectedSuggestionIndex", 1 );

        expect( model.get( "replacementText" ) ).toEqual( "banana" );
        expect( log[ log.length - 1 ][ 0 ].options.action ).toBe( "selection" );
      } );

      it( "fires replacementText change event with action selection when restting", function() {
        model.addListener( "change:selectedSuggestionIndex", createClientListener( "AutoSuggest.js" ) );
        model.set( "currentSuggestions", [ "bar", "banana" ] );
        model.set( "replacementText", "banana" );
        model.addListener( "change:replacementText", logger );

        model.set( "selectedSuggestionIndex", -1 );

        expect( model.get( "replacementText" ) ).toBeNull();
        expect( log[ log.length - 1 ][ 0 ].options.action ).toBe( "selection" );
      } );

    } );

    describe( "change:replacementText", function() {

      it( "ignores events from change:userText", function() {
        model.addListener( "change:replacementText", createClientListener( "AutoSuggest.js" ) );
        model.set( "text", "bar" );
        model.set( "textSelection", [ 0, 0 ] );

        model.set( "replacementText", "foo", { "action" : "sync" } );

        expect( model.get( "text" ) ).toEqual( "bar" );
        expect( model.get( "textSelection" ) ).toEqual( [ 0, 0 ] );
      } );

      it( "sets text to suggestion", function() {
        model.addListener( "change:replacementText", createClientListener( "AutoSuggest.js" ) );

        model.set( "replacementText", "foo" );

        expect( model.get( "text" ) ).toEqual( "foo" );
      } );

      it( "sets textSelection for replacementText", function() {
        model.addListener( "change:replacementText", createClientListener( "AutoSuggest.js" ) );

        model.set( "replacementText", "foo", { "action" : "selection" } );

        expect( model.get( "textSelection" ) ).toEqual( [ 0, 3 ] );
      } );

      it( "sets textSelection for auto complete", function() {
        model.addListener( "change:replacementText", createClientListener( "AutoSuggest.js" ) );
        model.set( "userText", "foo" );

        model.set( "replacementText", "foobar" );

        expect( model.get( "textSelection" ) ).toEqual( [ 3, 6 ] );
      } );

      it( "resets text to userText", function() {
        model.addListener( "change:replacementText", createClientListener( "AutoSuggest.js" ) );
        model.set( "userText", "bar" );

        model.set( "replacementText", null, { "action" : "selection" } );

        expect( model.get( "text" ) ).toEqual( "bar" );
      } );

      it( "resets selection to userText end", function() {
        model.addListener( "change:replacementText", createClientListener( "AutoSuggest.js" ) );
        model.set( "userText", "bar" );

        model.set( "replacementText", null, { "action" : "selection" } );

        expect( model.get( "textSelection" ) ).toEqual( [ 3, 3 ] );
      } );

    } );

    describe( "change:currentSuggestions", function() {

      it( "does not change replacementText without autocomplete", function() {
        model.set( "replacementText", "ban" );
        model.addListener( "change:currentSuggestions", createClientListener( "AutoSuggest.js" ) );

        model.set( "currentSuggestions", [ "banana" ] );

        expect( model.get( "replacementText" ) ).toEqual( "ban" );
      } );

      it( "does change replacementText if not typing", function() {
        model.set( "replacementText", "ban" );
        model.addListener( "change:currentSuggestions", createClientListener( "AutoSuggest.js" ) );
        model.set( "autoComplete", true );

        model.set( "currentSuggestions", [ "banana" ] );

        expect( model.get( "replacementText" ) ).toEqual( "ban" );
      } );

      it( "autocompletes text while typing on single currentSuggestion", function() {
        model.set( "replacementText", "ban" );
        model.set( "userText", "b" );
        model.set( "autoComplete", true );
        model.addListener( "change:currentSuggestions", createClientListener( "AutoSuggest.js" ) );

        model.set( "currentSuggestions", [ [ "banana", "foo" ] ], { "action" : "typing" } );

        expect( model.get( "replacementText" ) ).toEqual( "banana" );
      } );

      it( "autocompletes currentSuggestion that is array", function() {
        model.set( "replacementText", "ban" );
        model.set( "userText", "b" );
        model.set( "autoComplete", true );
        model.addListener( "change:currentSuggestions", createClientListener( "AutoSuggest.js" ) );

        model.set( "currentSuggestions", [ [ "banana", "foo" ] ], { "action" : "typing" } );

        expect( model.get( "replacementText" ) ).toEqual( "banana" );
      } );

      it( "autocompletes suggestion while refreshing on single currentSuggestion", function() {
        model.set( "replacementText", "ban" );
        model.set( "userText", "b" );
        model.set( "autoComplete", true );
        model.addListener( "change:currentSuggestions", createClientListener( "AutoSuggest.js" ) );

        model.set( "currentSuggestions", [ "banana" ], { "action" : "refresh" } );

        expect( model.get( "replacementText" ) ).toEqual( "banana" );
      } );

      it( "partially autocompletes suggestion for common text", function() {
        model.set( "autoComplete", true );
        model.set( "userText", "b" );
        model.addListener( "change:currentSuggestions", createClientListener( "AutoSuggest.js" ) );

        var items = [ "banana foo", "banana bar" ];
        model.set( "currentSuggestions", items, { "action" : "typing" } );

        expect( model.get( "replacementText" ) ).toEqual( "banana " );
      } );

      it( "does not autocomplete if common text is shorter than userText", function() {
        model.set( "replacementText", null );
        model.set( "autoComplete", true );
        model.set( "userText", "banana xxx" );
        model.addListener( "change:currentSuggestions", createClientListener( "AutoSuggest.js" ) );

        var items = [ "banana foo", "banana bar" ];
        model.set( "currentSuggestions", items, { "action" : "typing" } );

        expect( model.get( "replacementText" ) ).toBe( null );
      } );

      it( "sets currentSuggestionTexts unchanged if no custom template is set", function() {
        model.addListener( "change:currentSuggestions", createClientListener( "AutoSuggest.js" ) );

        model.set( "currentSuggestions", [ "a", "b" ] );

        expect( model.get( "currentSuggestionTexts" ) ).toEqual( [ "a", "b" ] );
      } );

      it( "sets currentSuggestionTexts for columns", function() {
        model.addListener( "change:currentSuggestions", createClientListener( "AutoSuggest.js" ) );

        model.set( "currentSuggestions", [ [ "a", "b", "c" ], [ "x", "y", "z" ] ] );

        expect( model.get( "currentSuggestionTexts" ) ).toEqual( [ "b\tc", "y\tz" ] );
      } );

      it( "applies template to currentSuggestionTexts", function() {
        model.addListener( "change:currentSuggestions", createClientListener( "AutoSuggest.js" ) );
        model.set( "template", function( suggestion ) { return "x" + suggestion; } );

        model.set( "currentSuggestions", [ "a", "b" ] );

        expect( model.get( "currentSuggestionTexts" ) ).toEqual( [ "xa", "xb" ] );
      } );

      it( "evaluates templateScript from dataSource", function() {
        model.addListener( "change:currentSuggestions", createClientListener( "AutoSuggest.js" ) );
        var dataSource = rap.typeHandler[ "rwt.remote.Model" ].factory();
        spyOn( rap, "getObject" ).andReturn( dataSource );
        dataSource.set( "templateScript", "function( suggestion ) { return \"x\" + suggestion; }" );
        model.set( "dataSourceId", "fooId" );

        model.set( "currentSuggestions", [ "a", "b" ] );

        expect( model.get( "template" )( "foo" ) ).toBe( "xfoo" );
      } );

      it( "throws custom exception when templateScript can not be parsed", function() {
        model.addListener( "change:currentSuggestions", createClientListener( "AutoSuggest.js" ) );
        var dataSource = rap.typeHandler[ "rwt.remote.Model" ].factory();
        spyOn( rap, "getObject" ).andReturn( dataSource );
        dataSource.set( "templateScript", "funasdfction( suggestion ) { return true; }" );
        model.set( "dataSourceId", "fooId" );
        var error;

        try {
          model.set( "currentSuggestions", [ "a", "b" ] );
        } catch( ex ) {
          error = ex;
        }

        expect( error.message ).toContain( "AutoSuggest" );
      } );

    } );

    describe( "accept", function() {

      beforeEach( function() {
        model.set( "suggestionsVisible", true );
        model.set( "selectedSuggestionIndex", -1 );
        model.addListener( "accept", createClientListener( "AutoSuggest.js" ) );
        model.addListener( "suggestionSelected", logger );
      } );

      it( "fires suggestionSelected for selectedSuggestionIndex", function() {
        model.set( "currentSuggestions", [ "bar", "banana" ] );
        model.set( "selectedSuggestionIndex", 1 );

        model.notify( "accept", { source : model, type : "accept" } );

        expect( log.length ).toBe( 1 );
        expect( model.get( "suggestionsVisible" ) ).toBe( false );
      } );

      describe( "for full auto complete", function() {

        beforeEach( function() {
          model.set( "autoComplete", true );
          model.set( "userText", "ban" );
          model.set( "text", "banana" );
        } );

        it( "fires suggestionSelected for simple suggestion", function() {
          model.set( "currentSuggestions", [ "banana" ] );

          model.notify( "accept", { source : model, type : "accept" } );

          expect( log.length ).toBe( 1 );
          expect( model.get( "suggestionsVisible" ) ).toBe( false );
        } );

        it( "fires suggestionSelected for array suggestion", function() {
          model.set( "currentSuggestions", [ [ "banana", "foo" ] ] );
          model.notify( "accept", { source : model, type : "accept" } );

          expect( log.length ).toBe( 1 );
          expect( model.get( "suggestionsVisible" ) ).toBe( false );
        } );

      } );

      it( "does nothing when attempting accepting without selected suggestion or auto complete", function() {
        model.set( "currentSuggestions", [ "banana" ] );
        model.set( "userText", "ban" );
        model.set( "text", "ban" );

        model.notify( "accept", { source : model, type : "accept" } );

        expect( log.length ).toBe( 0 );
        expect( model.get( "suggestionsVisible" ) ).toBe( true );
      } );

      it( "does nothing if inserted replacement text has been deleted", function() {
        model.set( "currentSuggestions", [ "banana" ] );
        model.set( "autoComplete", true );
        model.set( "userText", "ban" );
        model.set( "text", "Ban" );

        model.notify( "accept", { source : model, type : "accept" } );

        expect( log.length ).toBe( 0 );
        expect( model.get( "suggestionsVisible" ) ).toBe( true );
      } );

      it( "clears text selection", function() {
        model.set( "text", "foobar" );

        model.notify( "accept", { source : model, type : "accept" } );

        expect( model.get( "textSelection" ) ).toEqual( [ 6, 6 ] );
      } );

    } );

  } );

 } );
