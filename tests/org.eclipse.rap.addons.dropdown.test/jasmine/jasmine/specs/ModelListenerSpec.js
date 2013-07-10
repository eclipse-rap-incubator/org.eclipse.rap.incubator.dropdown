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

  describe( "change:text listener", function() {

    var model;
    var rap;

    beforeEach( function() {
      rap = new RapMock();
      model = rap.typeHandler[ "rwt.remote.Model" ].factory();
      model.set( "elements", [ "foo", "bar", "foobar", "banana", "apple", "cherry" ] );
      model.addListener( "change:text", createClientListener( "ModelListener" ) );
    } );

    afterEach( function() {
      model.destroy();
    } );

    it( "updates results", function() {
      model.set( "text", "ba" );

      var results = model.get( "results" );
      expect( results.items ).toEqual( [ "bar", "banana" ] );
    } );

    it( "ignores resultSelection", function() {
      model.set( "text", "ba" );
      model.set( "text", "bar", { "resultSelection" : true } );

      var results = model.get ( "results" );
      expect( results.items ).toEqual( [ "bar", "banana" ] );
    } );

  } );

  describe( "change:resultSelection listener", function() {

    var model;
    var rap;
    var logger;
    var log;

    beforeEach( function() {
      rap = new RapMock();
      model = rap.typeHandler[ "rwt.remote.Model" ].factory();
      model.set( "elements", [ "foo", "bar", "foobar", "banana", "apple", "cherry" ] );
      model.addListener( "change:resultSelection", createClientListener( "ModelListener" ) );
      log = [];
      logger = function() {
        log.push( arguments );
      };
    } );

    afterEach( function() {
      model.destroy();
    } );

    it( "sets text to selected result", function() {
      model.set( "results", { "items" : [ "bar", "banana" ] } );

      model.set( "resultSelection", 1 );

      expect( model.get( "text" ) ).toEqual( "banana" );
    } );

    it( "sets text with option flag", function() {
      model.set( "results", { "items" : [ "bar", "banana" ] } );
      model.addListener( "change:text", logger );

      model.set( "resultSelection", 1 );

      expect( log[ 0 ][ 2 ] ).toEqual( { "resultSelection" : true } );
    } );

  } );

 } );

