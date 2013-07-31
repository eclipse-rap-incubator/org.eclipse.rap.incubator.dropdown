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

import org.eclipse.rap.clientscripting.Script;
import org.eclipse.rap.rwt.SingletonUtil;


public class EventDelegatorScript extends Script {

  public static EventDelegatorScript getInstance() {
    return SingletonUtil.getSessionInstance( EventDelegatorScript.class );
  }

  private EventDelegatorScript() {
    super( getText() );
  }

  private static String getText() {
    String path = "org/eclipse/rap/addons/autosuggest/internal/resources/EventDelegator.js";
    return ResourceLoaderUtil.readTextContent( path );
  }

}
