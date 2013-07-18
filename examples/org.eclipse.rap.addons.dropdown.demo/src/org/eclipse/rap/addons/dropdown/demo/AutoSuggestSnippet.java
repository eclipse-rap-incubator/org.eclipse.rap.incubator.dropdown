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
package org.eclipse.rap.addons.dropdown.demo;

import org.eclipse.rap.addons.dropdown.AutoSuggest;
import org.eclipse.rap.addons.dropdown.SimpleDataProvider;
import org.eclipse.rap.rwt.application.AbstractEntryPoint;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;


public class AutoSuggestSnippet extends AbstractEntryPoint {

  @Override
  protected void createContents( Composite parent ) {
    Text text = new Text( parent, SWT.BORDER );
    AutoSuggest autoSuggest = new AutoSuggest( text );
    SimpleDataProvider dataProvider
      = new SimpleDataProvider( new String[]{ "foo", "food", "foobar", "bar" } );
    autoSuggest.setDataProvider( dataProvider );
    autoSuggest.setAutoComplete( true );
  }

}
