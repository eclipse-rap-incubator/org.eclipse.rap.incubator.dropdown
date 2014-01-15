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
 * An implementation of this interface can be attached to a DataSource to provide multiple strings
 * per suggestion. The first string of each suggestion will be the one to be inserted as text.
 *
 * <p>
 *   It is recommended to use this interface in junction with a {@link ColumnTemplate}.
 * </p>
 */
public interface ColumnDataProvider extends DataProvider {

  String[] getTexts( Object element );

}
