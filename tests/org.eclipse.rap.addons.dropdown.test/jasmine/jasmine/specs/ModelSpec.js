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

 describe( "rwt.remote.Model", function() {

  var model;
  var log;
  var logger;

  beforeEach( function() {
    model = rap.typeHandler[ "rwt.remote.Model" ].factory();
    log = [];
    logger = function() {
      log.push( arguments );
    };
  } );

  afterEach( function() {
    model.destroy();
  } );

  describe( "addListener", function() {

    it( "ignores multiple registrations of same listener", function() {
      model.addListener( "Selection", logger );
      model.addListener( "Selection", logger );

      model.notify( "Selection", { "foo" : "bar" } );

      expect( log.length ).toBe( 1 );
    } );

  } );

  describe( "removeListener", function() {

    it( "de-registers listener", function() {
      model.addListener( "Selection", logger );

      model.removeListener( "Selection", logger );
      model.notify( "Selection", { "foo" : "bar" } );

      expect( log.length ).toBe( 0 );
    } );

    it( "works after model is destroyed", function() {
      model.addListener( "Selection", logger );
      model.destroy();

      model.removeListener( "Selection", logger );
      model.notify( "Selection", { "foo" : "bar" } );

      expect( log.length ).toBe( 0 );
    } );

  } );

  describe( "notify", function() {

    it( "notifies listener when called with event object", function() {
      model.addListener( "Selection", logger );

      model.notify( "Selection", { "foo" : "bar" } );

      expect( log.length ).toBe( 1 );
    } );

  } );

  describe( "set", function() {

    it( "notifies change listener", function() {
      model.addListener( "change:foo", logger );

      model.set( "foo", 23 );

      expect( log.length ).toBe( 1 );
    } );

    it( "notifies change listener with event", function() {
      model.addListener( "change:foo", logger );

      model.set( "foo", 23 );

      var event = log[ 0 ][ 0 ];
      expect( event.type ).toBe( "change" );
      expect( event.property ).toBe( "foo" );
      expect( event.value ).toBe( 23 );
      expect( event.options ).toEqual( {} );
    } );

    it( "notifies change listener with options", function() {
      model.addListener( "change:foo", logger );

      model.set( "foo", 23, { x : 1 } );

      expect( log[ 0 ][ 0 ].options ).toEqual( { x : 1 } );
    } );

    it( "notifies global change listener", function() {
      model.addListener( "change", logger );

      model.set( "foo", 23 );
      model.set( "bar", 25 );

      expect( log.length ).toBe( 2 );
    } );

    it( "does not notify change listener if value is unchanged", function() {
      model.addListener( "change:foo", logger );

      model.set( "foo", 23 );
      model.set( "foo", 23 );

      expect( log.length ).toBe( 1 );
    } );

  } );

} );
