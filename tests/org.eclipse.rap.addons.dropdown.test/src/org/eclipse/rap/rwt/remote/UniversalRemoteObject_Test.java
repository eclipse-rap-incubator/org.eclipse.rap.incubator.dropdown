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

package org.eclipse.rap.rwt.remote;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.rap.rwt.internal.remote.RemoteObjectImpl;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


@SuppressWarnings("restriction")
public class UniversalRemoteObject_Test {

  private static final String REMOTE_TYPE = "rwt.remote.UniversalRemoteObject";
  private Connection connection;
  private RemoteObjectImpl remoteObject;
  private UniversalRemoteObject uro;

  @Before
  public void setUp() {
    Fixture.setUp();
    Fixture.fakeNewRequest();
    connection = mock( Connection.class );
    Fixture.fakeConnection( connection );
    remoteObject = mock( RemoteObjectImpl.class );
    when( connection.createRemoteObject( eq( REMOTE_TYPE ) ) ).thenReturn( remoteObject );
    uro = new UniversalRemoteObject();
  }

  @After
  public void tearDown() {
    Fixture.tearDown();
  }

  @Test
  public void testContructor_CreatesRealRemoteObject() {
    verify( connection ).createRemoteObject( eq( REMOTE_TYPE ) );
  }

  @Test
  public void testCallThrough_GetId() {
    when( remoteObject.getId() ).thenReturn( "r11" );

    assertEquals( "r11", uro.getId() );
  }

  @Test
  public void testCallThrough_Destroy() {
    uro.destroy();

    verify( remoteObject ).destroy();
  }

  @Test
  public void testCallThrough_Set() {
    uro.set( "myBool", false );
    uro.set( "myDouble", 1000100.101 );
    uro.set( "myInt", 34 );
    uro.set( "myObject", new Object[ 0 ] );
    uro.set( "myString", "yourString" );

    verify( remoteObject ).set( eq( "myBool" ), eq( false ) );
    verify( remoteObject ).set( eq( "myDouble" ), eq( 1000100.101 ) );
    verify( remoteObject ).set( eq( "myInt" ), eq( 34 ) );
    verify( remoteObject ).set( eq( "myObject" ), eq( new Object[ 0 ] ) );
    verify( remoteObject ).set( eq( "myString" ), eq( "yourString" ) );
  }

  @Test
  public void testCallThrough_Listen() {
    uro.listen( "Selection", true );

    verify( remoteObject ).listen( eq( "Selection" ), eq( true ) );
  }

  @Test
  public void testCall_NotSupported() {
    try {
      uro.call( "foo", null );
      fail();
    } catch( UnsupportedOperationException ex ) {
      // expected
    }
  }

  @Test
  public void testGet() {
    uro.set( "myBool", false );
    uro.set( "myDouble", 1000100.101 );
    uro.set( "myInt", 34 );
    uro.set( "myObject", new Object[ 0 ] );
    uro.set( "myString", "yourString" );

    assertEquals( false, uro.getBoolean( "myBool" ) );
    assertTrue( 1000100.101 == uro.getDouble( "myDouble" ) );
    assertEquals( 34, uro.getInt( "myInt" ) );
    assertEquals( 0, ( ( Object[] )uro.get( "myObject" ) ).length );
    assertEquals( "yourString", uro.getString( "myString" ) );
  }

}
