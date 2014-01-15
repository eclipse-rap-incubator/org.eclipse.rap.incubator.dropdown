/*******************************************************************************
 * Copyright (c) 2013, 2014 EclipseSource and others.
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
 * An implementation of this interface can be attached to a <code>DataSource</code> instance
 * to display multiple texts per suggestion.
 *
 *
 * <p>
 *   The strings to be displayed in the list are provided using
 *   {@link ColumnDataProvider#getTexts(Object) getTexts}.
 *   The string to be inserted is provided by the
 *   {@link DataProvider#getValue(Object) getValue} method.
 * </p>
 *
 * <p>
 *   It is recommended to use this interface in conjunction with a {@link ColumnTemplate}, except
 *   when <code>getTexts</code> always returns only one String.
 * </p>
 *
 * @param <S> the type that represents a single suggestion
 * @see DataSource#setDataProvider(DataProvider)
 * @see DataProvider

 */
public interface ColumnDataProvider<S> extends DataProvider<S> {

  /**
   * This method returns the texts to be displayed for a given suggestion in the drop-down list
   * of <code>AutoSuggest</code>.
   *
   * <p>
   *   When used with {@link ColumnTemplate}, the nth string
   *   will be displayed in the nth column. If no string is given for the nth column it will be
   *   empty. Without <code>ColumnTempalte</code> all strings will be display together,
   *   separated by spaces.
   * </p>
   *
   * @param the suggestion, may not be null
   * @return a texts to be displayed, may be of any length but not null
   * @see DataSource#setTemplate(ColumnTemplate)
   */
  String[] getTexts( S suggestion );

}
