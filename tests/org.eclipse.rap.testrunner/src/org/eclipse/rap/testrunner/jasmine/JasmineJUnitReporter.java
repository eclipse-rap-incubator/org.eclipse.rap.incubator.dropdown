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


public final class JasmineJUnitReporter implements JasmineReporter {

  private final StringBuilder log = new StringBuilder();
  private int passedSpecs;
  private int executedSpecs;

  public void reportRunnerStarting() {
  }

  public void reportRunnerResults( int passedSpecs, int executedSpecs ) {
    this.passedSpecs = passedSpecs;
    this.executedSpecs = executedSpecs;
  }

  public void reportSuiteResults( String suiteDescription ) {
  }

  public void reportSpecStarting( String suiteDescription, String specDescription ) {
    log.append( suiteDescription + " : " + specDescription + " ... " );
  }

  public void reportSpecResults( boolean passed, String error ) {
    log.append( passed ? "passed\n" : error );
  }

  public void log( String message ) {
    log.append( message );
    log.append( '\n' );
  }

  public boolean hasPassed() {
    return executedSpecs > 0 && passedSpecs == executedSpecs;
  }

  public String getLog() {
    if( executedSpecs == 0 ) {
      return "No specs executed\n";
    }
    return passedSpecs + " of " + executedSpecs + " specs passed\n" + log.toString();
  }

}
