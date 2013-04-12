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

import static org.junit.Assert.assertSame;

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

  private Display display;
  private Text text;
  private DropDownViewer viewer;

  @Before
  public void setUp() {
    Fixture.setUp();
    display = new Display();
    Shell shell = new Shell( display );
    text = new Text( shell, SWT.NONE );
    Fixture.fakeNewRequest();
    viewer = new DropDownViewer( text );
  }

  @After
  public void tearDown() {
    Fixture.tearDown();
  }

  @Test
  public void testContructor_CreatesDropDownWithParent() {
    assertSame( text, viewer.getDropDown().getParent() );
  }

}
