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
package org.eclipse.rap.addons.autosuggest.internal.resources;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.JavaScriptLoader;


public final class ModelResources {

  public static final String SCRIPT = "rwt/remote/Model.js";

  private static final boolean DEBUG = true;

  public static void ensure() {
    ensureRegistered();
    ensureLoaded();
  }

  private static void ensureRegistered() {
    if( DEBUG && RWT.getResourceManager().isRegistered( SCRIPT ) ) {
      deregister();
    }
    if( !RWT.getResourceManager().isRegistered( SCRIPT ) ) {
      try {
        register();
      } catch( IOException exception ) {
        throw new RuntimeException( "Failed to register resources", exception );
      }
    }
  }

  private static void ensureLoaded() {
    JavaScriptLoader loader = RWT.getClient().getService( JavaScriptLoader.class );
    loader.require( RWT.getResourceManager().getLocation( SCRIPT ) );
  }

  private static void register() throws IOException {
    InputStream inputStream = getResourceAsStream( SCRIPT );
    try {
      RWT.getResourceManager().register( SCRIPT, inputStream );
    } finally {
      inputStream.close();
    }
  }

  private static void deregister() {
    RWT.getResourceManager().unregister( SCRIPT );
  }

  public static InputStream getResourceAsStream( String resourceName ) {
    ClassLoader classLoader = ModelResources.class.getClassLoader();
    InputStream inputStream = classLoader.getResourceAsStream( resourceName );
    if( inputStream == null ) {
      throw new RuntimeException( "Resource not found: " + resourceName );
    }
    return inputStream;
  }

  private ModelResources() {
  }

}
