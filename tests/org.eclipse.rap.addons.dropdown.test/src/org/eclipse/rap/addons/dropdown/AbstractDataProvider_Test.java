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
package org.eclipse.rap.addons.dropdown;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.rwt.remote.Connection;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.junit.*;


public class AbstractDataProvider_Test {

  private static final String REMOTE_TYPE = "rwt.remote.Model";

  private Connection connection;

  private RemoteObject remoteObject;

  @Before
  public void setUp() {
    Fixture.setUp();
    connection = mock( Connection.class );
    remoteObject = mock( RemoteObject.class );
    when( connection.createRemoteObject( anyString() ) ).thenReturn( remoteObject );
    when( remoteObject.getId() ).thenReturn( "idFoo" );
    Fixture.fakeConnection( connection );
  }

  @After
  public void tearDown() {
    Fixture.tearDown();
  }

  @Test
  public void testConstructor_createsRemoteObject() {
    new AbstractDataProvider(){};

    verify( connection ).createRemoteObject( eq( REMOTE_TYPE ) );
  }

  @Test
  public void testGetId() {
    AbstractDataProvider dataProvider = new AbstractDataProvider(){};
    assertEquals( "idFoo", dataProvider.getId() );
  }

  @Test ( expected = NullPointerException.class )
  public void testSetData_failsWithNullArgument() {
    AbstractDataProvider dataProvider = new AbstractDataProvider(){};

    dataProvider.setData( null );
  }

  @Test
  public void testSetData_setsDataOnRemoteObject() {
    JsonArray array = new JsonArray().add( "foo" ).add( "bar" );
    AbstractDataProvider dataProvider = new AbstractDataProvider(){};

    dataProvider.setData( array );

    verify( remoteObject ).set( eq( "data" ), eq( array ) );
  }

}
