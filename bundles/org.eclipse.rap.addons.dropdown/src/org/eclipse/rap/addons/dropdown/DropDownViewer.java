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

package org.eclipse.rap.addons.dropdown;

import org.eclipse.swt.widgets.Text;


public class DropDownViewer {

  private DropDown dropdown;

  public DropDownViewer( Text text ) {
    dropdown = new DropDown( text );
  }

  public DropDown getDropDown() {
    return dropdown;
  }

}
