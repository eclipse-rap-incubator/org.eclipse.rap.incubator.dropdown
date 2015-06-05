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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;


public class ArrayDataProvider_Test {

  @Test ( expected = NullPointerException.class )
  public void testConstructor_failsWithNullArgument() {
    new ArrayDataProvider( ( String[] )null );
  }

  @Test
  public void testGetSuggestions_returnsElements() {
    DataProvider<?> dataProvider = new ArrayDataProvider( new String[]{ "foo", "bar" } );

    Iterable<?> suggestions = dataProvider.getSuggestions();

    assertEquals( Arrays.asList( "foo", "bar" ), getElements( suggestions ) );
  }

  @Test( expected = UnsupportedOperationException.class )
  public void testGetSuggestions_returnsUnmodifiableIterator() {
    DataProvider<?> dataProvider = new ArrayDataProvider( new String[]{ "foo", "bar" } );

    Iterable<?> suggestions = dataProvider.getSuggestions();

    Iterator<?> iterator = suggestions.iterator();
    iterator.next();
    iterator.remove();
  }

  private static List<Object> getElements( Iterable<?> suggestions ) {
    List<Object> elements = new ArrayList<>();
    for( Object object : suggestions ) {
      elements.add( object );
    }
    return elements;
  }

}
