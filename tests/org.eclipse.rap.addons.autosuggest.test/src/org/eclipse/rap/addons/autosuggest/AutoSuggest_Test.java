/*******************************************************************************
 * Copyright (c) 2013, 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.addons.autosuggest;

import static org.eclipse.rap.rwt.widgets.WidgetUtil.getId;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetDataUtil;
import org.eclipse.rap.rwt.lifecycle.PhaseId;
import org.eclipse.rap.rwt.remote.Connection;
import org.eclipse.rap.rwt.remote.OperationHandler;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.rap.rwt.scripting.ClientListener;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.eclipse.rap.rwt.widgets.DropDown;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;


@SuppressWarnings( "restriction" )
public class AutoSuggest_Test {

  private static final String REMOTE_SELECTION_EVENT = "suggestionSelected";
  private static final String REMOTE_TYPE = "rwt.remote.Model";
  private static final String MODEL_ID_KEY = "org.eclipse.rap.addons.autosuggest#Model";

  private Text text;
  private RemoteObject remoteObject;
  private Connection connection;

  @Before
  public void setUp() {
    Fixture.setUp();
    Fixture.fakePhase( PhaseId.PROCESS_ACTION );
    Display display = new Display();
    Shell shell = new Shell( display );
    text = new Text( shell, SWT.NONE );
    mockRemoteObject();
  }

  @After
  public void tearDown() {
    Fixture.tearDown();
  }

  @Test( expected = NullPointerException.class )
  public void testConstructor_failsWithNull() {
    new AutoSuggest( null );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructor_failsWithDisposedText() {
    text.dispose();
    new AutoSuggest( text );
  }

  @Test
  public void testConstructor_createsDropDownWithParent() {
    AutoSuggest autoSuggest = new AutoSuggest( text );

    DropDown dropDown = autoSuggest.getDropDown();
    assertSame( text, dropDown.getParent() );
  }

  @Test
  public void testConstructor_createsRemoteObject() {
    new AutoSuggest( text );

    verify( connection ).createRemoteObject( eq( REMOTE_TYPE ) );
  }

  @Test
  public void testConstructor_addsClientListenersToText() {
    new AutoSuggest( text );

    assertTrue( text.getListeners( SWT.Verify )[ 0 ] instanceof ClientListener );
    assertTrue( text.getListeners( SWT.Modify )[ 0 ] instanceof ClientListener );
  }

  @Test
  public void testConstructor_addsClientListenersToDropDown() {
    AutoSuggest autoSuggest = new AutoSuggest( text );

    DropDown dropDown = autoSuggest.getDropDown();
    assertTrue( dropDown.getListeners( SWT.Show )[ 0 ] instanceof ClientListener );
    assertTrue( dropDown.getListeners( SWT.Hide )[ 0 ] instanceof ClientListener );
    assertTrue( dropDown.getListeners( SWT.Selection )[ 0 ] instanceof ClientListener );
    assertTrue( dropDown.getListeners( SWT.DefaultSelection )[ 0 ] instanceof ClientListener );
  }

  @Test
  public void testConstructor_setsTextIdOnModel() {
    new AutoSuggest( text );

    verify( remoteObject ).set( "textWidgetId", getId( text ) );
  }

  @Test
  public void testConstructor_setDropDownIdOnModel() {
    AutoSuggest autoSuggest = new AutoSuggest( text );

    DropDown dropDown = autoSuggest.getDropDown();
    verify( remoteObject ).set( "dropDownWidgetId", getId( dropDown ) );
  }

  @Test
  public void testConstructor_setModelIdOnDropDown() {
    AutoSuggest autoSuggest = new AutoSuggest( text );

    DropDown dropDown = autoSuggest.getDropDown();
    assertEquals( "foo", dropDown.getData( MODEL_ID_KEY ) );
  }

  @Test
  public void testConstructor_setModelIdOnText() {
    new AutoSuggest( text );

    assertEquals( "foo", text.getData( MODEL_ID_KEY ) );
  }

  @Test
  public void testConstructor_addKeysToWidgetDataWhiteList() {
    new AutoSuggest( text );

    assertTrue( WidgetDataUtil.getDataKeys().contains( MODEL_ID_KEY ) );
  }

  @Test
  public void testIsDisposed_returnsFalse() {
    AutoSuggest autoSuggest = new AutoSuggest( text );

    assertFalse( autoSuggest.isDisposed() );
  }

  @Test
  public void testIsDisposed_returnsTrueAfterDispose() {
    AutoSuggest autoSuggest = new AutoSuggest( text );

    autoSuggest.dispose();

    assertTrue( autoSuggest.isDisposed() );
  }

  @Test
  public void testDispose_disposesDropDown() {
    AutoSuggest autoSuggest = new AutoSuggest( text );
    DropDown dropDown = autoSuggest.getDropDown();

    autoSuggest.dispose();

    assertTrue( dropDown.isDisposed() );
  }

  @Test
  public void testDispose_destroyRemoteObject() {
    AutoSuggest autoSuggest = new AutoSuggest( text );

    autoSuggest.dispose();

    verify( remoteObject ).destroy();
  }

  @Test
  public void testDispose_callingTwicedestroysRemoteObjectOnce() {
    AutoSuggest autoSuggest = new AutoSuggest( text );

    autoSuggest.dispose();
    autoSuggest.dispose();

    verify( remoteObject, times( 1 ) ).destroy();
  }

  @Test
  public void testDispose_removesClientListenersFromText() {
    AutoSuggest autoSuggest = new AutoSuggest( text );

    autoSuggest.dispose();

    assertFalse( text.isListening( SWT.Verify ) );
    assertFalse( text.isListening( SWT.Modify ) );
  }

  @Test
  public void testDispose_disposeTwice() {
    AutoSuggest autoSuggest = new AutoSuggest( text );

    autoSuggest.dispose();
    autoSuggest.dispose();
  }


  @Test
  public void testDisposedOnTextDispose() {
    AutoSuggest autoSuggest = new AutoSuggest( text );

    text.dispose();

    assertTrue( autoSuggest.isDisposed() );
  }

  @Test
  public void testSetVisibleItemCount_setsDropDownVisibleItemCount() {
    AutoSuggest autoSuggest = new AutoSuggest( text );
    DropDown dropDown = autoSuggest.getDropDown();

    autoSuggest.setVisibleItemCount( 23 );

    assertEquals( 23, dropDown.getVisibleItemCount() );
  }

  @Test( expected = IllegalStateException.class )
  public void testSetVisibleItemCount_failsIfDisposed() {
    AutoSuggest autoSuggest = new AutoSuggest( text );
    autoSuggest.dispose();

    autoSuggest.setVisibleItemCount( 23 );
  }

  @Test
  public void testGetVisibleItemCount_getsDropDownVisibleItemCount() {
    AutoSuggest autoSuggest = new AutoSuggest( text );
    DropDown dropDown = autoSuggest.getDropDown();

    dropDown.setVisibleItemCount( 23 );

    assertEquals( 23, autoSuggest.getVisibleItemCount() );
  }

  @Test( expected = IllegalStateException.class )
  public void testGetVisibleItemCount_failsIfDisposed() {
    AutoSuggest autoSuggest = new AutoSuggest( text );
    autoSuggest.dispose();

    autoSuggest.getVisibleItemCount();
  }

  @Test
  public void testSetDataSource_acceptsNullArgument() {
    AutoSuggest autoSuggest = new AutoSuggest( text );

    autoSuggest.setDataSource( null );

    verify( remoteObject ).set( eq( "dataSourceId" ), ( String )isNull() );
  }

  @Test( expected = IllegalStateException.class )
  public void testSetDataSource_failsIfDisposed() {
    AutoSuggest autoSuggest = new AutoSuggest( text );
    autoSuggest.dispose();

    autoSuggest.setDataSource( mock( DataSource.class ) );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testSetDataSource_failsIfDataSourceIsDisposed() {
    AutoSuggest autoSuggest = new AutoSuggest( text );
    DataSource dataSource = mock( DataSource.class );
    when( new Boolean( dataSource.isDisposed() ) ).thenReturn( Boolean.TRUE );

    autoSuggest.setDataSource( dataSource );
  }

  @Test
  public void testSetDataSource_setsDataSourceOnRemoteObject() {
    AutoSuggest autoSuggest = new AutoSuggest( text );
    DataSource dataSource = mock( DataSource.class );
    when( dataSource.getId() ).thenReturn( "providerId" );

    autoSuggest.setDataSource( dataSource );

    verify( remoteObject ).set( eq( "dataSourceId" ), eq( "providerId" ) );
  }

  @Test
  public void testSetDataSource_setsColumnWidthOnDropDown() {
    AutoSuggest autoSuggest = new AutoSuggest( text );
    DataSource dataSource = new DataSource();
    dataSource.setTemplate( new ColumnTemplate( 23, 42 ) );

    autoSuggest.setDataSource( dataSource );

    int[] columns = ( int[] )autoSuggest.getDropDown().getData( "columns" );
    assertArrayEquals( new int[] { 23, 42 }, columns );
  }

  @Test
  public void testSetAutoComplete_defaultIsNotSet() {
    new AutoSuggest( text );

    verify( remoteObject, never() ).set( eq( "autoComplete" ), anyBoolean() );
  }

  @Test( expected = IllegalStateException.class )
  public void testSetAutoComplete_failsIfDisposed() {
    AutoSuggest autoSuggest = new AutoSuggest( text );
    autoSuggest.dispose();

    autoSuggest.setAutoComplete( true );
  }

  @Test
  public void testSetAutoComplete_setsAutoCompleteTrueOnRemoteObject() {
    AutoSuggest autoSuggest = new AutoSuggest( text );

    autoSuggest.setAutoComplete( true );

    verify( remoteObject ).set( eq( "autoComplete" ), eq( true ) );
  }

  @Test
  public void testSetAutoComplete_setsAutoCompleteFalseOnRemoteObject() {
    AutoSuggest autoSuggest = new AutoSuggest( text );

    autoSuggest.setAutoComplete( false );

    verify( remoteObject ).set( eq( "autoComplete" ), eq( false ) );
  }

  @Test( expected = NullPointerException.class )
  public void testAddSelectionListener_failsWithNull() {
    AutoSuggest autoSuggest = new AutoSuggest( text );

    autoSuggest.addSelectionListener( null );
  }

  @Test( expected = IllegalStateException.class )
  public void testAddSelectionListener_failsIfDisposed() {
    AutoSuggest autoSuggest = new AutoSuggest( text );
    autoSuggest.dispose();

    autoSuggest.addSelectionListener( mock( SuggestionSelectedListener.class ) );
  }

  @Test
  public void testAddSelectionListener_doesNotAddListenerTwice() {
    AutoSuggest autoSuggest = new AutoSuggest( text );
    SuggestionSelectedListener listener = mock( SuggestionSelectedListener.class );

    autoSuggest.addSelectionListener( listener );
    autoSuggest.addSelectionListener( listener );

    autoSuggest.notifySelectionListeners();
    verify( listener, times( 1 ) ).suggestionSelected();
  }

  @Test
  public void testAddSelectionListener_callsListenOnRemoteObject() {
    AutoSuggest autoSuggest = new AutoSuggest( text );

    autoSuggest.addSelectionListener( mock( SuggestionSelectedListener.class ) );

    verify( remoteObject ).listen( REMOTE_SELECTION_EVENT, true );
  }

  @Test
  public void testAddSelectionListener_callsListenOnRemoteObjectOnlyOnce() {
    AutoSuggest autoSuggest = new AutoSuggest( text );

    autoSuggest.addSelectionListener( mock( SuggestionSelectedListener.class ) );
    autoSuggest.addSelectionListener( mock( SuggestionSelectedListener.class ) );

    verify( remoteObject, times( 1 ) ).listen( REMOTE_SELECTION_EVENT, true );
  }

  @Test
  public void testRemoveSelectionListener() {
    AutoSuggest autoSuggest = new AutoSuggest( text );
    SuggestionSelectedListener listener = mock( SuggestionSelectedListener.class );
    autoSuggest.addSelectionListener( listener );

    autoSuggest.removeSelectionListener( listener );

    autoSuggest.notifySelectionListeners();
    verify( listener, never() ).suggestionSelected();
  }

  @Test( expected = NullPointerException.class )
  public void testRemoveSelectionListener_failsWithNull() {
    AutoSuggest autoSuggest = new AutoSuggest( text );

    autoSuggest.removeSelectionListener( null );
  }

  @Test( expected = IllegalStateException.class )
  public void testRemoveSelectionListener_failsIfDispose() {
    AutoSuggest autoSuggest = new AutoSuggest( text );
    autoSuggest.dispose();

    autoSuggest.removeSelectionListener( mock( SuggestionSelectedListener.class ) );
  }

  @Test
  public void testRemoveSelectionListener_doesNotFailWithUnknownListener() {
    AutoSuggest autoSuggest = new AutoSuggest( text );
    SuggestionSelectedListener listener = mock( SuggestionSelectedListener.class );

    autoSuggest.removeSelectionListener( listener );
  }

  @Test
  public void testRemoveSelectionListener_callsListenOnRemoteObject() {
    AutoSuggest autoSuggest = new AutoSuggest( text );
    SuggestionSelectedListener listener = mock( SuggestionSelectedListener.class );
    autoSuggest.addSelectionListener( listener );

    autoSuggest.removeSelectionListener( listener );

    verify( remoteObject ).listen( REMOTE_SELECTION_EVENT, false );
  }

  @Test
  public void testRemoveSelectionListener_callsListenOnRemoteObjectOnlyForLastListener() {
    AutoSuggest autoSuggest = new AutoSuggest( text );
    SuggestionSelectedListener listener = mock( SuggestionSelectedListener.class );
    autoSuggest.addSelectionListener( mock( SuggestionSelectedListener.class ) );
    autoSuggest.addSelectionListener( listener );

    autoSuggest.removeSelectionListener( listener );

    verify( remoteObject, never() ).listen( REMOTE_SELECTION_EVENT, false );
  }

  @Test
  public void testNotifySelectionListeners_notifiesSelectionListener() {
    AutoSuggest autoSuggest = new AutoSuggest( text );
    SuggestionSelectedListener listener = mock( SuggestionSelectedListener.class );

    autoSuggest.addSelectionListener( listener );

    autoSuggest.notifySelectionListeners();
    verify( listener ).suggestionSelected();
  }

  @Test
  public void testNotifySelectionListeners_notifiesMultipleSelectionListener() {
    AutoSuggest autoSuggest = new AutoSuggest( text );
    SuggestionSelectedListener listener1 = mock( SuggestionSelectedListener.class );
    SuggestionSelectedListener listener2 = mock( SuggestionSelectedListener.class );

    autoSuggest.addSelectionListener( listener1 );
    autoSuggest.addSelectionListener( listener2 );

    autoSuggest.notifySelectionListeners();
    verify( listener1 ).suggestionSelected();
    verify( listener2 ).suggestionSelected();
  }

  @Test
  public void testNotifySelectionListeners_calledOnNotifyOperation() {
    AtomicReference<OperationHandler> operationHandlerCaptor = captureOperationHandler();
    AutoSuggest autoSuggest = new AutoSuggest( text );
    SuggestionSelectedListener listener = mock( SuggestionSelectedListener.class );
    autoSuggest.addSelectionListener( listener );

    operationHandlerCaptor.get().handleNotify( REMOTE_SELECTION_EVENT, null );

    verify( listener ).suggestionSelected();
  }

  private AtomicReference<OperationHandler> captureOperationHandler() {
    final AtomicReference<OperationHandler> captor = new AtomicReference<OperationHandler>();
    doAnswer( new Answer<Object>() {
      public Object answer( InvocationOnMock invocation ) throws Throwable {
        captor.set( ( OperationHandler )invocation.getArguments()[ 0 ] );
        return null;
      }
    } ).when( remoteObject ).setHandler( any( OperationHandler.class ) );
    return captor;
  }

  private void mockRemoteObject() {
    remoteObject = mock( RemoteObject.class );
    when( remoteObject.getId() ).thenReturn( "foo" );
    connection = spy( RWT.getUISession().getConnection() );
    when( connection.createRemoteObject( REMOTE_TYPE ) ).thenReturn( remoteObject );
    Fixture.fakeConnection( connection );
  }

}
