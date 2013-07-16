/*******************************************************************************
 * Copyright (c) 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.addons.dropdown;

import static org.eclipse.rap.rwt.lifecycle.WidgetUtil.getId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.rap.clientscripting.ClientListener;
import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.lifecycle.PhaseId;
import org.eclipse.rap.rwt.remote.Connection;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.junit.*;


public class AutoSuggest_Test {

  private Display display;
  private Text text;
  private RemoteObject remoteObject;

  private static final String REMOTE_TYPE = "rwt.remote.Model";
  private Connection connection;

  @Before
  public void setUp() {
    Fixture.setUp();
    Fixture.fakePhase( PhaseId.PROCESS_ACTION );
    display = new Display();
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

    verify( connection ).createRemoteObject( eq( REMOTE_TYPE  ) );
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

  @Test
  public void testGetVisibleItemCount_getsDropDownVisibleItemCount() {
    AutoSuggest autoSuggest = new AutoSuggest( text );
    DropDown dropDown = autoSuggest.getDropDown();

    dropDown.setVisibleItemCount( 23 );

    assertEquals( 23, autoSuggest.getVisibleItemCount() );
  }

  @Test( expected = NullPointerException.class )
  public void testSetData_failsWithNullArgument() {
    AutoSuggest autoSuggest = new AutoSuggest( text );

    autoSuggest.setData( null );
  }

  @Test
  public void testSetData_setsElementsOnRemoteObject() {
    AutoSuggest autoSuggest = new AutoSuggest( text );

    autoSuggest.setData( new String[]{ "foo", "bar" } );

    verify( remoteObject ).set( eq( "elements" ), eq( new JsonArray().add( "foo" ).add( "bar" ) ) );
  }

  private void mockRemoteObject() {
    remoteObject = mock( RemoteObject.class );
    when( remoteObject.getId() ).thenReturn( "foo" );
    connection = spy( RWT.getUISession().getConnection() );
    when( connection.createRemoteObject( REMOTE_TYPE ) ).thenReturn( remoteObject );
    Fixture.fakeConnection( connection );
  }

}
