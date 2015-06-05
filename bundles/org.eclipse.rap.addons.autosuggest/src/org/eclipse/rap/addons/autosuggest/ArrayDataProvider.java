/*******************************************************************************
 * Copyright (c) 2013, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.addons.autosuggest;

import java.util.Arrays;
import java.util.List;


/**
 * This class provides a simple implementation of the default {@link DataProvider} interface
 * based on a String array.
 */
public class ArrayDataProvider implements DataProvider<String> {

  private final List<String> elements;


  /**
   * Constructs a new instance of this class given an array of strings.
   *
   * @param elements an array of suggestions texts for a {@DataSource}
   **/
  public ArrayDataProvider( String... elements ) {
    this.elements = Arrays.asList( elements );
  }

  @Override
  public Iterable<String> getSuggestions() {
    return elements;
  }

  @Override
  public String getValue( String suggestion ) {
    return suggestion;
  }

}
