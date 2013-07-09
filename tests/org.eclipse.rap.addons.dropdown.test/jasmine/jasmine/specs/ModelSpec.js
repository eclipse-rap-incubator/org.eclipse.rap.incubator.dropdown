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

    it( "notifies client listener for Selection", function() {
      model.addListener( "Selection", logger );

      model.notify( "Selection", { "foo" : "bar" } );

      expect( log.length ).toBe( 1 );
      expect( log[ 0 ][ 0 ] ).toBe( model );
      expect( log[ 0 ][ 1 ] ).toEqual( { "foo" : "bar" }  );
    } );

    it( "notifies client listener without properties argument", function() {
      model.addListener( "Selection", logger );

      model.notify( { "event" : "Selection", "nosync" : true } );

      expect( log[ 0 ].length ).toBe( 1 );
      expect( log[ 0 ][ 0 ] ).toBe( model );
    } );

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

} );
