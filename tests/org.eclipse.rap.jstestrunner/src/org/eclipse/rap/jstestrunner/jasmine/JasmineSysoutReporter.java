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
package org.eclipse.rap.jstestrunner.jasmine;


public final class JasmineSysoutReporter implements JasmineReporter {

  private int passedSpecs;
  private int executedSpecs;

  @Override
  public void reportRunnerStarting() {
    System.out.println( "Test runner started." );
  }

  @Override
  public void reportRunnerResults() {
    System.out.println( "Test runner finished." );
    System.out.println( passedSpecs + " of " + executedSpecs + " specs passed" );
  }

  @Override
  public void reportSuiteResults( Suite suite ) {
  }

  @Override
  public void reportSpecStarting( Spec spec ) {
    System.out.print( spec.getSuite().getDescription() + " : " + spec.getDescription() + " ... " );
  }

  @Override
  public void reportSpecResults( Spec spec ) {
    executedSpecs++;
    if( spec.hasPassed() ) {
      passedSpecs++;
      System.out.println( "passed." );
    } else {
      System.err.println( "FAILED." );
      System.err.println( spec.getError() );
    }
  }

  @Override
  public void log( String str ) {
    System.out.println( "Jasmine log: " + str );
  }

}
