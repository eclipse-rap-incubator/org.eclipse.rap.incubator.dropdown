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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.rwt.remote.Connection;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class SimpleDataSource_Test {

  private Connection connection;
  private RemoteObject remoteObject;

  @Before
  public void setUp() {
    Fixture.setUp();
    connection = mock( Connection.class );
    remoteObject = mock( RemoteObject.class );
    when( connection.createRemoteObject( anyString() ) ).thenReturn( remoteObject );
    when( remoteObject.getId() ).thenReturn( "idFoo" );
    Fixture.fakeConnection( connection );
  }

  @After
  public void tearDown() {
    Fixture.tearDown();
  }

  @Test ( expected = NullPointerException.class )
  public void testConstructor_failsWithNullArgument() {
    new SimpleDataSource( null );
  }

  @Test
  public void testConstructor_setsData() {
    final AtomicReference<JsonArray> captor = new AtomicReference<JsonArray>();
    new SimpleDataSource( new String[]{ "foo", "bar" } ) {
      @Override
      protected void setData( JsonArray array ) {
        captor.set( array );
      }
    };

    assertEquals( new JsonArray().add( "foo" ).add( "bar" ), captor.get() );
  }

}
