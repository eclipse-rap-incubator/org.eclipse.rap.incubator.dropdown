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


public final class JasmineSysoutReporter implements JasmineReporter {

  public void reportRunnerStarting() {
    System.out.println( "Jasmine Started" );
  }

  public void reportRunnerResults( int passedSpecs, int executedSpecs ) {
    System.out.println( "Jasmine Finished" );
    String msg = passedSpecs + " of " + executedSpecs + " Specs passed";
    if( passedSpecs == executedSpecs ) {
      System.out.println( msg );
    } else {
      System.err.println( msg );
    }
  }

  public void reportSuiteResults( String suiteDescription ) {
  }

  public void reportSpecStarting( String suiteDescription, String specDescription ) {
    System.out.print( suiteDescription + " : " + specDescription + " ... ");
  }

  public void reportSpecResults( boolean passed, String error ) {
    if( passed ) {
      System.out.println( "Passed." );
    } else {
      System.err.println( "Failed:" );
      System.err.println( error );
    }
  }

  public void log( String str ) {
    System.out.println( "Jasmine log: " + str );
  }
}
