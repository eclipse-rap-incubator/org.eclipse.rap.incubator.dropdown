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
package org.eclipse.rap.addons.dropdown.demo;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.application.Application;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;


public class DropDownApplicationConfiguration implements ApplicationConfiguration {

  public void configure( Application application ) {
    application.addStyleSheet( RWT.DEFAULT_THEME_ID, "theme/theme.css" );
    application.addEntryPoint( "/dropdown", DropDownDemo.class, null );
  }

}
