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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.eclipse.rap.clientscripting.ClientListener;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class DropDownViewer_Test {

  // TODO : test reading scripts and attaching listener when possible

  private Display display;
  private Text text;
  private DropDownViewer viewer;
  private Shell shell;

  @Before
  public void setUp() {
    Fixture.setUp();
    display = new Display();
    shell = new Shell( display );
    text = new Text( shell, SWT.NONE );
    Fixture.fakeNewRequest();
    viewer = new DropDownViewer( text );
//    connection = mock( Connection.class );
//    Fixture.fakeConnection( connection );
//    when( connection.createRemoteObject( anyString() ) ).thenReturn( remoteObject );
//    remoteObject = mock( RemoteObjectImpl.class );
  }

  @After
  public void tearDown() {
    Fixture.tearDown();
  }

  @Test
  public void testContructor_CreatesDropDownWithParent() {
    assertSame( text, viewer.getDropDown().getParent() );
  }

  @Test
  public void testGetDefaultTextModifyListenerTwice_ReturnsSameInstance() {
    ClientListener listener1 = viewer.getTextModifyListener();
    ClientListener listener2 = viewer.getTextModifyListener();

    assertNotNull( listener1 );
    assertSame( listener1, listener2 );
  }

  @Test
  public void testGetDefaultTextModifyListenerTwice_DifferentViewerReturnSameInstance() {
    ClientListener listener1 = viewer.getTextModifyListener();
    DropDownViewer viewer2 = new DropDownViewer( new Text( shell, SWT.NONE ) );
    ClientListener listener2 = viewer2.getTextModifyListener();

    assertNotNull( listener1 );
    assertSame( listener1, listener2 );
  }

  @Test
  public void testGetDefaultTextVerifyListenerTwice_DifferentViewerReturnSameInstance() {
    ClientListener listener1 = viewer.getTextVerifyListener();
    DropDownViewer viewer2 = new DropDownViewer( new Text( shell, SWT.NONE ) );
    ClientListener listener2 = viewer2.getTextVerifyListener();

    assertNotNull( listener1 );
    assertSame( listener1, listener2 );
  }

  @Test
  public void testGetDefaultTextKeyDownListenerTwice_DifferentViewerReturnSameInstance() {
    ClientListener listener1 = viewer.getTextKeyDownListener();
    DropDownViewer viewer2 = new DropDownViewer( new Text( shell, SWT.NONE ) );
    ClientListener listener2 = viewer2.getTextKeyDownListener();

    assertNotNull( listener1 );
    assertSame( listener1, listener2 );
  }

  @Test
  public void testGetDefaultDropDownSelectionListenerTwice_DifferentViewerReturnSameInstance() {
    ClientListener listener1 = viewer.getDropDownSelectionListener();
    DropDownViewer viewer2 = new DropDownViewer( new Text( shell, SWT.NONE ) );
    ClientListener listener2 = viewer2.getDropDownSelectionListener();

    assertNotNull( listener1 );
    assertSame( listener1, listener2 );
  }

  @Test
  public void testGetDefaultDropDownDefaultSelectionListenerTwice_DifferentViewerReturnSameInstance() {
    ClientListener listener1 = viewer.getDropDownDefaultSelectionListener();
    DropDownViewer viewer2 = new DropDownViewer( new Text( shell, SWT.NONE ) );
    ClientListener listener2 = viewer2.getDropDownDefaultSelectionListener();

    assertNotNull( listener1 );
    assertSame( listener1, listener2 );
  }

}
