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
package org.eclipse.rap.addons.autosuggest.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.rap.addons.autosuggest.internal.ClientModelListener;
import org.eclipse.rap.addons.autosuggest.internal.Model;
import org.eclipse.rap.addons.autosuggest.internal.ModelListener;
import org.eclipse.rap.clientscripting.internal.ClientListenerBinding;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.remote.Connection;
import org.eclipse.rap.rwt.remote.OperationHandler;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;


@SuppressWarnings("restriction")
public class Model_Test {

  private static final String REMOTE_TYPE = "rwt.remote.Model";
  private RemoteObject remoteObject;
  private OperationHandler handler;
  private Model model;
  private Connection connection;

  @Before
  public void setUp() {
    Fixture.setUp();
    Fixture.fakeNewRequest();
    remoteObject = mock( RemoteObject.class );
    when( remoteObject.getId() ).thenReturn( "modelId" );
    connection = spy( RWT.getUISession().getConnection() );
    when( connection.createRemoteObject( REMOTE_TYPE ) ).thenReturn( remoteObject );
    Fixture.fakeConnection( connection );
    doAnswer( new Answer<Object>(){
      public Object answer( InvocationOnMock invocation ) throws Throwable {
        handler = ( OperationHandler )invocation.getArguments()[ 0 ];
        return null;
      }
    } ).when( remoteObject ).setHandler( any( OperationHandler.class ) );
    model = new Model();
  }

  @After
  public void tearDown() {
    Fixture.tearDown();
  }

  @Test
  public void testConstructor_CreatesRemoteObject() {
    verify( connection ).createRemoteObject( eq( REMOTE_TYPE ) );
  }

  @Test
  public void testGetId() {
    when( remoteObject.getId() ).thenReturn( "foo" );

    assertEquals( "foo", model.getId() );
  }

  @Test
  public void testDispose_CallsRemoteObjectDestroy() {
    model.dispose();

    verify( remoteObject ).destroy();
  }

  @Test
  public void testDispose_callTwiceHasNoEffect() {
    model.dispose();
    model.dispose();

    verify( remoteObject, times( 1 ) ).destroy();
  }

  @Test
  public void testIsDisposed_returnsFalse() {
    assertFalse( model.isDisposed() );
  }

  @Test
  public void testIsDisposed_returnsTrueAfterDispose() {
    model.dispose();

    assertTrue( model.isDisposed() );
  }

  @Test
  public void testDispose_DisposesListenerBinding() {
    ClientModelListener listener = new ClientModelListener( "" );
    model.addListener( "foo", listener );
    ClientListenerBinding binding = listener.findBinding( model, "foo" );

    model.dispose();

    assertTrue( binding.isDisposed() );
  }

  @Test
  public void testSet_CallsRemoteObjectSetString() {
    model.set( "foo", "bar" );

    verify( remoteObject ).set( eq( "foo" ), eq( "bar" ) );
  }

  @Test
  public void testSet_CallsRemoteObjectSetInt() {
    model.set( "foo", 1 );

    verify( remoteObject ).set( eq( "foo" ), eq( 1 ) );
  }

  @Test
  public void testSet_CallsRemoteObjectSetJsonValue() {
    model.set( "foo", JsonValue.valueOf( "bar" ) );

    verify( remoteObject ).set( eq( "foo" ), eq( JsonValue.valueOf( "bar" ) ) );
  }

  @Test
  public void testAddListener_SetsListenTrue() {
    model.addListener( "foo", mock( ModelListener.class ) );

    verify( remoteObject ).listen( eq( "foo" ), eq( true ) );
  }

  @Test
  public void testAddListener_ThrowsExceptionForNullListener() {
    try{
      model.addListener( "foo", null );
      fail();
    } catch( IllegalArgumentException ex ) {
      // expected
    }

    verify( remoteObject, never() ).listen( anyString(), anyBoolean() );
  }
  @Test
  public void testAddListener_ThrowsExceptionForNullType() {
    try{
      model.addListener( null, mock( ModelListener.class ) );
      fail();
    } catch( IllegalArgumentException ex ) {
      // expected
    }

    verify( remoteObject, never() ).listen( anyString(), anyBoolean() );
  }

  @Test
  public void testAddListener_DoesNotSetListenTrueIfNotFirstListener() {
    model.addListener( "foo", mock( ModelListener.class ) );
    reset( remoteObject );

    model.addListener( "foo", mock( ModelListener.class ) );

    verify( remoteObject, never() ).listen( eq( "foo" ), eq( true ) );
  }

  @Test
  public void testAddListener_SetsListenTrueForDifferentEventType() {
    model.addListener( "foo", mock( ModelListener.class ) );
    reset( remoteObject );

    model.addListener( "bar", mock( ModelListener.class ) );

    verify( remoteObject ).listen( eq( "bar" ), eq( true ) );
  }

  @Test
  public void testAddListener_DoesNotSetListenTrueIfClientListener() {
    model.addListener( "foo", new ClientModelListener( "" ) );

    verify( remoteObject, never() ).listen( eq( "foo" ), eq( true ) );
  }

  @Test
  public void testAddListener_CreatesListenerBinding() {
    ClientModelListener listener = new ClientModelListener( "" );

    model.addListener( "foo", listener );

    assertNotNull( listener.findBinding( model, "foo" ) );
  }

  @Test
  public void testRemoveListener_SetsListenFalse() {
    ModelListener listener = mock( ModelListener.class );
    model.addListener( "foo", listener );
    reset( remoteObject );

    model.removeListener( "foo", listener );

    verify( remoteObject ).listen( eq( "foo" ), eq( false ) );
  }

  @Test
  public void testRemoveListener_DoesNotSetListenFalseIfListenerRemains() {
    ModelListener listener = mock( ModelListener.class );
    model.addListener( "foo", listener );
    model.addListener( "foo", mock( ModelListener.class ) );
    reset( remoteObject );

    model.removeListener( "foo", listener );

    verify( remoteObject, never() ).listen( eq( "foo" ), eq( false ) );
  }

  @Test
  public void testRemoveListener_DoesSetListenFalseIfDifferentEventTypeRemains() {
    ModelListener listener = mock( ModelListener.class );
    model.addListener( "bar", listener );
    model.addListener( "foo", mock( ModelListener.class ) );
    reset( remoteObject );

    model.removeListener( "bar", listener );

    verify( remoteObject ).listen( eq( "bar" ), eq( false ) );
  }


  @Test
  public void testRemoveListener_DoesNotSetListenFalseIfListenerWasNotRegistered() {
    ModelListener listener = mock( ModelListener.class );

    model.removeListener( "foo", listener );

    verify( remoteObject, never() ).listen( eq( "foo" ), eq( false ) );
  }

  @Test
  public void testRemoveListener_ThrowsExceptionForNullListener() {
    try{
      model.removeListener( "foo", null );
      fail();
    } catch( IllegalArgumentException ex ) {
      // expected
    }

    verify( remoteObject, never() ).listen( anyString(), anyBoolean() );
  }
  @Test
  public void testRemoveListener_ThrowsExceptionForNullType() {
    try{
      model.removeListener( null, mock( ModelListener.class ) );
      fail();
    } catch( IllegalArgumentException ex ) {
      // expected
    }

    verify( remoteObject, never() ).listen( anyString(), anyBoolean() );
  }

  @Test
  public void testRemoveListener_DisposesListenerBinding() {
    ClientModelListener listener = new ClientModelListener( "" );
    model.addListener( "foo", listener );
    ClientListenerBinding binding = listener.findBinding( model, "foo" );

    model.removeListener( "foo", listener );

    assertTrue( binding.isDisposed() );
  }

  @Test
  public void testProcessClientNotify_CallHandleEvent() {
    ModelListener listener = mock( ModelListener.class );
    model.addListener( "foo", listener );
    JsonObject argument = mock( JsonObject.class );

    handler.handleNotify( "foo", argument );

    verify( listener ).handleEvent( eq( argument ) );
  }

  @Test
  public void testProcessClientNotify_DoNotCallHandleEventTwice() {
    ModelListener listener = mock( ModelListener.class );
    model.addListener( "foo", listener );
    model.addListener( "foo", listener );
    handler.handleNotify( "foo", mock( JsonObject.class ) );

    verify( listener, times( 1 ) ).handleEvent( any( JsonObject.class ) );
  }


}
