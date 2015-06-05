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

import org.eclipse.rap.jstestrunner.jasmine.JasmineReporter.Suite;


class SuiteImpl implements Suite {

  private final Suite parent;
  private final String description;

  SuiteImpl( Suite parent, String description ) {
    if( description == null ) {
      throw new NullPointerException( "Parameter is null: description" );
    }
    this.parent = parent;
    this.description = description;
  }

  @Override
  public Suite getParent() {
    return parent;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public int hashCode() {
    int result = 1;
    result = 31 * result + ( ( parent == null ) ? 0 : parent.hashCode() );
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
    SuiteImpl other = ( SuiteImpl )obj;
    if( parent == null ) {
      if( other.parent != null ) {
        return false;
      }
    } else if( !parent.equals( other.parent ) ) {
      return false;
    }
    if( !description.equals( other.description ) ) {
      return false;
    }
    return true;
  }

}
