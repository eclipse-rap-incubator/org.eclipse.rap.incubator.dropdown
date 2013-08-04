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
package org.eclipse.rap.addons.autosuggest;

import org.eclipse.rap.clientscripting.internal.ClientFunction;


@SuppressWarnings( "restriction" )
public class AutoSuggestClientListener extends ClientFunction {

  public AutoSuggestClientListener( String scriptCode ) {
    super( scriptCode );
  }

  public String getId() {
    return getRemoteId();
  }

}
