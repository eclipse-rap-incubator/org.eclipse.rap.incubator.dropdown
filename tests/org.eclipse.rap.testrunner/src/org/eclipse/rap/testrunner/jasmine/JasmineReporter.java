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
package org.eclipse.rap.testrunner.jasmine;

public interface JasmineReporter {

  public void reportRunnerStarting();

  public void reportRunnerResults( int passedSpecs, int executedSpecs  );

  public void reportSpecStarting( String suiteDescription, String specDescription );

  public void reportSpecResults( boolean passed, String errorMessage );

  public void reportSuiteResults( String suiteDescription );

  public void log( String str );

}
