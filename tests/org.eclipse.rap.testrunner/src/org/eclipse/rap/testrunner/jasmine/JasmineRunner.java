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

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import org.mozilla.javascript.*;


public class JasmineRunner {

  private static final String CHARSET = "UTF-8";
  private ScriptableObject scope;
  private ScriptableObject jasmineEnv;
  private JasmineReporter publicReporter;
  private JsConsole console;
  private TestUtil testUtil;

  public JasmineRunner() {
    scope = Context.enter().initStandardObjects();
    createConsole();
    createTestUtil();
    createStubs( "setTimeout", "clearTimeout", "setInterval", "clearInterval" );
    parseScript( getClass().getClassLoader(), "org/eclipse/rap/testrunner/jasmine/jasmine.js" );
    jasmineEnv = getJasmineEnv();
    createReporter();
    Context.exit();
  }

  private void createConsole() {
    console = new JsConsole();
    scope.put( "console", scope, console );
  }

  private void createTestUtil() {
    testUtil = new TestUtil();
    scope.put( "TestUtil", scope, testUtil );
  }

  public void setReporter( JasmineReporter publicReporter ) {
    this.publicReporter = publicReporter;
  }

  public void parseScript( ClassLoader loader, String path )  {
    String script = readContent( loader, path );
    Context.enter();
    Context c = Context.getCurrentContext();
    try {
      c.evaluateString( scope, script, path, 1, null );
    } catch( EcmaError ex ) {
      System.out.println( ex.getErrorMessage() );
      System.out.println( ex.getScriptStackTrace() );
      throw new IllegalStateException( "Could not execute " + path );
    }
    Context.exit();
  }

  public void addResource( String name, ClassLoader loader, String path ) {
    testUtil.loadResourceFromClassLoader( name, loader, path );
  }

  public void execute() {
    Context.enter();
    ScriptableObject.putProperty( jasmineEnv, "updateInterval", Integer.valueOf( 0 ) );
    ScriptableObject.callMethod( jasmineEnv, "execute", null );
    Context.exit();
  }

  public void enterConsole() {
    console.enter();
  }

  ////////////
  // Internals

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
    } catch( IOException e ) {
      throw new IllegalArgumentException( "Failed to read resource: " + resource );
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

    private int passedSpecs = 0;
    private int executedSpecs = 0;

    public void reportRunnerStarting( ScriptableObject runner ) {
      publicReporter.reportRunnerStarting();
    }

    public void reportRunnerResults( ScriptableObject runner ) {
      publicReporter.reportRunnerResults( passedSpecs, executedSpecs );
    }

    public void reportSpecStarting( ScriptableObject spec ) {
      executedSpecs++;
      ScriptableObject suite = ( ScriptableObject )spec.get( "suite" );
      String suiteDescription = ( String )suite.get( "description" );
      String specDescription = ( String )spec.get( "description" );
      publicReporter.reportSpecStarting( suiteDescription, specDescription );
    }

    public void reportSpecResults( ScriptableObject spec ) {
      ScriptableObject results = ( ScriptableObject )ScriptableObject.callMethod( spec, "results", null );
      boolean passed = ( ( Boolean )ScriptableObject.callMethod( results, "passed", null ) ).booleanValue();
      if( passed ) {
        passedSpecs++;
       }
      publicReporter.reportSpecResults( passed );
    }

    public void reportSuiteResults( ScriptableObject suite ) {
      publicReporter.reportSuiteResults( ( String )suite.get( "description" ) );
    }

    public void log( String str ) {
      publicReporter.log( str );
    }

  }

  public class JsConsole {

    private boolean readFromInput = false;

    public void log( Object... args ) {
      System.out.print( "console.log: " );
      for( int i = 0; i < args.length; i++ ) {
        System.out.print( args[ i ] );
        if( i != args.length -1 ) {
          System.out.print( ", " );
        }
      }
      System.out.println();
    }

    public void enter() {
      if( readFromInput ) {
        throw new IllegalStateException( "Console already active" );
      }
      readFromInput = true;
      Context context = Context.enter();
      BufferedReader br = new BufferedReader( new InputStreamReader( System.in ) );
      while( readFromInput ) {
        System.out.print( "JS > " );
        try {
          Object result = context.evaluateString( scope, br.readLine(), "console", 1, null );
          if( !( result instanceof org.mozilla.javascript.Undefined ) ) {
            System.out.println( result );
          }
        } catch( EcmaError e ) {
          System.out.println( e.details() );
        } catch( IOException e ) {
          System.out.println( "IO Error" );
          e.printStackTrace();
          Context.exit();
          System.exit( 0 );
        }
      }
    }

    public void exit() {
      if( !readFromInput ) {
        throw new IllegalStateException( "Console not active" );
      }
      readFromInput = false;
    }

  }

  public class TestUtil {

    Map<String, Object> resources = new HashMap< String, Object>();

    public void loadResourceFromURL( String name, String url ) {
      throw new UnsupportedOperationException( "Not supported in Rhino environment" );
    }

    public void loadResourceFromClassLoader( String name, ClassLoader loader, String path ) {
      resources.put( name, readContent( loader, path ) );
    }

    public Object getResource( String name ) {
      return resources.get( name );
    }

  }

}
