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


/**
 * Instances of this interface represent a data set used as suggestions.
 * It provides a set of suggestions and their textual representation.
 *
 * @param <S> the type that represents a single suggestion
 * @see DataSource#setDataProvider(DataProvider)
 * @see ColumnDataProvider
 */
public interface DataProvider<S> {

  /**
   * Provides the list of all possible suggestions.
   *
   * @return the list of suggestions, may be empty but not <code>null</code>
   */
  Iterable<S> getSuggestions();

  /**
   * Provides the text that will be inserted when the given suggestion is selected.
   *
   * @return the text to be inserted, never <code>null</code>
   */
  String getValue( S suggestion );

}
