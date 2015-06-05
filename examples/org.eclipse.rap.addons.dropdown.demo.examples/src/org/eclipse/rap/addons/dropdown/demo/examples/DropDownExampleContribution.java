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
package org.eclipse.rap.addons.dropdown.demo.examples;

import org.eclipse.rap.examples.IExampleContribution;
import org.eclipse.rap.examples.IExamplePage;


public class DropDownExampleContribution implements IExampleContribution {

  @Override
  public String getId() {
    return "drop-down";
  }

  @Override
  public String getTitle() {
    return "Auto Suggest";
  }

  @Override
  public IExamplePage createPage() {
    return new DropDownExamplePage();
  }

}
