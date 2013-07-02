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
package org.eclipse.rap.addons.dropdown;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rap.clientscripting.ClientListener;
import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.Client;
import org.eclipse.rap.rwt.internal.client.WidgetDataWhiteList;
import org.eclipse.rap.rwt.internal.protocol.JsonUtil;
import org.eclipse.rap.rwt.internal.remote.RemoteObjectImpl;
import org.eclipse.rap.rwt.lifecycle.PhaseId;
import org.eclipse.rap.rwt.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.remote.*;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.*;
import org.junit.*;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;


@SuppressWarnings("restriction")
public class DropDown_Test {

  private Display display;
  private Text text;
  private DropDown dropdown;
  private RemoteObject remoteObject;
  private Connection connection;
  private OperationHandler handler;

  @Before
  public void setUp() {
    Fixture.setUp();
    display = new Display();
    Shell shell = new Shell( display );
    text = new Text( shell, SWT.NONE );
    Fixture.fakeNewRequest();
    remoteObject = mock( RemoteObjectImpl.class );
    connection = mock( Connection.class );
    when( connection.createRemoteObject( anyString() ) ).thenReturn( remoteObject );
    Fixture.fakeConnection( connection );
    doAnswer( new Answer<Object>(){
      public Object answer( InvocationOnMock invocation ) throws Throwable {
        handler = ( OperationHandler )invocation.getArguments()[ 0 ];
        return null;
      }
    } ).when( remoteObject ).setHandler( any( OperationHandler.class ) );
    dropdown = new DropDown( text );
  }

  @After
  public void tearDown() {
    Fixture.tearDown();
  }

  @Test
  public void testContructor_CreatesRemoteObjectWithCorrentType() {
    verify( connection ).createRemoteObject( "rwt.dropdown.DropDown" );
  }

  @Test
  public void testContructor_SetsReferenceWidget() {
    verify( remoteObject ).set( "parent", WidgetUtil.getId( text ) );
  }

  @Test
  public void testGetParent_ThrowsExceptionIfDisposed() {
    dropdown.dispose();

    try {
      dropdown.getParent();
      fail();
    } catch( SWTException ex ) {
      // expected
    }
  }

  @Test
  public void testGetParent_ReturnsParent() {
    assertSame( text, dropdown.getParent() );
  }

  @Test
  public void testDipose_RendersDetroy() {
    dropdown.dispose();
    verify( remoteObject ).destroy();
  }

  @Test
  public void testDipose_RemovesParentListener() {
    dropdown.dispose();

    assertFalse( text.isListening( SWT.Dispose ) );
  }

  @Test
  public void testDipose_FiresDispose() {
    Fixture.fakePhase( PhaseId.PROCESS_ACTION );
    Listener listener = mock( Listener.class );
    dropdown.addListener( SWT.Dispose, listener );

    dropdown.dispose();

    verify( listener ).handleEvent( any( Event.class ) );
  }

  @Test
  public void testDipose_CalledOnControlDispose() {
    Fixture.fakePhase( PhaseId.PROCESS_ACTION );

    text.dispose();

    verify( remoteObject ).destroy();
  }

  @Test
  public void testShow_ThrowsExceptionIfDisposed() {
    dropdown.dispose();
    try {
      dropdown.show();
      fail();
    } catch( SWTException ex ) {
      // expected
    }
  }

  @Test
  public void testShow_SetsVisible() {
    dropdown.show();

    assertTrue( dropdown.getVisible() );
  }

  @Test
  public void testShow_RendersVisibleTrue() {
    dropdown.show();
    verify( remoteObject ).set( "visible", true );
  }

  @Test
  public void testShow_CallTwiceRenderVisibleOnce() {
    dropdown.show();
    dropdown.show();

    verify( remoteObject, times( 1 ) ).set( "visible", true );
  }

  @Test
  public void testHide_ThrowsExceptionIfDisposed() {
    dropdown.dispose();
    try {
      dropdown.hide();
      fail();
    } catch( SWTException ex ) {
      // expected
    }
  }

  @Test
  public void testHide_SetsVisible() {
    dropdown.setVisible( true );
    dropdown.hide();

    assertFalse( dropdown.getVisible() );
  }

  @Test
  public void testHide_RendersVisibleFalse() {
    dropdown.show();
    dropdown.hide();
    verify( remoteObject ).set( "visible", false );
  }

  @Test
  public void testGetVisible_ThrowsExceptionIfDisposed() {
    dropdown.dispose();

    try {
      dropdown.getVisible();
      fail();
    } catch( SWTException ex ) {
      // expected
    }
  }

  @Test
  public void testGetVisible_InitialValueIsFalse() {
    assertFalse( dropdown.getVisible() );
  }

  @Test
  public void testGetSelectionIndex_ThrowsExceptionIfDisposed() {
    dropdown.dispose();

    try {
      dropdown.getSelectionIndex();
      fail();
    } catch( SWTException ex ) {
      // expected
    }
  }

  @Test
  public void testGetSelectionIndex_InitialValue() {
    assertEquals( -1, dropdown.getSelectionIndex() );
  }

  @Test
  public void testSetItems_ThrowsExceptionIfDisposed() {
    dropdown.dispose();

    try {
      dropdown.setItems( new String[]{ "a", "b", "c" } );
      fail();
    } catch( SWTException ex ) {
      // expected
    }
  }

  @Test
  public void testSetItems_ThrowsExceptionForNullArgument() {
    try {
      dropdown.setItems( null );
      fail();
    } catch( IllegalArgumentException ex ) {
      // expected
    }
  }

  @Test
  public void testSetItems_ThrowsExceptionForNullItem() {
    try {
      dropdown.setItems( new String[]{ "a", null, "b" } );
      fail();
    } catch( IllegalArgumentException ex ) {
      // expected
    }
  }

  @Test
  public void testSetItems_RenderItems() {
    dropdown.setItems( new String[]{ "a", "b", "c" } );

    JsonArray expected = JsonUtil.createJsonArray( new String[]{ "a", "b", "c" } );
    verify( remoteObject ).set( eq( "items" ), eq( expected ) );
  }

  @Test
  public void testSetItems_ResetsSelectionIndex() {
    handler.handleSet( new JsonObject().add( "selectionIndex", 7) );
    dropdown.setItems( new String[]{ "a" } );

    assertEquals( -1, dropdown.getSelectionIndex() );
  }

  @Test
  public void testSetVisibleItemCount_ThrowsExceptionIfDisposed() {
    dropdown.dispose();

    try {
      dropdown.setVisibleItemCount( 7 );
      fail();
    } catch( SWTException ex ) {
      // expected
    }
  }

  @Test
  public void testSetVisibleItemCount_RendersVisibleItemCount() {
    dropdown.setVisibleItemCount( 7 );
    verify( remoteObject ).set( "visibleItemCount", 7 );
  }

  @Test
  public void testSetVisibleItemCount_DoesNotRenderVisibleItemCountIfUnchanged() {
    dropdown.setVisibleItemCount( 7 );
    dropdown.setVisibleItemCount( 7 );

    verify( remoteObject, times( 1 ) ).set( "visibleItemCount", 7 );
  }

  @Test
  public void testGetVisibleItemCount_ThrowsExceptionIfDisposed() {
    dropdown.dispose();

    try {
      dropdown.getVisibleItemCount();
      fail();
    } catch( SWTException ex ) {
      // expected
    }
  }

  @Test
  public void testGetVisibleItemCount_ReturnInitialValue() {
    assertEquals( 5, dropdown.getVisibleItemCount() );
  }

  @Test
  public void testGetVisibleItemCount_ReturnUserValue() {
    dropdown.setVisibleItemCount( 23 );

    assertEquals( 23, dropdown.getVisibleItemCount() );
  }

  @Test
  public void testSetData_ThrowsExceptionIfDiposed() {
    dropdown.dispose();

    try {
      dropdown.setData( "foo", "bar" );
      fail();
    } catch( SWTException ex ) {
      // expected
    }
  }

  @Test
  public void testSetData_RendersDataInWhiteList() {
    Fixture.fakePhase( PhaseId.PROCESS_ACTION );

    fakeWidgetDataWhiteList( new String[]{ "foo", "bar" } );
    dropdown.setData( "foo", "bar" );

    @SuppressWarnings("rawtypes")
    ArgumentCaptor<JsonObject> argument = ArgumentCaptor.forClass( JsonObject.class );
    verify( remoteObject ).call( eq( "setData" ), argument.capture() );

    assertEquals( "bar", argument.getValue().get( "foo" ).asString() );
  }

  @Test
  public void testSetData_RendersMarkupEnabled() {
    Fixture.fakePhase( PhaseId.PROCESS_ACTION );

    dropdown.setData( RWT.MARKUP_ENABLED, Boolean.TRUE );

    verify( remoteObject ).set( "markupEnabled", true );
  }

  @Test
  public void testSetData_DoesNotRenderDataNotInWhiteList() {
    Fixture.fakePhase( PhaseId.PROCESS_ACTION );

    fakeWidgetDataWhiteList( new String[]{ "foo", "bar" } );
    dropdown.setData( "fool", "bar" );

    verify( remoteObject, never() ).set( eq( "data" ), any( JsonObject.class ) );
  }

  @Test
  public void testAddListener_SelectionRenderListenTrue() {
    Listener listener = mock( Listener.class );
    dropdown.addListener( SWT.Selection, listener );

    verify( remoteObject ).listen( eq( "Selection" ), eq( true ) );
  }

  @Test
  public void testAddListener_DefaultSelectionRenderListenTrue() {
    Listener listener = mock( Listener.class );
    dropdown.addListener( SWT.DefaultSelection, listener );

    verify( remoteObject ).listen( eq( "DefaultSelection" ), eq( true ) );
  }

  @Test
  public void testRemoveListener_SelectionRenderListenFalse() {
    Listener listener = mock( Listener.class );
    dropdown.addListener( SWT.Selection, listener );
    //Mockito.reset( remoteObject );
    dropdown.removeListener( SWT.Selection, listener );

    verify( remoteObject ).listen( eq( "Selection" ), eq( false ) );
  }

  @Test
  public void testRemoveListener_DefaultSelectionRenderListenFalse() {
    Listener listener = mock( Listener.class );
    dropdown.addListener( SWT.DefaultSelection, listener );
    dropdown.removeListener( SWT.DefaultSelection, listener );

    verify( remoteObject ).listen( eq( "DefaultSelection" ), eq( false ) );
  }

  @Test
  public void testAddListener_Selection_doesNotSendListenTwice() {
    dropdown.addListener( SWT.Selection, mock( Listener.class ) );
    dropdown.addListener( SWT.Selection, mock( Listener.class ) );

    verify( remoteObject, times( 1 ) ).listen( eq( "Selection" ), eq( true ) );
  }

  @Test
  public void testAddListener_Selection_doesNotSendListenForClientListener() {
    dropdown.addListener( SWT.Selection, new ClientListener( "foo" ) );

    verify( remoteObject, times( 0 ) ).listen( eq( "Selection" ), eq( true ) );
  }

  @Test
  public void testProcessSetVisible_ValueIsTrue() {
    handler.handleSet( new JsonObject().add( "visible", true ) );

    assertTrue( dropdown.getVisible() );
  }

  @Test
  public void testProcessSetVisible_ValueIsFalse() {
    dropdown.setVisible( true );

    handler.handleSet( new JsonObject().add( "visible", false ) );

    assertFalse( dropdown.getVisible() );
  }

  @Test
  public void testProcessSetVisible_DoNotRenderToRemoteObject() {
    handler.handleSet( new JsonObject().add( "visible", true ) );

    verify( remoteObject, never() ).set( eq( "visible" ), anyBoolean() );
  }

  @Test
  public void testProcessSetSelectionIndex() {
    handler.handleSet( new JsonObject().add( "selectionIndex", 7) );

    assertEquals( 7, dropdown.getSelectionIndex() );
  }

  @Test
  public void testFireSelectionEvent() {
    Fixture.fakePhase( PhaseId.PROCESS_ACTION );
    final List<Event> log = new ArrayList<Event>();
    dropdown.addListener( SWT.Selection, new Listener() {
      public void handleEvent( Event event ) {
        log.add( event );
      }
    } );

    handler.handleNotify( "Selection", new JsonObject()
      .add( "index", 2 )
      .add( "text", "foo" )
    );

    assertEquals( 1, log.size() );
    assertEquals( 2, log.get( 0 ).index );
    assertEquals( "foo", log.get( 0 ).text );
  }

  @Test
  public void testFireDefaultSelectionEvent() {
    Fixture.fakePhase( PhaseId.PROCESS_ACTION );
    final List<Event> log = new ArrayList<Event>();
    dropdown.addListener( SWT.DefaultSelection, new Listener() {
      public void handleEvent( Event event ) {
        log.add( event );
      }
    } );

    handler.handleNotify( "DefaultSelection", new JsonObject()
      .add( "index", 2 )
      .add( "text", "foo" )
    );

    assertEquals( 1, log.size() );
    assertEquals( 2, log.get( 0 ).index );
    assertEquals( "foo", log.get( 0 ).text );
  }

  ///////////
  // Helper

  public static void fakeWidgetDataWhiteList( String[] keys ) {
    WidgetDataWhiteList service = mock( WidgetDataWhiteList.class );
    when( service.getKeys() ).thenReturn( keys );
    Client client = mock( Client.class );
    when( client.getService( WidgetDataWhiteList.class ) ).thenReturn( service );
    Fixture.fakeClient( client );
  }

}
