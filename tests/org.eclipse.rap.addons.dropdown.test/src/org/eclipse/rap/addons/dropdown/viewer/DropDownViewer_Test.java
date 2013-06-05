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

import static org.eclipse.rap.rwt.internal.protocol.JsonUtil.createJsonArray;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.*;
import java.util.List;

import org.eclipse.jface.viewers.*;
import org.eclipse.rap.addons.dropdown.DropDown;
import org.eclipse.rap.clientscripting.ClientListener;
import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.client.WidgetDataWhiteList;
import org.eclipse.rap.rwt.internal.client.WidgetDataWhiteListImpl;
import org.eclipse.rap.rwt.internal.remote.RemoteObjectImpl;
import org.eclipse.rap.rwt.lifecycle.PhaseId;
import org.eclipse.rap.rwt.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.remote.*;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.junit.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;


@SuppressWarnings("restriction")
public class DropDownViewer_Test {

  // TODO : test reading scripts and attaching listener when possible

  private static final List<Integer> INTEGER_LIST = Arrays.asList(
    Integer.valueOf( 7 ),
    Integer.valueOf( 14 ),
    Integer.valueOf( 21 )
  );
  private static final String REMOTE_TYPE = "rwt.remote.Model";
  private static final String VIEWER_LINK =
      "org.eclipse.rap.addons.dropdown.viewer.DropDownViewer#viewer";
  private static String DROPDOWN_KEY = "dropDown";
  private static final String TEXT_KEY = "text";
  private static final String DECORATOR_KEY = "decorator";
  private static String ELEMENTS_KEY = "elements";
  private static String SELECTION_KEY = "selection";
  private Display display;
  private Text text;
  private DropDownViewer viewer;
  private Shell shell;
  private DropDown dropDown;
  private RemoteObject remoteObject;
  private OperationHandler handler;

  @Before
  public void setUp() {
    Fixture.setUp();
    display = new Display();
    shell = new Shell( display );
    text = new Text( shell, SWT.NONE );
    Fixture.fakeNewRequest();
    remoteObject = mock( RemoteObjectImpl.class );
    when( remoteObject.getId() ).thenReturn( "foo" );
    Connection connection = spy( RWT.getUISession().getConnection() );
    when( connection.createRemoteObject( REMOTE_TYPE ) ).thenReturn( remoteObject );
    Fixture.fakeConnection( connection );
    doAnswer( new Answer<Object>(){
      public Object answer( InvocationOnMock invocation ) throws Throwable {
        handler = ( OperationHandler )invocation.getArguments()[ 0 ];
        return null;
      }
    } ).when( remoteObject ).setHandler( any( OperationHandler.class ) );

  }

  @After
  public void tearDown() {
    Fixture.tearDown();
  }

  @Test
  public void testContructor_CreatesDropDownWithParent() {
    createViewer();
    assertSame( text, viewer.getDropDown().getParent() );
  }

  @Test
  public void testContructor_CreatesDecorator() {
    createViewer();
    assertSame( text, viewer.getDecorator().getControl() );
    assertEquals( 2, viewer.getDecorator().getMarginWidth() );
    assertFalse( viewer.getDecorator().isVisible() );
    assertNotNull( viewer.getDecorator().getImage() );
  }

  @Test
  public void testConstructor_SetEmptyElements() {
    createViewer();

    JsonArray expected = new JsonArray();
    verify( remoteObject ).set( eq( ELEMENTS_KEY ), eq( expected ) );
  }

  @Test
  public void testConstructor_SetRemoteObjectSelection() {
    createViewer();

    verify( remoteObject ).set( eq( SELECTION_KEY ), eq( -1 ) );
  }

  @Test
  public void testGetDefaultTextModifyListenerTwice_ReturnsSameInstance() {
    createViewer();
    ClientListener listener1 = viewer.getTextModifyListener();
    ClientListener listener2 = viewer.getTextModifyListener();

    assertNotNull( listener1 );
    assertSame( listener1, listener2 );
  }

  @Test
  public void testGetDefaultTextModifyListenerTwice_DifferentViewerReturnSameInstance() {
    createViewer();
    ClientListener listener1 = viewer.getTextModifyListener();
    DropDownViewer viewer2 = new DropDownViewer( new Text( shell, SWT.NONE ) );
    ClientListener listener2 = viewer2.getTextModifyListener();

    assertNotNull( listener1 );
    assertSame( listener1, listener2 );
  }

  @Test
  public void testGetDefaultTextVerifyListenerTwice_DifferentViewerReturnSameInstance() {
    createViewer();
    ClientListener listener1 = viewer.getTextVerifyListener();
    DropDownViewer viewer2 = new DropDownViewer( new Text( shell, SWT.NONE ) );
    ClientListener listener2 = viewer2.getTextVerifyListener();


    assertNotNull( listener1 );
    assertSame( listener1, listener2 );
  }

  @Test
  public void testGetDefaultTextKeyDownListenerTwice_DifferentViewerReturnSameInstance() {
    createViewer();
    ClientListener listener1 = viewer.getTextKeyDownListener();
    DropDownViewer viewer2 = new DropDownViewer( new Text( shell, SWT.NONE ) );
    ClientListener listener2 = viewer2.getTextKeyDownListener();

    assertNotNull( listener1 );
    assertSame( listener1, listener2 );
  }

  @Test
  public void testGetDefaultTextMouseDownListenerTwice_DifferentViewerReturnSameInstance() {
    createViewer();
    ClientListener listener1 = viewer.getTextMouseDownListener();
    DropDownViewer viewer2 = new DropDownViewer( new Text( shell, SWT.NONE ) );
    ClientListener listener2 = viewer2.getTextMouseDownListener();

    assertNotNull( listener1 );
    assertSame( listener1, listener2 );
  }

  @Test
  public void testGetDefaultDropDownSelectionListenerTwice_DifferentViewerReturnSameInstance() {
    createViewer();
    ClientListener listener1 = viewer.getDropDownSelectionListener();
    DropDownViewer viewer2 = new DropDownViewer( new Text( shell, SWT.NONE ) );
    ClientListener listener2 = viewer2.getDropDownSelectionListener();

    assertNotNull( listener1 );
    assertSame( listener1, listener2 );
  }

  @Test
  public void testGetDefaultDropDownDefaultSelectionListenerTwice_DifferentViewerReturnSameInstance() {
    createViewer();
    ClientListener listener1 = viewer.getDropDownDefaultSelectionListener();
    DropDownViewer viewer2 = new DropDownViewer( new Text( shell, SWT.NONE ) );
    ClientListener listener2 = viewer2.getDropDownDefaultSelectionListener();

    assertNotNull( listener1 );
    assertSame( listener1, listener2 );
  }

  @Test
  public void testGetDefaultDropDownShowListenerTwice_DifferentViewerReturnSameInstance() {
    createViewer();
    ClientListener listener1 = viewer.getDropDownShowListener();
    DropDownViewer viewer2 = new DropDownViewer( new Text( shell, SWT.NONE ) );
    ClientListener listener2 = viewer2.getDropDownShowListener();

    assertNotNull( listener1 );
    assertSame( listener1, listener2 );
  }

  @Test
  public void testGetDefaultDropDownHideListenerTwice_DifferentViewerReturnSameInstance() {
    createViewer();
    ClientListener listener1 = viewer.getDropDownHideListener();
    DropDownViewer viewer2 = new DropDownViewer( new Text( shell, SWT.NONE ) );
    ClientListener listener2 = viewer2.getDropDownHideListener();

    assertNotNull( listener1 );
    assertSame( listener1, listener2 );
  }

  @Test
  public void testConstructor_LinksTextToRemoteObject() {
    createViewer();

    assertEquals( viewer.getModel().getId(), text.getData( VIEWER_LINK ) );
  }

  @Test
  public void testConstructor_LinksDropDownToRemoteObject() {
    createViewer();

    assertEquals( viewer.getModel().getId(), dropDown.getData( VIEWER_LINK ) );
  }

  @Test
  public void testConstructor_LinksRemoteObjectToDropDown() {
    createViewer();

    String expected = WidgetUtil.getId( dropDown );
    verify( remoteObject ).set( eq( DROPDOWN_KEY ), eq( expected ) );
  }

  @Test
  public void testLinkRemoteObjectToText() {
    createViewer();

    String expected = WidgetUtil.getId( text );
    verify( remoteObject ).set( eq( TEXT_KEY ), eq( expected ) );
  }

  @Test
  public void testLinkRemoteObjectToDecorator() {
    createViewer();

    String expected = WidgetUtil.getId( viewer.getDecorator() );
    verify( remoteObject ).set( eq( DECORATOR_KEY ), eq( expected ) );
  }

  @Test
  public void testAddKeysToWidgetDataWhiteList() {
    WidgetDataWhiteListImpl service
      = ( WidgetDataWhiteListImpl )RWT.getClient().getService( WidgetDataWhiteList.class );
    service.setKeys( new String[ 0 ] );

    createViewer();

    List<String> list = Arrays.asList( service.getKeys() );
    assertTrue( list.contains( VIEWER_LINK ) );
  }

  @Test
  public void testAddKeysToWidgetDataWhiteListAndKeepExistingKeys() {
    WidgetDataWhiteListImpl service
      = ( WidgetDataWhiteListImpl )RWT.getClient().getService( WidgetDataWhiteList.class );
    service.setKeys( new String[]{ "foo" } );

    createViewer();

    List<String> list = Arrays.asList( service.getKeys() );
    assertTrue( list.contains( "foo" ) );
  }

  @Test
  public void testAddKeysToWidgetDataWhiteListOnlyOnce() {
    WidgetDataWhiteListImpl service
      = ( WidgetDataWhiteListImpl )RWT.getClient().getService( WidgetDataWhiteList.class );
    service.setKeys( new String[]{ VIEWER_LINK } );

    createViewer();

    List<String> list = Arrays.asList( service.getKeys() );
    assertEquals( list.lastIndexOf( VIEWER_LINK ), list.indexOf( VIEWER_LINK ) );
  }

  @Test
  public void testSetInput_AddsElementsToRemoteObject() {
    createViewer();

    viewer.setInput( INTEGER_LIST );

    JsonArray expected = createJsonArray( "7", "14", "21" );
    verify( remoteObject ).set( eq( ELEMENTS_KEY ), eq( expected ) );
  }

  @Test
  public void testSetInput_ResetsSelectionOnRemoteObject() {
    createViewer();
    reset( remoteObject );

    viewer.setInput( INTEGER_LIST );

    verify( remoteObject ).set( eq( SELECTION_KEY ), eq( -1 ) );
  }

  @Test
  public void testSetLabelProvide_UpdatesElements() {
    createViewer();

    viewer.setInput( INTEGER_LIST );
    viewer.setLabelProvider( new LabelProvider() {
      @Override
      public String getText( Object element ) {
        return "Item " + element;
      }
    } );

    JsonArray expected = createJsonArray( "Item 7", "Item 14", "Item 21" );
    verify( remoteObject ).set( eq( ELEMENTS_KEY ), eq( expected ) );
  }

  @Test
  public void testProcessSelectionChangedEvent() {
    createViewer();
    final List<SelectionChangedEvent> log = new ArrayList<SelectionChangedEvent>();
    viewer.setInput( INTEGER_LIST );
    viewer.addSelectionChangedListener( new ISelectionChangedListener() {
      public void selectionChanged( SelectionChangedEvent event ) {
        log.add( event );
      }
    } );

    JsonObject event = new JsonObject().add( "index", 2 );
    handler.handleNotify( "SelectionChanged", event );

    assertEquals( 1, log.size() );
  }

  @Test
  public void testAddSelectionChangedListener_AddTwiceOnlyAddsOnce() {
    createViewer();
    final List<SelectionChangedEvent> log = new ArrayList<SelectionChangedEvent>();
    viewer.setInput( INTEGER_LIST );
    ISelectionChangedListener listener = new ISelectionChangedListener() {
      public void selectionChanged( SelectionChangedEvent event ) {
        log.add( event );
      }
    };

    viewer.addSelectionChangedListener( listener );
    viewer.addSelectionChangedListener( listener );
    JsonObject event = new JsonObject().add( "index", 2 );
    handler.handleNotify( "SelectionChanged", event );

    assertEquals( 1, log.size() );
  }

  @Test
  public void testProcessSelectionChangedEvent_ElementField() {
    createViewer();
    final List<SelectionChangedEvent> log = new ArrayList<SelectionChangedEvent>();
    viewer.setInput( INTEGER_LIST );
    viewer.addSelectionChangedListener( new ISelectionChangedListener() {
      public void selectionChanged( SelectionChangedEvent event ) {
        log.add( event );
      }
    } );

    JsonObject event = new JsonObject().add( "index", 2 );
    handler.handleNotify( "SelectionChanged", event );

    IStructuredSelection selection = ( IStructuredSelection )log.get( 0 ).getSelection();
    assertEquals( new Integer( 21 ), selection.getFirstElement() );
  }

  @Test
  public void testDestroyRemoteObject() {
    Fixture.fakePhase( PhaseId.PROCESS_ACTION );
    createViewer();

    viewer.getControl().dispose();

    verify( remoteObject ).destroy();
  }

  @Test
  public void testCreateViewerTwice() {
    Fixture.fakePhase( PhaseId.PROCESS_ACTION );
    createViewer();

    try {
      createViewer();
      fail();
    } catch( IllegalStateException ex ) {
      // expected
    }
  }

  @Test
  public void testSetContentProvider() {
    IContentProvider provider = mock( IStructuredContentProvider.class );
    createViewer();

    viewer.setContentProvider( provider );

    assertSame( provider, viewer.getContentProvider() );
  }

  @Test
  public void testSetInput_CallsContentProvider() {
    createViewer();
    IStructuredContentProvider provider = mock( IStructuredContentProvider.class );
    stub( provider.getElements( anyObject() ) ).toReturn( new Object[]{} );
    viewer.setContentProvider( provider );
    Object input = new Object();

    viewer.setInput( input );

    verify( provider ).getElements( eq( input ) );
  }

  @Test
  public void testSetContentProvider_CallsContentProvider() {
    createViewer();
    IStructuredContentProvider provider = mock( IStructuredContentProvider.class );
    stub( provider.getElements( anyObject() ) ).toReturn( new Object[]{} );
    viewer.setInput( INTEGER_LIST );

    viewer.setContentProvider( provider );

    verify( provider, times( 1 ) ).getElements( eq( INTEGER_LIST ) );
  }

  @Test
  public void testSetInput_CallsInputChanged() {
    createViewer();
    viewer.setInput( INTEGER_LIST );
    IStructuredContentProvider provider = mock( IStructuredContentProvider.class );
    stub( provider.getElements( anyObject() ) ).toReturn( new Object[]{} );
    viewer.setContentProvider( provider );
    List<Integer> newList = new ArrayList<Integer>();

    viewer.setInput( newList );

    verify( provider ).inputChanged( same( viewer ), eq( INTEGER_LIST ), eq( newList ) );
  }

  @Test
  public void testCallDisposeOnContentProvide() {
    Fixture.fakePhase( PhaseId.PROCESS_ACTION );
    createViewer();
    IStructuredContentProvider provider = mock( IStructuredContentProvider.class );
    viewer.setContentProvider( provider );

    viewer.getControl().dispose();

    verify( provider ).dispose();
  }

  //////////
  // Helpers

  private void createViewer() {
    viewer = new DropDownViewer( text );
    dropDown = viewer.getDropDown();
    viewer.setContentProvider( new MyContentProvider() );
    viewer.setLabelProvider( new LabelProvider() );
  }

  private static class MyContentProvider implements IStructuredContentProvider {

    public void dispose() {
    }

    public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
    }

    @SuppressWarnings( "unchecked" )
    public Object[] getElements( Object inputElement ) {
      return ( ( List<Integer> )inputElement ).toArray();
    }

  }

}
