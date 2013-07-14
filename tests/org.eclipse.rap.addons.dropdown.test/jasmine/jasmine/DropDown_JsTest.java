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

import org.eclipse.rap.addons.dropdown.DropDown_Test;
import org.eclipse.rap.addons.dropdown.viewer.DropDownViewer;
import org.eclipse.rap.clientscripting.ClientListener;
import org.eclipse.rap.jstestrunner.jasmine.*;
import org.junit.*;


public class DropDown_JsTest {

  private static final String MODEL_LISTENER_JS
    = "org/eclipse/rap/addons/dropdown/viewer/internal/resources/ModelListener.js";
  private static final ClassLoader LOCAL_LOADER = DropDown_Test.class.getClassLoader();
  private static final ClassLoader VIEWER_LOADER = DropDownViewer.class.getClassLoader();
  private static final ClassLoader SCRIPTING_LOADER = ClientListener.class.getClassLoader();

  @Rule
  public JasmineTestRunner jasmine = new JasmineTestRunner();

  @Before
  public void setUp() {
    jasmine.parseScript( LOCAL_LOADER, "jasmine/fixture/rap-mock.js" );
    jasmine.parseScript( LOCAL_LOADER, "jasmine/fixture/rwt-mock.js" );
    jasmine.parseScript( VIEWER_LOADER, "rwt/remote/Model.js" );
  }

  @Test
  public void testModelSpec() {
    jasmine.parseScript( LOCAL_LOADER, "jasmine/specs/ModelSpec.js" );
    jasmine.execute();
  }

  @Test
  public void testListenerSpec() {
    jasmine.parseScript( LOCAL_LOADER, "jasmine/specs/ModelListenerSpec.js" );
    jasmine.parseScript( SCRIPTING_LOADER, "org/eclipse/rap/clientscripting/SWT.js" );
    jasmine.parseScript( SCRIPTING_LOADER, "org/eclipse/rap/clientscripting/Function.js" );
    jasmine.addResource( "ModelListener", VIEWER_LOADER, MODEL_LISTENER_JS );
    jasmine.execute();
  }

}
