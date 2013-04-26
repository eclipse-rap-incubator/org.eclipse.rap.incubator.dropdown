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

package org.eclipse.rap.addons.dropdown.internal;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.rap.rwt.jstest.TestContribution;


public class DropDownTestContribution implements TestContribution {

  private static final ClassLoader CLASSLOADER = DropDownTestContribution.class.getClassLoader();

  private static final String[] ALL_TEST_RESOURCES = new String[] {
    "/rwt/dropdown/DropDown_Test.js",
    "/rwt/remote/UniversalRemoteObject_Test.js",
  };

  public String getName() {
    return "dropdown-tests";
  }

  public String[] getResources() {
    return ALL_TEST_RESOURCES;
  }

  public InputStream getResourceAsStream( String resourceName ) throws IOException {
    return CLASSLOADER.getResourceAsStream( resourceName );
  }

}
