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

 (function(){
  /*jshint evil:true */
  function getHome() {
    var result = null;
    var myName = "jasmine-runner.js";
    var scripts = document.getElementsByTagName( "script" );
    for( var i = 0; i < scripts.length; i++ ) {
      var src = scripts[ i ].getAttribute( "src" );
      var nameStart = src.indexOf( myName );
      if( nameStart !== -1 ) {
        result = scripts[ i ].getAttribute( "src" ).substr( 0, nameStart );
      }
    }
    if( result === null ) {
      throw new Error( myName + " script tag not found" );
    }
    return result;
  }
  function loadScript( src ) {
    document.write( "<script type=\"text/javascript\" src=\"" + src + "\"></script>" );
  }
  function loadCSS( src ) {
    document.write( "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + src + "\">" );
  }
  var home = getHome();
  loadCSS( home + "jasmine.css" );
  loadScript( home + "jasmine.js" );
  loadScript( home + "jasmine-html.js" );
  loadScript( home + "TestUtil.js" );
  window.onload = function() {
    var jasmineEnv = jasmine.getEnv();
    jasmineEnv.updateInterval = 1000;
    var htmlReporter = new jasmine.HtmlReporter();
    jasmineEnv.addReporter( htmlReporter );
    jasmineEnv.specFilter = function( spec ) {
      return htmlReporter.specFilter(spec );
    };
    jasmineEnv.execute();
  };
}());
