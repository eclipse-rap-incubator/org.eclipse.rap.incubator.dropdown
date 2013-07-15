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

 describe( "ModelListener", function() {

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
        createQuery = getVarFromScript( "ModelListener", "createQuery" );
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

  describe( "searchItems", function() {

    var searchItems;

    beforeEach( function() {
      if( !searchItems ) {
        searchItems = getVarFromScript( "ModelListener", "searchItems" );
      }
    } );

    it( "exists", function() {
      expect( searchItems instanceof Function ).toBe( true );
    } );

    it( "returns empty result for empty array", function() {
      var results = searchItems( [], /foo/ );

      expect( results.query.toString() ).toEqual( "/foo/" );
      expect( results.items ).toEqual( [] );
      expect( results.indicies ).toEqual( [] );
    } );

    it( "returns multiple items", function() {
      var items = [ "afoo", "bar", "food", "abc" ];

      var results = searchItems( items, /foo/ );

      expect( results.query.toString() ).toBe( "/foo/" );
      expect( results.items ).toEqual( [ "afoo", "food" ] );
      expect( results.indicies ).toEqual( [ 0, 2 ] );
      expect( results.limit ).toBe( 0 );
    } );

    it( "returns multiple items with limit", function() {
      var items = [ "afoo", "bfoo", "x", "cfoo", "foor", "four" ];

      var results = searchItems( items, /foo/, 3 );

      expect( results.query.toString() ).toBe( "/foo/" );
      expect( [ "afoo", "bfoo", "cfoo" ], results.items );
      expect( [ 0, 1, 3 ], results.indicies );
      expect( 3, results.limit );
    } );

    it( "returns only items starting with string", function() {
      var items = [ "afoo", "bar", "food", "abc" ];
      var results = searchItems( items, /^foo/ );

      expect( results.query.toString() ).toBe( "/^foo/" );
      expect( results.items ).toEqual( [ "food" ] );
      expect( results.indicies ).toEqual( [ 2 ] );
    } );

  } );

  describe( "listener", function() {

    var model;
    var rap;
    var logger;
    var log;

    beforeEach( function() {
      rap = new RapMock();
      model = rap.typeHandler[ "rwt.remote.Model" ].factory();
      model.set( "elements", [ "foo", "bar", "foobar", "banana", "apple", "cherry" ] );
      log = [];
      logger = function() {
        log.push( arguments );
      };
    } );

    afterEach( function() {
      model.destroy();
    } );

    describe( "change:userText", function() {

      it( "clears suggestion", function() {
        model.addListener( "change:userText", createClientListener( "ModelListener" ) );
        model.set( "suggestion", "banana" );
        model.addListener( "change:suggestion", logger );

        model.set( "userText", "ba" );

        expect( model.get( "suggestion" ) ).toBeNull();
      } );

      it( "shows results", function() {
        model.addListener( "change:userText", createClientListener( "ModelListener" ) );

        model.set( "userText", "ba" );

        expect( model.get( "resultsVisible" ) ).toBe( true );
      } );

      it( "updates results", function() {
        model.addListener( "change:userText", createClientListener( "ModelListener" ) );

        model.set( "userText", "ba" );

        expect( model.get( "results" ).items ).toEqual( [ "bar", "banana" ] );
      } );

      it( "forwards action option", function() {
        model.addListener( "change:userText", createClientListener( "ModelListener" ) );
        model.addListener( "change:results", logger );

        model.set( "userText", "ba", { "action" : "foo" );

        expect( log[ 0 ][ 0 ].options.action ).toBe( "foo" );
      } );

    } );

    describe( "change:resultSelection", function() {

      it( "sets suggestion to selected result", function() {
        model.addListener( "change:resultSelection", createClientListener( "ModelListener" ) );
        model.set( "results", { "items" : [ "bar", "banana" ] } );

        model.set( "resultSelection", 1 );

        expect( model.get( "suggestion" ) ).toEqual( "banana" );
      } );

      it( "resets suggestion when selection index is -1", function() {
        model.addListener( "change:resultSelection", createClientListener( "ModelListener" ) );
        model.set( "results", { "items" : [ "bar", "banana" ] } );
        model.set( "suggestion", "banana" );

        model.set( "resultSelection", -1 );

        expect( model.get( "suggestion" ) ).toBeNull();
      } );

      it( "sets action option", function() {
        model.addListener( "change:resultSelection", createClientListener( "ModelListener" ) );
        model.addListener( "change:suggestion", logger );
        model.set( "results", { "items" : [ "bar", "banana" ] } );

        model.set( "resultSelection", 1 );

        expect( log[ 0 ][ 0 ].options.action ).toBe( "selection" );
      } );

    } );

    describe( "change:suggestion", function() {

      it( "ignores events from change:userText", function() {
        model.addListener( "change:suggestion", createClientListener( "ModelListener" ) );
        model.set( "text", "bar" );
        model.set( "textSelection", [ 0, 0 ] );

        model.set( "suggestion", "foo", { "action" : "sync" } );

        expect( model.get( "text" ) ).toEqual( "bar" );
        expect( model.get( "textSelection" ) ).toEqual( [ 0, 0 ] );
      } );

      it( "sets text to suggestion", function() {
        model.addListener( "change:suggestion", createClientListener( "ModelListener" ) );

        model.set( "suggestion", "foo" );

        expect( model.get( "text" ) ).toEqual( "foo" );
      } );

      it( "sets textSelection for result selection", function() {
        model.addListener( "change:suggestion", createClientListener( "ModelListener" ) );

        model.set( "suggestion", "foo", { "action" : "selection" } );

        expect( model.get( "textSelection" ) ).toEqual( [ 0, 3 ] );
      } );

      it( "sets textSelection for auto complete", function() {
        model.addListener( "change:suggestion", createClientListener( "ModelListener" ) );
        model.set( "userText", "foo" );

        model.set( "suggestion", "foobar" );

        expect( model.get( "textSelection" ) ).toEqual( [ 3, 6 ] );
      } );

      it( "resets text to userText", function() {
        model.addListener( "change:suggestion", createClientListener( "ModelListener" ) );
        model.set( "userText", "bar" );

        model.set( "suggestion", null, { "action" : "selection" } );

        expect( model.get( "text" ) ).toEqual( "bar" );
      } );

      it( "resets selection to userText end", function() {
        model.addListener( "change:suggestion", createClientListener( "ModelListener" ) );
        model.set( "userText", "bar" );

        model.set( "suggestion", null, { "action" : "selection" } );

        expect( model.get( "textSelection" ) ).toEqual( [ 3, 3 ] );
      } );

    } );

    describe( "change:results", function() {

      it( "does nothing without autocomplete", function() {
        model.set( "suggestion", "ban" );
        model.addListener( "change:results", createClientListener( "ModelListener" ) );

        model.set( "results", { "items" : [ "banana" ] } );

        expect( model.get( "suggestion" ) ).toEqual( "ban" );
      } );

      it( "does nothing if not typing", function() {
        model.set( "suggestion", "ban" );
        model.addListener( "change:results", createClientListener( "ModelListener" ) );
        model.set( "autoComplete", true );

        model.set( "results", { "items" : [ "banana" ] } );

        expect( model.get( "suggestion" ) ).toEqual( "ban" );
      } );

      it( "autocompletes suggestion on single result", function() {
        model.set( "suggestion", "ban" );
        model.set( "autoComplete", true );
        model.addListener( "change:results", createClientListener( "ModelListener" ) );

        model.set( "results", { "items" : [ "banana" ] }, { "action" : "typing" } );

        expect( model.get( "suggestion" ) ).toEqual( "banana" );
      } );

    } );

    describe( "accept", function() {

      it( "sets elementSelection for resultSelection", function() {
        model.addListener( "accept", createClientListener( "ModelListener" ) );
        model.set( "results", { "items" : [ "bar", "banana" ], "indicies" : [ 1, 3 ] } );
        model.set( "resultSelection", 1 );

        model.notify( "accept", { source : model, type : "accept" } );

        expect( model.get( "elementSelection" ) ).toBe( 3 );
      } );

      it( "sets elementSelection for single result and autoComplete", function() {
        model.addListener( "accept", createClientListener( "ModelListener" ) );
        model.set( "results", { "items" : [ "banana" ], "indicies" : [ 3 ] } );
        model.set( "resultSelection", -1 );
        model.set( "autoComplete", true );

        model.notify( "accept", { source : model, type : "accept" } );

        expect( model.get( "elementSelection" ) ).toBe( 3 );
      } );

      it( "does nothin for single result without autoComplete", function() {
        model.addListener( "accept", createClientListener( "ModelListener" ) );
        model.set( "results", { "items" : [ "banana" ], "indicies" : [ 3 ] } );
        model.set( "resultSelection", -1 );
        model.set( "elementSelection", 0 );

        model.notify( "accept", { source : model, type : "accept" } );

        expect( model.get( "elementSelection" ) ).toBe( 0 );
      } );

      it( "clears text selection", function() {
        model.addListener( "accept", createClientListener( "ModelListener" ) );
        model.set( "text", "foobar" );

        model.notify( "accept", { source : model, type : "accept" } );

        expect( model.get( "textSelection" ) ).toEqual( [ 6, 6 ] );
      } );

    } );

    describe( "change:elementSelection", function() {

      it( "hides results", function() {
        model.addListener( "change:elementSelection", createClientListener( "ModelListener" ) );
        model.set( "resultsVisible", true );

        model.set( "elementSelection", 22 );

        expect( model.get( "resultsVisible" ) ).toBe( false );
      } );

    } );


  } );

 } );
