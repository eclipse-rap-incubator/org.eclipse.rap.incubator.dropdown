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
package org.eclipse.rap.addons.dropdown.viewer;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.*;
import java.util.List;

import org.eclipse.jface.viewers.*;
import org.eclipse.rap.clientscripting.ClientListener;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.client.WidgetDataWhiteList;
import org.eclipse.rap.rwt.internal.remote.RemoteObjectImpl;
import org.eclipse.rap.rwt.lifecycle.PhaseId;
import org.eclipse.rap.rwt.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.remote.Connection;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.junit.*;


@SuppressWarnings("restriction")
public class DropDownViewer_Test {

  // TODO : test reading scripts and attaching listener when possible

  private static final List<Integer> INTEGER_LIST = Arrays.asList( array( 7, 14, 21 ) );
  private static final String VIEWER_LINK = DropDownViewer.class.getName() + "#viewer";
  private static final String DROPDOWN_KEY = "dropDown";
  private static final String TEXT_KEY = "text";
  private static final String ELEMENTS_KEY = "elements";
  private Display display;
  private Shell shell;
  private Text text;
  private DropDownViewer viewer;

  @Before
  public void setUp() {
    Fixture.setUp();
    display = new Display();
    shell = new Shell( display );
    text = new Text( shell, SWT.NONE );
    Fixture.fakeNewRequest();
  }

  @After
  public void tearDown() {
    Fixture.tearDown();
  }

  @Test
  public void testContructor_createsDropDownWithParent() {
    viewer = new DropDownViewer( text );

    assertSame( text, viewer.getDropDown().getParent() );
  }

  @Test
  public void testConstructor_setsEmptyElements() {
    viewer = new DropDownViewer( text );

    assertEquals( 0, getClientElements().length );
  }

  @Test
  public void testConstructor_linksTextToRemoteObject() {
    viewer = new DropDownViewer( text );

    assertEquals( viewer.getRemoteObject().getId(), text.getData( VIEWER_LINK ) );
  }

  @Test
  public void testConstructor_linksDropDownToRemoteObject() {
    viewer = new DropDownViewer( text );

    assertEquals( viewer.getRemoteObject().getId(), viewer.getDropDown().getData( VIEWER_LINK ) );
  }

  @Test
  public void testConstructor_linksRemoteObjectToDropDown() {
    viewer = new DropDownViewer( text );

    String expected = WidgetUtil.getId( viewer.getDropDown() );
    assertEquals( expected, viewer.getRemoteObject().getString( DROPDOWN_KEY ) );
  }

  @Test
  public void testConstructor_linksRemoteObjectToText() {
    viewer = new DropDownViewer( text );

    String expected = WidgetUtil.getId( text );
    assertEquals( expected, viewer.getRemoteObject().getString( TEXT_KEY ) );
  }

  @Test
  public void testConstructor_addsKeysToWidgetDataWhiteList() {
    viewer = new DropDownViewer( text );

    WidgetDataWhiteList service = RWT.getClient().getService( WidgetDataWhiteList.class );
    assertTrue( Arrays.asList( service.getKeys() ).contains( VIEWER_LINK ) );
  }

  @Test
  public void testConstructor_preventsMultipleViewersOnSameText() {
    Fixture.fakePhase( PhaseId.PROCESS_ACTION );
    viewer = new DropDownViewer( text );

    try {
      new DropDownViewer( text );
      fail();
    } catch( IllegalStateException ex ) {
      // expected
    }
  }

  @Test
  public void testGetTextModifyListener_twiceReturnsSameInstance() {
    viewer = new DropDownViewer( text );

    ClientListener listener1 = viewer.getTextModifyListener();
    ClientListener listener2 = viewer.getTextModifyListener();

    assertNotNull( listener1 );
    assertSame( listener1, listener2 );
  }

  @Test
  public void testGetTextModifyListener_returnSameInstanceForDifferentViewer() {
    viewer = new DropDownViewer( text );

    ClientListener listener1 = viewer.getTextModifyListener();
    DropDownViewer viewer2 = new DropDownViewer( new Text( shell, SWT.NONE ) );
    ClientListener listener2 = viewer2.getTextModifyListener();

    assertNotNull( listener1 );
    assertSame( listener1, listener2 );
  }

  @Test
  public void testGetTextVerifyListener_returnSameInstanceForDifferentViewer() {
    viewer = new DropDownViewer( text );

    ClientListener listener1 = viewer.getTextVerifyListener();
    DropDownViewer viewer2 = new DropDownViewer( new Text( shell, SWT.NONE ) );
    ClientListener listener2 = viewer2.getTextVerifyListener();

    assertNotNull( listener1 );
    assertSame( listener1, listener2 );
  }

  @Test
  public void testGetTextKeyDownListener_returnSameInstanceForDifferentViewer() {
    viewer = new DropDownViewer( text );

    ClientListener listener1 = viewer.getTextKeyDownListener();
    DropDownViewer viewer2 = new DropDownViewer( new Text( shell, SWT.NONE ) );
    ClientListener listener2 = viewer2.getTextKeyDownListener();

    assertNotNull( listener1 );
    assertSame( listener1, listener2 );
  }

  @Test
  public void testGetDropDownSelectionListener_returnSameInstanceForDifferentViewer() {
    viewer = new DropDownViewer( text );

    ClientListener listener1 = viewer.getDropDownSelectionListener();
    DropDownViewer viewer2 = new DropDownViewer( new Text( shell, SWT.NONE ) );
    ClientListener listener2 = viewer2.getDropDownSelectionListener();

    assertNotNull( listener1 );
    assertSame( listener1, listener2 );
  }

  @Test
  public void testGetDropDownDefaultSelectionListener_returnSameInstanceForDifferentViewer() {
    viewer = new DropDownViewer( text );

    ClientListener listener1 = viewer.getDropDownDefaultSelectionListener();
    DropDownViewer viewer2 = new DropDownViewer( new Text( shell, SWT.NONE ) );
    ClientListener listener2 = viewer2.getDropDownDefaultSelectionListener();

    assertNotNull( listener1 );
    assertSame( listener1, listener2 );
  }

  @Test
  public void testGetDropDownShowListener_returnSameInstanceForDifferentViewer() {
    viewer = new DropDownViewer( text );

    ClientListener listener1 = viewer.getDropDownShowListener();
    DropDownViewer viewer2 = new DropDownViewer( new Text( shell, SWT.NONE ) );
    ClientListener listener2 = viewer2.getDropDownShowListener();

    assertNotNull( listener1 );
    assertSame( listener1, listener2 );
  }

  @Test
  public void testInputChanged_addsElementsToRemoteObject() {
    viewer = new DropDownViewer( text );
    viewer.setContentProvider( new ArrayContentProvider() );

    viewer.setInput( INTEGER_LIST );

    assertArrayEquals( array( "7", "14", "21" ), getClientElements() );
  }

  @Test
  public void testRefresh_updatesElements() {
    viewer = new DropDownViewer( text );
    viewer.setContentProvider( new ArrayContentProvider() );
    String[] input = array( "a", "b", "c" );
    viewer.setInput( input );
    input[1] = "x";

    viewer.refresh();

    assertArrayEquals( array( "a", "x", "c" ), getClientElements() );
  }

  @Test
  public void testClientElementsUseLabelProvider() {
    viewer = new DropDownViewer( text );
    viewer.setContentProvider( new ArrayContentProvider() );
    viewer.setInput( array( 1, 2, 3 ) );

    viewer.setLabelProvider( new LabelProvider() {
      @Override
      public String getText( Object element ) {
        return "Item " + element;
      }
    } );

    assertArrayEquals( array( "Item 1", "Item 2", "Item 3" ), getClientElements() );
  }

  @Test
  public void testProcessSelectionChangedEvent() {
    viewer = new DropDownViewer( text );
    viewer.setContentProvider( new ArrayContentProvider() );
    viewer.setInput( INTEGER_LIST );
    ISelectionChangedListener listener = mock( ISelectionChangedListener.class );
    viewer.addSelectionChangedListener( listener );

    Map<String, Object> event = createMap( "index", Integer.valueOf( 2 ) );
    viewer.getRemoteObject().notify( "SelectionChanged", event );

    verify( listener ).selectionChanged( any( SelectionChangedEvent.class ) );
  }

  @Test
  public void testProcessSelectionChangedEvent_elementField() {
    viewer = new DropDownViewer( text );
    viewer.setContentProvider( new ArrayContentProvider() );
    final List<SelectionChangedEvent> log = new ArrayList<SelectionChangedEvent>();
    viewer.setInput( INTEGER_LIST );
    viewer.addSelectionChangedListener( new ISelectionChangedListener() {
      public void selectionChanged( SelectionChangedEvent event ) {
        log.add( event );
      }
    } );

    Map<String, Object> event = createMap( "index", Integer.valueOf( 2 ) );
    viewer.getRemoteObject().notify( "SelectionChanged", event );

    IStructuredSelection selection = ( IStructuredSelection )log.get( 0 ).getSelection();
    assertEquals( Integer.valueOf( 21 ), selection.getFirstElement() );
  }

  @Test
  public void testAddSelectionChangedListener_callsRemoteObjectListen() {
    RemoteObject remoteObject = fakeRemoteObject();
    viewer = new DropDownViewer( text );

    viewer.addSelectionChangedListener( mock( ISelectionChangedListener.class ) );

    verify( remoteObject ).listen( eq( "SelectionChanged" ), eq( true ) );
  }

  @Test
  public void testDestroyRemoteObject() {
    Fixture.fakePhase( PhaseId.PROCESS_ACTION );
    viewer = new DropDownViewer( text );
    viewer.setContentProvider( new ArrayContentProvider() );

    viewer.getControl().dispose();

    assertTrue( viewer.getRemoteObject().isDestroyed() );
  }

  @Test
  public void testInputChanged_callsContentProviderWithInput() {
    viewer = new DropDownViewer( text );
    IStructuredContentProvider contentProvider = mock( IStructuredContentProvider.class );
    when( contentProvider.getElements( any() ) ).thenReturn( new Object[ 0 ] );
    viewer.setContentProvider( contentProvider );
    Object input = new Object();

    viewer.setInput( input );

    verify( contentProvider ).getElements( eq( input ) );
  }

  @Test
  public void testRefresh_callsContentProviderWithInput() {
    viewer = new DropDownViewer( text );
    IStructuredContentProvider contentProvider = mock( IStructuredContentProvider.class );
    when( contentProvider.getElements( any() ) ).thenReturn( new Object[ 0 ] );
    viewer.setContentProvider( contentProvider );
    Object input = new Object();
    viewer.setInput( input );
    reset( contentProvider );

    viewer.refresh();

    verify( contentProvider ).getElements( eq( input ) );
  }

  //////////
  // Helpers

  private String[] getClientElements() {
    return ( String[] )viewer.getRemoteObject().get( ELEMENTS_KEY );
  }

  private static RemoteObject fakeRemoteObject() {
    RemoteObject remoteObject = mock( RemoteObjectImpl.class );
    Connection connection = mock( Connection.class );
    when( connection.createRemoteObject( anyString() ) ).thenReturn( remoteObject );
    Fixture.fakeConnection( connection );
    return remoteObject;
  }

  private static Map<String, Object> createMap( String key, Object value ) {
    Map<String, Object> event = new HashMap<String, Object>();
    event.put( key, value );
    return event;
  }

  private static Integer[] array( int... values ) {
    Integer[] result = new Integer[ values.length ];
    for( int i = 0; i < result.length; i++ ) {
      result[ i ] = Integer.valueOf( values[ i ] );
    }
    return result;
  }

  private static String[] array( String... values ) {
    return values;
  }

}
