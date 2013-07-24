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
package org.eclipse.rap.addons.dropdown.test.internal;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.rap.addons.dropdown.internal.resources.DropDownResources;
import org.eclipse.rap.rwt.jstest.TestContribution;


@SuppressWarnings( "restriction" )
public class DropDownResourcesContribution implements TestContribution {

  public String getName() {
    return "dropdown-resources";
  }

  public String[] getResources() {
    return DropDownResources.SCRIPTS;
  }

  public InputStream getResourceAsStream( String resourceName ) throws IOException {
    return DropDownResources.getResourceAsStream( resourceName );
  }

}
