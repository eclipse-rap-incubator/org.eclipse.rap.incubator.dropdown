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
package jasmine;

import org.eclipse.rap.jstestrunner.jasmine.JasmineTestRunner;
import org.eclipse.rap.rwt.scripting.ClientListener;
import org.junit.Before;
import org.junit.Test;


public class AutoSuggestJs_Test {

  private static final ClassLoader LOCAL = AutoSuggestJs_Test.class.getClassLoader();
  private static final ClassLoader SCRIPTING = ClientListener.class.getClassLoader();
  private static final String AUTO_SUGGEST_JS
    = "org/eclipse/rap/addons/autosuggest/internal/resources/AutoSuggest.js";

  private JasmineTestRunner jasmine;

  @Before
  public void setUp() {
    jasmine = new JasmineTestRunner();
    jasmine.parseScript( LOCAL, "jasmine/fixture/rap-mock.js" );
    jasmine.parseScript( LOCAL, "jasmine/fixture/rwt-mock.js" );
    jasmine.parseScript( LOCAL, "rwt/remote/Model.js" );
  }

  @Test
  public void testModelSpec() {
    jasmine.parseScript( LOCAL, "jasmine/specs/ModelSpec.js" );
    jasmine.execute();
  }

  @Test
  public void testListenerSpec() {
    jasmine.addResource( "AutoSuggest.js", LOCAL, AUTO_SUGGEST_JS );
    jasmine.parseScript( SCRIPTING, "rwt.js" );
    jasmine.parseScript( SCRIPTING, "SWT.js" );
    jasmine.parseScript( SCRIPTING, "rwt/scripting/FunctionFactory.js" );
    jasmine.parseScript( LOCAL, "jasmine/specs/AutoSuggestSpec.js" );
    jasmine.execute();
  }

}
