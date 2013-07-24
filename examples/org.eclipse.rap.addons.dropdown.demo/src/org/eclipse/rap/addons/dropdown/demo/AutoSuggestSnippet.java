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

import org.eclipse.rap.addons.autosuggest.ArrayDataProvider;
import org.eclipse.rap.addons.autosuggest.AutoSuggest;
import org.eclipse.rap.addons.autosuggest.DataSource;
import org.eclipse.rap.addons.autosuggest.SuggestionSelectedListener;
import org.eclipse.rap.rwt.application.AbstractEntryPoint;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;


public class AutoSuggestSnippet extends AbstractEntryPoint {

  @Override
  protected void createContents( Composite parent ) {
    final Text text = new Text( parent, SWT.BORDER );
    AutoSuggest autoSuggest = new AutoSuggest( text );
    autoSuggest.setAutoComplete( true );
    DataSource dataSource = new DataSource();
    dataSource.setDataProvider( new ArrayDataProvider( "foo", "food", "foobar", "bar" ) );
    autoSuggest.setDataSource( dataSource );
    autoSuggest.addSelectionListener( new SuggestionSelectedListener() {
      public void suggestionSelected() {
        System.out.println( "selected: " + text.getText() );
      }
    } );
  }

}
