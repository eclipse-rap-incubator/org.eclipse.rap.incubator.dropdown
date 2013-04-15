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

package org.eclipse.rap.rwt.remote;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.rap.rwt.internal.remote.RemoteObjectImpl;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


@SuppressWarnings("restriction")
public class UniversalRemoteObject_Test {

  private static final String REMOTE_TYPE = "rwt.remote.UniversalRemoteObject";
  private Connection connection;
  private RemoteObjectImpl remoteObject;
  private UniversalRemoteObject uro;

  @Before
  public void setUp() {
    Fixture.setUp();
    Fixture.fakeNewRequest();
    connection = mock( Connection.class );
    Fixture.fakeConnection( connection );
    remoteObject = mock( RemoteObjectImpl.class );
    when( connection.createRemoteObject( eq( REMOTE_TYPE ) ) ).thenReturn( remoteObject );
    when( remoteObject.getId() ).thenReturn( "r11" );
    uro = new UniversalRemoteObject();
  }

  @After
  public void tearDown() {
    Fixture.tearDown();
  }

  @Test
  public void testContructor_CreatesRealRemoteObject() {
    verify( connection ).createRemoteObject( eq( REMOTE_TYPE ) );
  }

  @Test
  public void testGetId_ReturnsId() {
    assertEquals( "r11", uro.getId() );
  }

}
