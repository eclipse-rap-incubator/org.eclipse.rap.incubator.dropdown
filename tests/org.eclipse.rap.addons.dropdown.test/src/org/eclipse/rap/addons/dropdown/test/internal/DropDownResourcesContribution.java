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
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.rap.addons.dropdown.internal.resources.DropDownResources;
import org.eclipse.rap.addons.dropdown.viewer.internal.remote.UniversalRemoteObject;
import org.eclipse.rap.rwt.jstest.TestContribution;

@SuppressWarnings( "restriction" )
public class DropDownResourcesContribution implements TestContribution {

  public static String[] ADDITIONAL_RESOURCES = new String[] {
    "rwt/remote/UniversalRemoteObject.js"
  };

  public static String[] concat( String[] array1, String[] array2 ) {
    ArrayList<String> baseArray = new ArrayList< String >( Arrays.asList( array1 ) );
    baseArray.addAll( Arrays.asList( array2 ) );
    return baseArray.toArray( new String[ baseArray.size() ] );
  }

  public String getName() {
    return "dropdown-resources";
  }

  public String[] getResources() {
    return concat( DropDownResources.SCRIPTS, ADDITIONAL_RESOURCES );
  }

  public InputStream getResourceAsStream( String resourceName ) throws IOException {
    if( resourceName.equals( ADDITIONAL_RESOURCES[0] ) ) {
      return UniversalRemoteObject.class.getClassLoader().getResourceAsStream( resourceName );
    }
    return DropDownResources.getResourceAsStream( resourceName );
  }

}
