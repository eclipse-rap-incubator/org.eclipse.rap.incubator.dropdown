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
package org.eclipse.rap.jstestrunner.jasmine;


public interface JasmineReporter {

  public void reportRunnerStarting();

  public void reportRunnerResults();

  public void reportSpecStarting( Spec spec );

  public void reportSpecResults( Spec spec );

  public void reportSuiteResults( Suite suite );

  public void log( String str );

  public interface Spec {

    public String getDescription();

    public Suite getSuite();

    public boolean hasPassed();

    public String getError();

  }

  public interface Suite {

    public String getDescription();

    public Suite getParent();

  }

}
