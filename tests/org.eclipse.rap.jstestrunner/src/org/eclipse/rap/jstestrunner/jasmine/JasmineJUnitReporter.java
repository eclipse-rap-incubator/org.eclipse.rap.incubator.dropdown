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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public final class JasmineJUnitReporter implements JasmineReporter {

  private final StringBuilder log = new StringBuilder();
  private int passedSpecs;
  private int executedSpecs;
  private Suite currentSuite;
  private String indent = "";

  @Override
  public void reportRunnerStarting() {
  }

  @Override
  public void reportRunnerResults() {
  }

  @Override
  public void reportSuiteResults( Suite suite ) {
  }

  @Override
  public void reportSpecStarting( Spec spec ) {
    if( !spec.getSuite().equals( currentSuite ) ) {
      indent = "";
      Suite suite = spec.getSuite();
      String[] oldPath = getSuitePath( currentSuite );
      String[] path = getSuitePath( suite );
      for( int i = 0; i < path.length; i++ ) {
        if( i >= oldPath.length || !path[ i ].equals( oldPath[ i ] ) ) {
          log.append( indent );
          log.append( path[ i ] );
          log.append( '\n' );
        }
        indent += "   ";
      }
      currentSuite = suite;
    }
  }

  @Override
  public void reportSpecResults( Spec spec ) {
    executedSpecs++;
    log.append( indent );
    log.append( spec.hasPassed() ? "[OK] " : "[FAILED] " );
    log.append( spec.getDescription() );
    log.append( '\n' );
    if( spec.hasPassed() ) {
      passedSpecs++;
    } else {
      log.append( indent + "          " + spec.getError() );
    }
    if( !log.toString().endsWith( "\n" ) ) {
      log.append( '\n' );
    }
  }

  @Override
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
    return passedSpecs + " of " + executedSpecs + " specs passed.\n" + log.toString();
  }

  private static String[] getSuitePath( Suite suite ) {
    List<String> path = new ArrayList<>();
    Suite current = suite;
    while( current != null ) {
      path.add( current.getDescription() );
      current = current.getParent();
    }
    Collections.reverse( path );
    return path.toArray( new String[ path.size() ] );
  }

}
