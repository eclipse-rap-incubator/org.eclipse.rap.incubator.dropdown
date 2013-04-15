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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.rap.clientscripting.ClientListener;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.client.WidgetDataWhiteList;
import org.eclipse.rap.rwt.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


@SuppressWarnings("restriction")
public class DropDownViewer_Test {

  // TODO : test reading scripts and attaching listener when possible

  private static final String VIEWER_LINK =
      "org.eclipse.rap.addons.dropdown.DropDownViewer#viewer";
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

    List< String > list = Arrays.asList( service.getKeys() );;
    assertTrue( list.contains( VIEWER_LINK ) );
  }

  @Test
  public void testAddKeysToWidgetDataWhiteListAndKeepExistingKeys() {
    WidgetDataWhiteList service = RWT.getClient().getService( WidgetDataWhiteList.class );
    service.setKeys( new String[]{ "foo" } );

    createViewer();

    List< String > list = Arrays.asList( service.getKeys() );;
    assertTrue( list.contains( "foo" ) );
  }

  @Test
  public void testAddKeysToWidgetDataWhiteListOnlyOnce() {
    WidgetDataWhiteList service = RWT.getClient().getService( WidgetDataWhiteList.class );
    service.setKeys( new String[]{ VIEWER_LINK } );

    createViewer();

    List< String > list = Arrays.asList( service.getKeys() );;
    assertEquals( list.lastIndexOf( VIEWER_LINK ), list.indexOf( VIEWER_LINK ) );
  }

  @Test
  public void testSetInput_AddsElementsToRemoteObject() {
    createViewer();
    List<?> input = Arrays.asList( 7, 14, 21 );

    viewer.setLabelProvider( new LabelProvider() );
    viewer.setInput( input );

    String[] result = getElements();
    List< String > expected = Arrays.asList( new String[]{ "7", "14", "21"} );
    assertEquals( expected, Arrays.asList( result ) );
  }

  @Test
  public void testSetLabelProvide_UpdatesElements() {
    createViewer();
    List<?> input = Arrays.asList( 7, 14, 21 );

    viewer.setLabelProvider( new LabelProvider() );
    viewer.setInput( input );
    viewer.setLabelProvider( new LabelProvider() {
      @Override
      public String getText( Object element ) {
        return "Item " + element;
      };
    } );

    String[] result = getElements();
    List< String > expected = Arrays.asList( new String[]{ "Item 7", "Item 14", "Item 21"} );
    assertEquals( expected, Arrays.asList( result ) );
  }


  //////////
  // Helpers

  private void createViewer() {
    viewer = new DropDownViewer( text );
    dropDown = viewer.getDropDown();
  }

  private String[] getElements() {
    return ( String[] )viewer.getRemoteObject().get( ELEMENTS_KEY );
  }


}
