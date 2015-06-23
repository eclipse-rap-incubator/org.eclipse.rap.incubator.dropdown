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

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.rap.jstestrunner.jasmine.JasmineReporter.Spec;
import org.eclipse.rap.jstestrunner.jasmine.JasmineReporter.Suite;
import org.mozilla.javascript.*;


class JasmineRunner {

  private static final String CHARSET = "UTF-8";
  private final ScriptableObject scope;
  private final ScriptableObject jasmineEnv;
  private final Map<String, Object> resources = new HashMap<>();
  private JasmineReporter publicReporter;

  public JasmineRunner() {
    scope = Context.enter().initStandardObjects();
    initializeScope();
    parseScript( getClass().getClassLoader(), "org/eclipse/rap/jstestrunner/jasmine/jasmine.js" );
    jasmineEnv = getJasmineEnv();
    createReporter();
    Context.exit();
  }

  public void setReporter( JasmineReporter publicReporter ) {
    this.publicReporter = publicReporter;
  }

  public ScriptableObject getScope() {
    return scope;
  }

  public void parseScript( ClassLoader loader, String path )  {
    String script = readContent( loader, path );
    Context.enter();
    Context context = Context.getCurrentContext();
    try {
      context.evaluateString( scope, script, path, 1, null );
    } catch( EcmaError ex ) {
      String message = "Could not execute "
                       + path
                       + "\n"
                       + ex.getErrorMessage()
                       + "\n"
                       + ex.getScriptStackTrace();
      throw new IllegalStateException( message );
    }
    Context.exit();
  }

  public void addResource( String name, ClassLoader loader, String path ) {
    resources.put( name, readContent( loader, path ) );
  }

  public void execute() {
    Context.enter();
    ScriptableObject.putProperty( jasmineEnv, "updateInterval", Integer.valueOf( 0 ) );
    ScriptableObject.callMethod( jasmineEnv, "execute", null );
    Context.exit();
  }

  ////////////
  // Internals

  private void initializeScope() {
    scope.put( "TestUtil", scope, new TestUtil() );
    scope.put( "window", scope, scope );
    createStubs( "setTimeout", "clearTimeout", "setInterval", "clearInterval" );
  }

  private void createReporter() {
    ScriptableObject.callMethod( jasmineEnv, "addReporter", new Object[]{ new InternalReporter() } );
  }

  private ScriptableObject getJasmineEnv() {
    ScriptableObject jasmine = ( ScriptableObject )scope.get( "jasmine" );
    return ( ScriptableObject )ScriptableObject.callMethod( jasmine, "getEnv", null );
  }

  private void createStubs( final String... names ) {
    for( String name : names ) {
      createStub( name );
    }
  }

  private void createStub( final String name ) {
    scope.put( name, scope, new BaseFunction() {
      @Override
      public Object call( Context cx, Scriptable scope, Scriptable thisObj, Object[] args ) {
        throw new UnsupportedOperationException( name + " is not implemented" );
      }
    } );
  }

  private static String readContent( ClassLoader loader, String resource ) {
    try {
      return readTextContentChecked( loader, resource );
    } catch( IOException ioe ) {
      throw new IllegalArgumentException( "Failed to read resource: " + resource, ioe );
    }
  }

  private static String readTextContentChecked( ClassLoader loader, String resource ) throws IOException {
    InputStream stream = loader.getResourceAsStream( resource );
    if( stream == null ) {
      throw new IllegalArgumentException( "Resource not found: " + resource );
    }
    try {
      BufferedReader reader = new BufferedReader( new InputStreamReader( stream, CHARSET ) );
      return readLines( reader );
    } finally {
      stream.close();
    }
  }

  private static String readLines( BufferedReader reader ) throws IOException {
    StringBuilder builder = new StringBuilder();
    String line = reader.readLine();
    while( line != null ) {
      builder.append( line );
      builder.append( '\n' );
      line = reader.readLine();
    }
    return builder.toString();
  }

  public class InternalReporter {

    public void reportRunnerStarting() {
      publicReporter.reportRunnerStarting();
    }

    public void reportRunnerResults() {
      publicReporter.reportRunnerResults();
    }

    public void reportSpecStarting( ScriptableObject specObj ) {
      publicReporter.reportSpecStarting( createSpec( specObj ) );
    }

    public void reportSpecResults( ScriptableObject specObj ) {
      publicReporter.reportSpecResults( createSpec( specObj ) );
    }


    public void reportSuiteResults( ScriptableObject suiteObj ) {
      publicReporter.reportSuiteResults( createSuite( suiteObj ) );
    }

    public void log( String message ) {
      publicReporter.log( message );
    }

    private Spec createSpec( ScriptableObject specObj ) {
      Suite suite = createSuite( ( ScriptableObject )specObj.get( "suite" ) );
      String description = ( String )specObj.get( "description" );
      ScriptableObject results
        = ( ScriptableObject )ScriptableObject.callMethod( specObj, "results", null );
      boolean passed
        = ( ( Boolean )ScriptableObject.callMethod( results, "passed", null ) ).booleanValue();
      String error = getMessageForSpec( results );
      return new SpecImpl( suite, description, passed, error );
    }

    private String getMessageForSpec( ScriptableObject resultsObj ) {
      String result = "";
      ScriptableObject items
        = ( ( ScriptableObject )ScriptableObject.callMethod( resultsObj, "getItems", null ) );
      for( Object id : items.getIds() ) {
        ScriptableObject item = ( ScriptableObject )items.get( id );
        boolean itemPassed
          = ( ( Boolean )ScriptableObject.callMethod( item, "passed", null ) ).booleanValue();
        if( !itemPassed ) {
          result += "Error [" + id + "]: " + ScriptableObject.getProperty( item, "message" ) + "\n";
        }
      }
      return result;
    }

    private Suite createSuite( ScriptableObject suite ) {
      if( suite == null ) {
        return null;
      }
      String description = ( String )suite.get( "description" );
      ScriptableObject parent = ( ScriptableObject )suite.get( "parentSuite" );
      return new SuiteImpl( createSuite( parent ), description );
    }

  }

  public class TestUtil {

    public Object getResource( String name ) {
      return resources.get( name );
    }

  }

}
