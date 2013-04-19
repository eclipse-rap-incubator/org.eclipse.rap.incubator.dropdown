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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.rap.rwt.client.Client;
import org.eclipse.rap.rwt.internal.client.WidgetDataWhiteList;
import org.eclipse.rap.rwt.internal.remote.RemoteObjectImpl;
import org.eclipse.rap.rwt.lifecycle.PhaseId;
import org.eclipse.rap.rwt.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.remote.Connection;
import org.eclipse.rap.rwt.remote.OperationHandler;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
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
    doAnswer( new Answer< Object >(){
      @Override
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
  public void testGetParent() {
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
  public void testShow_RendersVisibilityTrue() {
    dropdown.show();
    verify( remoteObject ).set( "visibility", true );
  }

  @Test
  public void testShow_CallTwiceRenderVisibilityOnce() {
    dropdown.show();
    dropdown.show();

    verify( remoteObject, times( 1 ) ).set( "visibility", true );
  }

  @Test
  public void testGetVisibility_InitialValueIsFalse() {
    assertFalse( dropdown.getVisibility() );
  }

  @Test
  public void testShow_SetsVisibility() {
    dropdown.show();

    assertTrue( dropdown.getVisibility() );
  }

  @Test
  public void testProcessSetVisibility_ValueIsTrue() {
    handler.handleSet( createMap( "visibility", true ) );

    assertTrue( dropdown.getVisibility() );
  }

  @Test
  public void testProcessSetVisibility_ValueIsFalse() {
    dropdown.setVisibility( true );

    handler.handleSet( createMap( "visibility", false ) );

    assertFalse( dropdown.getVisibility() );
  }

  @Test
  public void testProcessSetSelectionIndex() {
    handler.handleSet( createMap( "selectionIndex", 7 ) );

    assertEquals( 7, dropdown.getSelectionIndex() );
  }

  @Test
  public void testSetItems_ResetsSelectionIndex() {
    handler.handleSet( createMap( "selectionIndex", 7 ) );
    dropdown.setItems( new String[]{ "a" } );

    assertEquals( -1, dropdown.getSelectionIndex() );
  }

  @Test
  public void testProcessSetVisibility_DoNotRenderToRemoteObject() {
    handler.handleSet( createMap( "visibility", true ) );

    verify( remoteObject, never() ).set( eq( "visibility" ), anyBoolean() );
  }

  @Test
  public void testHide_SetsVisibility() {
    dropdown.setVisibility( true );
    dropdown.hide();

    assertFalse( dropdown.getVisibility() );
  }

  @Test
  public void testSetVisibleItemCount_RendersVisibleItemCount() {
    dropdown.setVisibleItemCount( 7 );
    verify( remoteObject ).set( "visibleItemCount", 7 );
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testSetData_RendersDataInWhiteList() {
    Fixture.fakePhase( PhaseId.PROCESS_ACTION );

    fakeWidgetDataWhiteList( new String[]{ "foo", "bar" } );
    dropdown.setData( "foo", "bar" );

    @SuppressWarnings("rawtypes")
    ArgumentCaptor< Map > argument = ArgumentCaptor.forClass( Map.class );
    verify( remoteObject ).call( eq( "setData" ), argument.capture() );
    assertEquals( "bar", argument.getValue().get( "foo" ) );
  }

  @Test
  public void testSetData_DoesNotRenderDataNotInWhiteList() {
    Fixture.fakePhase( PhaseId.PROCESS_ACTION );

    fakeWidgetDataWhiteList( new String[]{ "foo", "bar" } );
    dropdown.setData( "fool", "bar" );

    verify( remoteObject, never() ).set( eq( "data" ), any() );
  }

  @Test
  public void testShow_ThrowsExceptionIfDisposed() {
    dropdown.dispose();
    try {
      dropdown.show();
      fail();
    } catch( IllegalStateException ex ) {
      // expected
    }
  }

  @Test
  public void testHide_RendersVisibilityFalse() {
    dropdown.show();
    dropdown.hide();
    verify( remoteObject ).set( "visibility", false );
  }

  @Test
  public void testHide_ThrowsExceptionIfDisposed() {
    dropdown.dispose();
    try {
      dropdown.hide();
      fail();
    } catch( IllegalStateException ex ) {
      // expected
    }
  }

  @Test
  public void testGetSelectionIndex_InitialValue() {
    assertEquals( -1, dropdown.getSelectionIndex() );
  }

  @Test
  public void testSetItem_RenderItems() {
    dropdown.setItems( new String[]{ "a", "b", "c" } );

    verify( remoteObject ).set( eq( "items" ), eq( new String[]{ "a", "b", "c" } ) );
  }

  @Test
  public void testSetItem_RenderItemsSaveCopy() {
    String[] strings = new String[]{ "a", "b", "c" };
    dropdown.setItems( strings );
    strings[ 1 ] = "x";

    verify( remoteObject ).set( eq( "items" ), eq( new String[]{ "a", "b", "c" } ) );
  }

  @Test
  public void testAddListener_SelectionRenderListenTrue() {
    Listener listener = mock( Listener.class );
    dropdown.addListener( SWT.Selection, listener );

    verify( remoteObject ).listen( eq( "Selection" ), eq( true ) );
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
  public void testAddListener_DefaultSelectionRenderListenTrue() {
    Listener listener = mock( Listener.class );
    dropdown.addListener( SWT.DefaultSelection, listener );

    verify( remoteObject ).listen( eq( "DefaultSelection" ), eq( true ) );
  }

  @Test
  public void testRemoveListener_DefaultSelectionRenderListenFalse() {
    Listener listener = mock( Listener.class );
    dropdown.addListener( SWT.DefaultSelection, listener );
    //Mockito.reset( remoteObject );
    dropdown.removeListener( SWT.DefaultSelection, listener );

    verify( remoteObject ).listen( eq( "DefaultSelection" ), eq( false ) );
  }

  @Test
  public void testFireSelectionEvent() {
    Fixture.fakePhase( PhaseId.PROCESS_ACTION );
    final List< Event > log = new ArrayList< Event >();
    dropdown.addListener( SWT.Selection, new Listener() {
      @Override
      public void handleEvent( Event event ) {
        log.add( event );
      }
    } );

    handler.handleNotify( "Selection", createMap(
      "index", 2,
      "text", "foo"
    ) );

    assertEquals( 1, log.size() );
    assertEquals( 2, log.get( 0 ).index );
    assertEquals( "foo", log.get( 0 ).text );
  }

  @Test
  public void testFireDefaultSelectionEvent() {
    Fixture.fakePhase( PhaseId.PROCESS_ACTION );
    final List< Event > log = new ArrayList< Event >();
    dropdown.addListener( SWT.DefaultSelection, new Listener() {
      @Override
      public void handleEvent( Event event ) {
        log.add( event );
      }
    } );

    handler.handleNotify( "DefaultSelection", createMap(
      "index", 2,
      "text", "foo"
    ) );

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

  private Map<String, Object> createMap( Object... values ) {
    Map<String, Object> properties = new HashMap<String, Object>();
    for( int i = 0; i < values.length; i += 2 ) {
      properties.put( ( String )values[ i ], values[ i + 1 ] );
    }
    return properties;
  }

}
