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

import org.eclipse.rap.jstestrunner.jasmine.JasmineReporter.Spec;
import org.eclipse.rap.jstestrunner.jasmine.JasmineReporter.Suite;


class SpecImpl implements Spec {

  private final Suite parent;
  private final String description;
  private final boolean passed;
  private final String error;

  SpecImpl( Suite parent, String description, boolean passed, String error ) {
    if( parent == null ) {
      throw new NullPointerException( "Parameter is null: parent" );
    }
    if( description == null ) {
      throw new NullPointerException( "Parameter is null: description" );
    }
    this.parent = parent;
    this.description = description;
    this.passed = passed;
    this.error = error;
  }

  @Override
  public Suite getSuite() {
    return parent;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public boolean hasPassed() {
    return passed;
  }

  @Override
  public String getError() {
    return error;
  }

  @Override
  public int hashCode() {
    int result = 1;
    result = 31 * result + parent.hashCode();
    result = 31 * result + description.hashCode();
    return result;
  }

  @Override
  public boolean equals( Object obj ) {
    if( this == obj ) {
      return true;
    }
    if( obj == null ) {
      return false;
    }
    if( getClass() != obj.getClass() ) {
      return false;
    }
    SpecImpl other = ( SpecImpl )obj;
    return parent.equals( other.parent ) && description.equals( other.description );
  }

}
