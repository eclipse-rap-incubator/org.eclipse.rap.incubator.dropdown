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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.*;
import java.util.List;

import org.eclipse.jface.viewers.*;
import org.eclipse.rap.addons.dropdown.DropDown;
import org.eclipse.rap.clientscripting.ClientListener;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.client.WidgetDataWhiteList;
import org.eclipse.rap.rwt.lifecycle.PhaseId;
import org.eclipse.rap.rwt.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.junit.*;


@SuppressWarnings("restriction")
public class DropDownViewer_Test {

  // TODO : test reading scripts and attaching listener when possible

  private static final List<Integer> INTEGER_LIST = Arrays.asList(
    Integer.valueOf( 7 ),
    Integer.valueOf( 14 ),
    Integer.valueOf( 21 )
  );
  private static final String VIEWER_LINK =
      "org.eclipse.rap.addons.dropdown.viewer.DropDownViewer#viewer";
  private static String DROPDOWN_KEY = "dropDown";
  private static final String TEXT_KEY = "text";
  private static String ELEMENTS_KEY = "elements";
  private Display display;
  private Text text;
  private DropDownViewer viewer;
  private Shell shell;
  private DropDown dropDown;

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
  public void testContructor_CreatesDropDownWithParent() {
    createViewer();
    assertSame( text, viewer.getDropDown().getParent() );
  }

  @Test
  public void testConstructor_SetEmptyElements() {
    createViewer();

    List<String> expected = new ArrayList<String>();
    assertEquals( expected, Arrays.asList( getElements() ) );
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
  public void testLinkTextToRemoteObject() {
    createViewer();

    assertEquals( viewer.getRemoteObject().getId(), text.getData( VIEWER_LINK ) );
  }

  @Test
  public void testLinkDropDownToRemoteObject() {
    createViewer();

    assertEquals( viewer.getRemoteObject().getId(), dropDown.getData( VIEWER_LINK ) );
  }

  @Test
  public void testLinkRemoteObjectToDropDown() {
    createViewer();

    String expected = WidgetUtil.getId( dropDown );
    assertEquals( expected, viewer.getRemoteObject().getString( DROPDOWN_KEY ) );
  }

  @Test
  public void testLinkRemoteObjectToText() {
    createViewer();

    String expected = WidgetUtil.getId( text );
    assertEquals( expected, viewer.getRemoteObject().getString( TEXT_KEY ) );
  }

  @Test
  public void testAddKeysToWidgetDataWhiteList() {
    WidgetDataWhiteList service = RWT.getClient().getService( WidgetDataWhiteList.class );
    service.setKeys( new String[ 0 ] );

    createViewer();

    List<String> list = Arrays.asList( service.getKeys() );
    assertTrue( list.contains( VIEWER_LINK ) );
  }

  @Test
  public void testAddKeysToWidgetDataWhiteListAndKeepExistingKeys() {
    WidgetDataWhiteList service = RWT.getClient().getService( WidgetDataWhiteList.class );
    service.setKeys( new String[]{ "foo" } );

    createViewer();

    List<String> list = Arrays.asList( service.getKeys() );
    assertTrue( list.contains( "foo" ) );
  }

  @Test
  public void testAddKeysToWidgetDataWhiteListOnlyOnce() {
    WidgetDataWhiteList service = RWT.getClient().getService( WidgetDataWhiteList.class );
    service.setKeys( new String[]{ VIEWER_LINK } );

    createViewer();

    List<String> list = Arrays.asList( service.getKeys() );
    assertEquals( list.lastIndexOf( VIEWER_LINK ), list.indexOf( VIEWER_LINK ) );
  }

  @Test
  public void testSetInput_AddsElementsToRemoteObject() {
    createViewer();

    viewer.setInput( INTEGER_LIST );

    String[] result = getElements();
    List<String> expected = Arrays.asList( new String[]{ "7", "14", "21"} );
    assertEquals( expected, Arrays.asList( result ) );
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

    String[] result = getElements();
    List<String> expected = Arrays.asList( new String[]{ "Item 7", "Item 14", "Item 21"} );
    assertEquals( expected, Arrays.asList( result ) );
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

    Map<String, Object> event = createMap( "index", Integer.valueOf( 2 ) );
    viewer.getRemoteObject().notify( "SelectionChanged", event );

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
    Map<String, Object> event = createMap( "index", Integer.valueOf( 2 ) );
    viewer.getRemoteObject().notify( "SelectionChanged", event );

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

    Map<String, Object> event = createMap( "index", Integer.valueOf( 2 ) );
    viewer.getRemoteObject().notify( "SelectionChanged", event );

    IStructuredSelection selection = ( IStructuredSelection )log.get( 0 ).getSelection();
    assertEquals( new Integer( 21 ), selection.getFirstElement() );
  }

  @Test
  public void testDestroyRemoteObject() {
    Fixture.fakePhase( PhaseId.PROCESS_ACTION );
    createViewer();

    viewer.getControl().dispose();

    assertTrue( viewer.getRemoteObject().isDestroyed() );
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

  private String[] getElements() {
    return ( String[] )viewer.getRemoteObject().get( ELEMENTS_KEY );
  }

  private Map<String, Object> createMap( String key, Object value ) {
    Map<String, Object> event = new HashMap<String, Object>();
    event.put( key, value );
    return event;
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
