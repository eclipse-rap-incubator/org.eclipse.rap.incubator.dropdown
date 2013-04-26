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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.rap.rwt.internal.remote.RemoteObjectImpl;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;


@SuppressWarnings("restriction")
public class UniversalRemoteObject_Test {

  private static final String REMOTE_TYPE = "rwt.remote.UniversalRemoteObject";
  private Connection connection;
  private RemoteObjectImpl remoteObject;
  private UniversalRemoteObject uro;
  private OperationHandler operationHandler;

  @Before
  public void setUp() {
    Fixture.setUp();
    Fixture.fakeNewRequest();
    connection = mock( Connection.class );
    Fixture.fakeConnection( connection );
    remoteObject = mock( RemoteObjectImpl.class );
    doAnswer( new Answer< Object >() {
        public Object answer( InvocationOnMock invocation ) {
            Object[] args = invocation.getArguments();
            operationHandler = ( OperationHandler )args[ 0 ];
            return null;
        } }
    ).when( remoteObject ).setHandler( any( OperationHandler.class ) );
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
    // TODO [tb] : Also test handling notify operations
    uro.set( "myBool", false );
    uro.set( "myDouble", 1000100.101 );
    uro.set( "myInt", 34 );
    uro.set( "myObject", new Object[ 0 ] );
    uro.set( "myString", "yourString" );

    assertFalse( uro.getBoolean( "myBool" ) );
    assertTrue( 1000100.101 == uro.getDouble( "myDouble" ) );
    assertEquals( 34, uro.getInt( "myInt" ) );
    assertEquals( 0, ( ( Object[] )uro.get( "myObject" ) ).length );
    assertEquals( "yourString", uro.getString( "myString" ) );
  }

  @Test
  public void testGetAfterDestroyCrashs() {
    uro.set( "myString", "yourString" );

    markDestroyed();

    try {
      uro.getString( "myString" );
      fail();
    } catch( IllegalStateException ex ) {
     // expected
    }
  }

  @Test
  public void testIsDestroyedCallsThrough() {
    markDestroyed();

    assertTrue( uro.isDestroyed() );
  }

  @Test
  public void testHandleNotifyOperation() {
    final List< Map< String, Object > > log = new ArrayList< Map< String, Object > >();
    uro.setHandler( new AbstractOperationHandler() {
      @Override
      public void handleNotify( String event, Map<String, Object> properties ) {
        if( event.equals( "Selection" ) ) {
          log.add( properties );
        }
      }
    } );

    Map< String, Object > properties = new HashMap< String, Object >();
    properties.put( "index", new Integer( 23 ) );
    operationHandler.handleNotify( "Selection", properties );

    assertEquals( 1, log.size() );
    assertEquals( new Integer( 23 ), log.get( 0 ).get( "index" ) );
  }

  @Test
  public void testHandleNotifyLocalCall() {
    final List< Map< String, Object > > log = new ArrayList< Map< String, Object > >();
    uro.setHandler( new AbstractOperationHandler() {
      @Override
      public void handleNotify( String event, Map<String, Object> properties ) {
        if( event.equals( "Selection" ) ) {
          log.add( properties );
        }
      }
    } );

    Map< String, Object > properties = new HashMap< String, Object >();
    properties.put( "index", new Integer( 23 ) );
    uro.notify( "Selection", properties );

    assertEquals( 1, log.size() );
    assertEquals( new Integer( 23 ), log.get( 0 ).get( "index" ) );
  }

  private void markDestroyed() {
    doAnswer( new Answer< Boolean >() {
      public Boolean answer( InvocationOnMock invocation ) {
          return Boolean.TRUE;
      }
    } ).when( remoteObject ).isDestroyed();
  }

}
