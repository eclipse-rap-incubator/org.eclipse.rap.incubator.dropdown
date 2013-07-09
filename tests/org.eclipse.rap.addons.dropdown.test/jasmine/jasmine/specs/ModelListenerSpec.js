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

  var model;
  var rap;

  beforeEach( function() {
    rap = new RapMock();
    model = rap.typeHandler[ "rwt.remote.Model" ].factory();
  } );

  afterEach( function() {
    model.destroy();
  } );

  it( "runs", function() {
    //console.log( TestUtil.getResource( "ModelListener" ) );
    expect( true ).toBe( true );
  } );

} );
