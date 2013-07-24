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
    var listener = new org.eclipse.rap.clientscripting.Function( listenerScript );
    return function() {
      listener.call.apply( listener, arguments );
    };
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

  describe( "createQuery", function() {

    var createQuery; // can not be loaded here since resources are not ready yet.

    beforeEach( function() {
      if( !createQuery ) {
        createQuery = getVarFromScript( "AutoSuggest", "createQuery" );
      }
    } );

    it( "exists", function() {
      expect( createQuery instanceof Function ).toBe( true );
    } );

    it( "creates RegExp object", function() {
      var query = createQuery( "foo" );
      expect( query instanceof RegExp ).toBe( true );
    } );

    it( "generates queries that are case insensitive and start with new line", function() {
      var query = createQuery( "OoX" );

      expect( query.toString() ).toEqual( "/^OoX/i" );
    } );

    it( "escapes special regexp characters", function() {
      var query = createQuery( "^foo" );

      expect( query.toString() ).toEqual( "/^\\^foo/i" );
    } );

    it( "can generate query that is case sensitive", function() {
      var query = createQuery( "foo", true );

      expect( query.toString() ).toEqual( "/^foo/" );
    } );

    it( "can generate query that ignores position", function() {
      var query = createQuery( "foo", true, true );

      expect( query.toString() ).toEqual( "/foo/" );
    } );

  } );

  describe( "commonText", function() {

    var commonText;

    beforeEach( function() {
      if( !commonText ) {
        commonText = getVarFromScript( "AutoSuggest", "commonText" );
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

  describe( "searchItems", function() {

    var searchItems;

    beforeEach( function() {
      if( !searchItems ) {
        searchItems = getVarFromScript( "AutoSuggest", "searchItems" );
      }
    } );

    it( "exists", function() {
      expect( searchItems instanceof Function ).toBe( true );
    } );

    it( "returns empty result for empty array", function() {
      var results = searchItems( [], /foo/ );

      expect( results.items ).toEqual( [] );
      expect( results.indicies ).toEqual( [] );
    } );

    it( "returns multiple items", function() {
      var items = [ "afoo", "bar", "food", "abc" ];

      var results = searchItems( items, /foo/ );

      expect( results.items ).toEqual( [ "afoo", "food" ] );
      expect( results.indicies ).toEqual( [ 0, 2 ] );
    } );

    it( "returns multiple items with limit", function() {
      var items = [ "afoo", "bfoo", "x", "cfoo", "foor", "four" ];

      var results = searchItems( items, /foo/, 3 );

      expect( [ "afoo", "bfoo", "cfoo" ], results.items );
      expect( [ 0, 1, 3 ], results.indicies );
    } );

    it( "returns only items starting with string", function() {
      var items = [ "afoo", "bar", "food", "abc" ];
      var results = searchItems( items, /^foo/ );

      expect( results.items ).toEqual( [ "food" ] );
      expect( results.indicies ).toEqual( [ 2 ] );
    } );

  } );

  describe( "listener", function() {

    var model;
    var logger;
    var log;

    beforeEach( function() {
      rap = new RapMock();
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

      it( "sets suggestions to null", function() {
        model.addListener( "change:dataSourceId", createClientListener( "AutoSuggest" ) );
        model.set( "suggestions", [] );

        model.set( "dataSourceId", "fooId" );

        expect( model.get( "suggestions" ) ).toBeNull();
      } );

    } );

    describe( "change:userText", function() {

      it( "clears replacementText", function() {
        model.addListener( "change:userText", createClientListener( "AutoSuggest" ) );
        model.set( "replacementText", "banana" );

        model.set( "userText", "ba" );

        expect( model.get( "replacementText" ) ).toBeNull();
      } );

      it( "shows suggestions", function() {
        model.addListener( "change:userText", createClientListener( "AutoSuggest" ) );

        model.set( "userText", "ba" );

        expect( model.get( "suggestionsVisible" ) ).toBe( true );
      } );

      it( "hides results if text length is zero", function() {
        model.addListener( "change:userText", createClientListener( "AutoSuggest" ) );
        model.set( "suggestionsVisible", true );

        model.set( "userText", "" );

        expect( model.get( "suggestionsVisible" ) ).toBe( false );
      } );

      it( "updates currentSuggestions", function() {
        model.addListener( "change:userText", createClientListener( "AutoSuggest" ) );

        model.set( "userText", "ba" );

        expect( model.get( "currentSuggestions" ).items ).toEqual( [ "bar", "banana" ] );
      } );

      it( "updates results from data provider if suggestions is not set", function() {
        model.addListener( "change:userText", createClientListener( "AutoSuggest" ) );
        model.set( "suggestions", null );
        var dataSource = rap.typeHandler[ "rwt.remote.Model" ].factory();
        dataSource.set( "data", [ "foo", "bar", "foobar", "banana", "apple", "cherry" ] );
        spyOn( rap, "getObject" ).andReturn( dataSource );
        model.set( "dataSourceId", "fooId" );

        model.set( "userText", "ba" );

        expect( model.get( "currentSuggestions" ).items ).toEqual( [ "bar", "banana" ] );
      } );

      it( "forwards action option", function() {
        model.addListener( "change:userText", createClientListener( "AutoSuggest" ) );
        model.addListener( "change:currentSuggestions", logger );

        model.set( "userText", "ba", { "action" : "foo" } );

        expect( log[ 0 ][ 0 ].options.action ).toBe( "foo" );
      } );

    } );

    describe( "change:suggestions", function() {

      it( "clears suggestion", function() {
        model.addListener( "change:suggestions", createClientListener( "AutoSuggest" ) );
        model.set( "replacementText", "banana" );
        model.set( "suggestionsVisible", true );
        model.addListener( "change:replacementText", logger );

        model.set( "suggestions", [] );

        expect( model.get( "replacementText" ) ).toBeNull();
      } );

      it( "updates suggestions", function() {
        model.addListener( "change:suggestions", createClientListener( "AutoSuggest" ) );
        model.set( "userText", "ba" );
        model.set( "suggestionsVisible", true );

        model.set( "suggestions", [ "foo", "bar" ] );

        expect( model.get( "currentSuggestions" ).items ).toEqual( [ "bar" ] );
      } );

      it( "does not update suggestions if not visible", function() {
        model.addListener( "change:suggestions", createClientListener( "AutoSuggest" ) );
        model.set( "suggestionsVisible", false );
        model.addListener( "change:currentSuggestions", logger );

        model.set( "suggestions", [ "foo", "bar" ] );

        expect( log.length ).toBe( 0 );
      } );

      it( "sets suggestions with refresh option", function() {
        model.addListener( "change:suggestions", createClientListener( "AutoSuggest" ) );
        model.addListener( "change:currentSuggestions", logger );
        model.set( "suggestionsVisible", true );
        model.set( "userText", "ba" );

        model.set( "suggestions", [ "foo", "bar" ] );

        expect( log[ 0 ][ 0 ].options.action ).toBe( "refresh" );
      } );

    } );

    describe( "change:selectedSuggestionIndex", function() {

      it( "sets replacementText to selected suggestion", function() {
        model.addListener( "change:selectedSuggestionIndex", createClientListener( "AutoSuggest" ) );
        model.set( "currentSuggestions", { "items" : [ "bar", "banana" ] } );

        model.set( "selectedSuggestionIndex", 1 );

        expect( model.get( "replacementText" ) ).toEqual( "banana" );
      } );

      it( "resets suggestion when selection index is -1", function() {
        model.addListener( "change:selectedSuggestionIndex", createClientListener( "AutoSuggest" ) );
        model.set( "currentSuggestions", { "items" : [ "bar", "banana" ] } );
        model.set( "replacementText", "banana" );

        model.set( "selectedSuggestionIndex", -1 );

        expect( model.get( "replacementText" ) ).toBeNull();
      } );

      it( "sets action option", function() {
        model.addListener( "change:selectedSuggestionIndex", createClientListener( "AutoSuggest" ) );
        model.addListener( "change:replacementText", logger );
        model.set( "currentSuggestions", { "items" : [ "bar", "banana" ] } );

        model.set( "selectedSuggestionIndex", 1 );

        expect( log[ 0 ][ 0 ].options.action ).toBe( "selection" );
      } );

    } );

    describe( "change:replacementText", function() {

      it( "ignores events from change:userText", function() {
        model.addListener( "change:replacementText", createClientListener( "AutoSuggest" ) );
        model.set( "text", "bar" );
        model.set( "textSelection", [ 0, 0 ] );

        model.set( "replacementText", "foo", { "action" : "sync" } );

        expect( model.get( "text" ) ).toEqual( "bar" );
        expect( model.get( "textSelection" ) ).toEqual( [ 0, 0 ] );
      } );

      it( "sets text to suggestion", function() {
        model.addListener( "change:replacementText", createClientListener( "AutoSuggest" ) );

        model.set( "replacementText", "foo" );

        expect( model.get( "text" ) ).toEqual( "foo" );
      } );

      it( "sets textSelection for replacementText", function() {
        model.addListener( "change:replacementText", createClientListener( "AutoSuggest" ) );

        model.set( "replacementText", "foo", { "action" : "selection" } );

        expect( model.get( "textSelection" ) ).toEqual( [ 0, 3 ] );
      } );

      it( "sets textSelection for auto complete", function() {
        model.addListener( "change:replacementText", createClientListener( "AutoSuggest" ) );
        model.set( "userText", "foo" );

        model.set( "replacementText", "foobar" );

        expect( model.get( "textSelection" ) ).toEqual( [ 3, 6 ] );
      } );

      it( "resets text to userText", function() {
        model.addListener( "change:replacementText", createClientListener( "AutoSuggest" ) );
        model.set( "userText", "bar" );

        model.set( "replacementText", null, { "action" : "selection" } );

        expect( model.get( "text" ) ).toEqual( "bar" );
      } );

      it( "resets selection to userText end", function() {
        model.addListener( "change:replacementText", createClientListener( "AutoSuggest" ) );
        model.set( "userText", "bar" );

        model.set( "replacementText", null, { "action" : "selection" } );

        expect( model.get( "textSelection" ) ).toEqual( [ 3, 3 ] );
      } );

    } );

    describe( "change:currentSuggestions", function() {

      it( "does nothing without autocomplete", function() {
        model.set( "replacementText", "ban" );
        model.addListener( "change:currentSuggestions", createClientListener( "AutoSuggest" ) );

        model.set( "currentSuggestions", { "items" : [ "banana" ] } );

        expect( model.get( "replacementText" ) ).toEqual( "ban" );
      } );

      it( "does nothing if not typing", function() {
        model.set( "replacementText", "ban" );
        model.addListener( "change:currentSuggestions", createClientListener( "AutoSuggest" ) );
        model.set( "autoComplete", true );

        model.set( "currentSuggestions", { "items" : [ "banana" ] } );

        expect( model.get( "replacementText" ) ).toEqual( "ban" );
      } );

      it( "autocompletes text while typing on single currentSuggestion", function() {
        model.set( "replacementText", "ban" );
        model.set( "userText", "b" );
        model.set( "autoComplete", true );
        model.addListener( "change:currentSuggestions", createClientListener( "AutoSuggest" ) );

        model.set( "currentSuggestions", { "items" : [ "banana" ] }, { "action" : "typing" } );

        expect( model.get( "replacementText" ) ).toEqual( "banana" );
      } );

      it( "autocompletes suggestion while refreshing on single currentSuggestion", function() {
        model.set( "replacementText", "ban" );
        model.set( "userText", "b" );
        model.set( "autoComplete", true );
        model.addListener( "change:currentSuggestions", createClientListener( "AutoSuggest" ) );

        model.set( "currentSuggestions", { "items" : [ "banana" ] }, { "action" : "refresh" } );

        expect( model.get( "replacementText" ) ).toEqual( "banana" );
      } );

      it( "partially autocompletes suggestion for common text", function() {
        model.set( "autoComplete", true );
        model.set( "userText", "b" );
        model.addListener( "change:currentSuggestions", createClientListener( "AutoSuggest" ) );

        var items = [ "banana foo", "banana bar" ];
        model.set( "currentSuggestions", { "items" : items }, { "action" : "typing" } );

        expect( model.get( "replacementText" ) ).toEqual( "banana " );
      } );

      it( "does not autocomplete if common text is shorter than userText", function() {
        model.set( "replacementText", null );
        model.set( "autoComplete", true );
        model.set( "userText", "banana xxx" );
        model.addListener( "change:currentSuggestions", createClientListener( "AutoSuggest" ) );

        var items = [ "banana foo", "banana bar" ];
        model.set( "currentSuggestions", { "items" : items }, { "action" : "typing" } );

        expect( model.get( "replacementText" ) ).toBe( null );
      } );

    } );

    describe( "accept", function() {

      it( "fires suggestionSelected for selectedSuggestionIndex", function() {
        model.addListener( "accept", createClientListener( "AutoSuggest" ) );
        model.set( "currentSuggestions", { "items" : [ "bar", "banana" ], "indicies" : [ 1, 3 ] } );
        model.set( "selectedSuggestionIndex", 1 );
        model.addListener( "suggestionSelected", logger );

        model.notify( "accept", { source : model, type : "accept" } );

        expect( log.length ).toBe( 1 );
        expect( model.get( "suggestionsVisible" ) ).toBe( false );
      } );

      it( "fires suggestionSelected when full auto complete is accepted", function() {
        model.addListener( "accept", createClientListener( "AutoSuggest" ) );
        model.set( "currentSuggestions", { "items" : [ "banana" ], "indicies" : [ 3 ] } );
        model.set( "selectedSuggestionIndex", -1 );
        model.set( "autoComplete", true );
        model.addListener( "suggestionSelected", logger );

        model.notify( "accept", { source : model, type : "accept" } );

        expect( log.length ).toBe( 1 );
        expect( model.get( "suggestionsVisible" ) ).toBe( false );
      } );

      it( "does nothing when attempting accepting without selected suggestion or auto complete", function() {
        model.addListener( "accept", createClientListener( "AutoSuggest" ) );
        model.set( "currentSuggestions", { "items" : [ "banana" ], "indicies" : [ 3 ] } );
        model.set( "selectedSuggestionIndex", -1 );
        model.set( "suggestionsVisible", true );
        model.addListener( "suggestionSelected", logger );

        model.notify( "accept", { source : model, type : "accept" } );

        expect( log.length ).toBe( 0 );
        expect( model.get( "suggestionsVisible" ) ).toBe( true );
      } );

      it( "clears text selection", function() {
        model.addListener( "accept", createClientListener( "AutoSuggest" ) );
        model.set( "text", "foobar" );

        model.notify( "accept", { source : model, type : "accept" } );

        expect( model.get( "textSelection" ) ).toEqual( [ 6, 6 ] );
      } );

    } );

  } );

 } );
