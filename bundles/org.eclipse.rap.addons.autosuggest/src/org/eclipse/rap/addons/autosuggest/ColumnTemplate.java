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
 * Instances of this class can be used to configure a {@link DataSource} to format the suggestions
 * provided by a {@link ColumnDataProvider} as a table.
 *
 *  <p>
 *    If used, the first string of a suggestion will not be displayed in the list, while the
 *    remaining strings will be placed in their natural order into each column defined by this
 *    template.
 *  </p>
 */
public class ColumnTemplate {

  private final int[] widths;

  /**
   * Constructs a new instance of this class given any number of integers.
   *
   * @param widths the width of all columns in pixels
   */
  public ColumnTemplate( int... widths ) {
    this.widths = widths;
  }

  int[] getColumnWidths() {
    return widths;
  }

}
