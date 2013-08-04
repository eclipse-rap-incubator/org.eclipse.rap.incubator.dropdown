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
package org.eclipse.rap.addons.dropdown.demo.scripts;

import org.eclipse.rap.addons.autosuggest.AutoSuggestClientListener;
import org.eclipse.rap.rwt.SingletonUtil;


public class CustomAutoSuggestClientListener extends AutoSuggestClientListener {

  public static CustomAutoSuggestClientListener getInstance() {
    return SingletonUtil.getSessionInstance( CustomAutoSuggestClientListener.class );
  }

  private CustomAutoSuggestClientListener() {
    super( getText() );
  }

  private static String getText() {
    String path = "org/eclipse/rap/addons/dropdown/demo/scripts/CustomAutoSuggest.js";
    return ResourceLoaderUtil.readTextContent( path );
  }

}
