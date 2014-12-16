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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.rwt.remote.Connection;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.rap.rwt.testfixture.internal.Fixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


@SuppressWarnings( "restriction" )
public class DataSource_Test {

  private static final String REMOTE_TYPE = "rwt.remote.Model";
  private Connection connection;
  private RemoteObject remoteObject;
  private DataSource dataSource;

  @Before
  public void setUp() {
    Fixture.setUp();
    connection = mock( Connection.class );
    remoteObject = mock( RemoteObject.class );
    when( connection.createRemoteObject( anyString() ) ).thenReturn( remoteObject );
    when( remoteObject.getId() ).thenReturn( "idFoo" );
    Fixture.fakeConnection( connection );
    dataSource = new DataSource();
  }

  @After
  public void tearDown() {
    Fixture.tearDown();
  }

  @Test
  public void testConstructor_createsRemoteObject() {
    verify( connection ).createRemoteObject( eq( REMOTE_TYPE ) );
  }

  @Test
  public void testGetId() {
    assertEquals( "idFoo", dataSource.getId() );
  }

  @Test ( expected = NullPointerException.class )
  public void testSetDataProvider_failsWithNullArgument() {
    dataSource.setDataProvider( null );
  }

  @Test
  public void testSetDataProvider_setsDataOnRemoteObject() {
    dataSource.setDataProvider( new ArrayDataProvider( "foo", "bar" ) );

    JsonArray array = new JsonArray().add( "foo" ).add( "bar" );
    verify( remoteObject ).set( eq( "data" ), eq( array ) );
  }

  @Test( expected = IllegalStateException.class )
  public void testSetDataProvider_failsIfDisposed() {
    dataSource.dispose();

    dataSource.setDataProvider( new ArrayDataProvider( "foo", "bar" ) );
  }

  @Test
  public void testSetDataProvider_setsDataOnRemoteObject_forColumnDataProvider() {
    dataSource.setDataProvider( new ColumnDataProvider() {
      public Iterable<?> getSuggestions() {
        return Arrays.asList( "foo", "bar" );
      }
      public String getValue( Object element ) {
        return ( String )element;
      }
      public String[] getTexts( Object element ) {
        return new String[] { element + "-1", element + "-2" };
      }
    } );

    JsonArray array = new JsonArray();
    array.add( new JsonArray().add( "foo" ).add( "foo-1").add( "foo-2") );
    array.add( new JsonArray().add( "bar" ).add( "bar-1").add( "bar-2") );
    verify( remoteObject ).set( eq( "data" ), eq( array ) );
  }

  @Test
  public void testSetFilterScript_setsFilterScriptOnRemoteObject() {
    dataSource.setFilterScript( "foobar" );

    verify( remoteObject ).set( eq( "filterScript" ), eq( "foobar" ) );
  }

  @Test( expected = IllegalStateException.class )
  public void testSetFilterScript_failsIfDisposed() {
    dataSource.dispose();

    dataSource.setFilterScript( "foobar" );
  }

  @Test
  public void testSetTemplate() {
    ColumnTemplate template = mock( ColumnTemplate.class );

    dataSource.setTemplate( template );

    assertSame( template, dataSource.getTemplate() );
  }

  @Test( expected = IllegalStateException.class )
  public void testSetTemplate_failsIfDisposed() {
    dataSource.dispose();

    dataSource.setTemplate( mock( ColumnTemplate.class ) );
  }

  @Test
  public void testIsDisposed_returnsFalse() {
    assertFalse( dataSource.isDisposed() );
  }

  @Test
  public void testIsDisposed_returnsTrueAfterDispose() {
    dataSource.dispose();

    assertTrue( dataSource.isDisposed() );
  }

  @Test
  public void testDispose_destroyRemoteObject() {
    dataSource.dispose();

    verify( remoteObject ).destroy();
  }

  @Test
  public void testDispose_callingTwicedestroysRemoteObjectOnce() {
    dataSource.dispose();
    dataSource.dispose();

    verify( remoteObject, times( 1 ) ).destroy();
  }
}
