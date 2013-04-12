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

package org.eclipse.rap.rwt.client;

import java.util.Map;

import org.eclipse.rap.rwt.remote.OperationHandler;


public abstract class OperationAdapter implements OperationHandler {

  public void handleSet( Map<String, Object> properties ) {
    // TODO Auto-generated method stub
  }

  public void handleCall( String method, Map<String, Object> parameters ) {
    // TODO Auto-generated method stub
  }

  public void handleNotify( String event, Map<String, Object> properties ) {
    // TODO Auto-generated method stub
  }
}
