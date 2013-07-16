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

import static org.junit.Assert.*;

import org.eclipse.rap.rwt.testfixture.Fixture;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.junit.*;


public class AutoSuggest_Test {

  private Display display;
  private Text text;

  @Before
  public void setUp() {
    Fixture.setUp();
    display = new Display();
    Shell shell = new Shell( display );
    text = new Text( shell, SWT.NONE );
//    Fixture.fakeNewRequest();
//    remoteObject = mock( RemoteObject.class );
//    when( remoteObject.getId() ).thenReturn( "foo" );
//    Connection connection = spy( RWT.getUISession().getConnection() );
//    when( connection.createRemoteObject( REMOTE_TYPE ) ).thenReturn( remoteObject );
//    Fixture.fakeConnection( connection );
//    doAnswer( new Answer<Object>(){
//      public Object answer( InvocationOnMock invocation ) throws Throwable {
//        handler = ( OperationHandler )invocation.getArguments()[ 0 ];
//        return null;
//      }
//    } ).when( remoteObject ).setHandler( any( OperationHandler.class ) );
  }

  @After
  public void tearDown() {
    Fixture.tearDown();
  }

  @Test
  public void testConstructor() {
    new AutoSuggest( text );
  }

  @Test( expected = NullPointerException.class )
  public void testConstructor_failsWithNull() {
    new AutoSuggest( null );
  }

  @Test
  public void testConstructor_createsDropDownWithParent() {
    AutoSuggest autoSuggest = new AutoSuggest( text );

    DropDown dropDown = autoSuggest.getDropDown();
    assertSame( text, dropDown.getParent() );
  }

  @Test
  public void testDispose_disposesDropDown() {
    AutoSuggest autoSuggest = new AutoSuggest( text );
    DropDown dropDown = autoSuggest.getDropDown();

    autoSuggest.dispose();

    assertTrue( dropDown.isDisposed() );
  }

  @Test
  public void testDispose_disposeTwice() {
    AutoSuggest autoSuggest = new AutoSuggest( text );

    autoSuggest.dispose();
    autoSuggest.dispose();
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructor_failsWithDisposedText() {
    text.dispose();
    new AutoSuggest( text );
  }

  @Test( expected = NullPointerException.class )
  public void testSetData_failsWithNullArgument() {
    AutoSuggest autoSuggest = new AutoSuggest( text );

    autoSuggest.setData( null );
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

}
