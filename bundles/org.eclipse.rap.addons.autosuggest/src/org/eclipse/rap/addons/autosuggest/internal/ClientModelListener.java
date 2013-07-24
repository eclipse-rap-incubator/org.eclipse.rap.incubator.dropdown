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
package org.eclipse.rap.addons.autosuggest.internal;

import org.eclipse.rap.clientscripting.internal.ClientFunction;
import org.eclipse.rap.clientscripting.internal.ClientListenerBinding;
import org.eclipse.rap.json.JsonObject;


@SuppressWarnings( "restriction" )
public final class ClientModelListener extends ClientFunction implements ModelListener {

  public ClientModelListener( String scriptCode ) {
    super( scriptCode );
  }

  public void handleEvent( JsonObject argument ) { }

  void addTo( Model model, String eventType ) {
    final ClientListenerBinding binding = addTo( model.getId(), eventType );
    if( binding != null ) {
      model.addListener( "destroy", new ModelListener() {
        public void handleEvent( JsonObject argument ) {
          binding.dispose();
        }
      } );
    }
  }

  void removeFrom( Model model, String eventType ) {
    removeFrom( model.getId(), eventType );
  }

  // For Testing only:
  ClientListenerBinding findBinding( Model model, String eventType ) {
    return findBinding( model.getId(), eventType );
  }

}
