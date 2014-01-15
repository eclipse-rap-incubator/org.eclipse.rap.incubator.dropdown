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
 * This is the default interface to be implemented to attach data to a {@link DataSource}
 *
 * @see DataSource#setDataProvider(DataProvider)
 */
public interface DataProvider {

  /**
   * Provides the raw suggestions data
   *
   * @return an iterable object containing all suggestions in any arbitrary format
   */
  Iterable<?> getSuggestions();

  /**
   * Provides a single suggestion text for a raw suggestion
   *
   * @return a string to be suggested as text input
   */
  String getValue( Object element );

}
