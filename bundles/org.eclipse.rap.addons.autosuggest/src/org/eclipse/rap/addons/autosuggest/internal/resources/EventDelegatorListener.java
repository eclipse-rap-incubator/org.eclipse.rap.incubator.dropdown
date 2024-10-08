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

import org.eclipse.rap.rwt.SingletonUtil;
import org.eclipse.rap.rwt.scripting.ClientListener;


public class EventDelegatorListener  {

  private final ClientListener eventDelegatorListener;

  // Can not extend ClientListener since Widget#addListner would not detect it
  public static ClientListener getInstance() {
    return SingletonUtil.getSessionInstance( EventDelegatorListener.class ).eventDelegatorListener;
  }

  private EventDelegatorListener() {
    eventDelegatorListener = new ClientListener( getText() );
  }

  private static String getText() {
    String path = "org/eclipse/rap/addons/autosuggest/internal/resources/EventDelegator.js";
    return ResourceLoaderUtil.readTextContent( path );
  }

}
