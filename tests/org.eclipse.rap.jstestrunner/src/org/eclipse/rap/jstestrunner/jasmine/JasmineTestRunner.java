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

import static org.junit.Assert.fail;

import org.mozilla.javascript.ScriptableObject;


public class JasmineTestRunner {

  private final JasmineRunner jasmine;
  private final JasmineJUnitReporter reporter;

  public JasmineTestRunner() {
    jasmine = new JasmineRunner();
    reporter = new JasmineJUnitReporter();
    jasmine.setReporter( reporter );
  }

  public void parseScript( ClassLoader loader, String path )  {
    jasmine.parseScript( loader, path );
  }

  public void addResource( String name, ClassLoader loader, String path ) {
    jasmine.addResource( name, loader, path );
  }

  public ScriptableObject getScope() {
    return jasmine.getScope();
  }

  public void execute() {
    jasmine.execute();
    if( !reporter.hasPassed() ) {
      fail( reporter.getLog() );
    }
  }

}
