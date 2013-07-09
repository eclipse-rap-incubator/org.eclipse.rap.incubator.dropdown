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

  function createClientListener( name ) {
    // NOTE : Using + "" to convert Java string to JavaScript string. Alternatives?
    var listenerScript = TestUtil.getResource( name ) + "";
    var listener = new org.eclipse.rap.clientscripting.Function( listenerScript );
    return function() {
      listener.call.apply( listener, arguments );
    };
  }

  var model;
  var rap;

  beforeEach( function() {
    rap = new RapMock();
    model = rap.typeHandler[ "rwt.remote.Model" ].factory();
    model.addListener( "refresh", createClientListener( "ModelListener" ) );
  } );

  afterEach( function() {
    model.destroy();
  } );

  it( "listens on refresh", function() {
    var error =  null;
    try {
      model.notify( "refresh", {} );
    } catch( ex ) {
      error = ex;
    }
    expect( error ).not.toBeNull();
  } );

} );
