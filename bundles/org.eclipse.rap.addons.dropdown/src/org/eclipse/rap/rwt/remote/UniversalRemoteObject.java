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

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.JavaScriptLoader;
import org.eclipse.rap.rwt.internal.remote.RemoteObjectImpl;
import org.eclipse.rap.rwt.service.ResourceManager;

@SuppressWarnings("restriction")
public class UniversalRemoteObject {

  private static final String REMOTE_TYPE = "rwt.remote.UniversalRemoteObject";
  private static final String UNIVERSAL_REMOTE_OBJECT_JS = "UniversalRemoteObject.js";

  private RemoteObjectImpl remoteObject;


  public UniversalRemoteObject() {
    ensureTypeHandler();
    remoteObject
      = ( RemoteObjectImpl )RWT.getUISession().getConnection().createRemoteObject( REMOTE_TYPE );
  }

  public String getId() {
    return remoteObject.getId();
  }

  private void ensureTypeHandler() {
    ResourceManager manager = RWT.getResourceManager();
    if( !manager.isRegistered( UNIVERSAL_REMOTE_OBJECT_JS ) ) {
      manager.register(
        UNIVERSAL_REMOTE_OBJECT_JS,
        getClass().getResourceAsStream( UNIVERSAL_REMOTE_OBJECT_JS )
      );
    }
    JavaScriptLoader jsl = RWT.getClient().getService( JavaScriptLoader.class );
    jsl.require( manager.getLocation( UNIVERSAL_REMOTE_OBJECT_JS ) );
  }

}
