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

import org.eclipse.rap.addons.dropdown.viewer.DropDownViewer;
import org.eclipse.rap.clientscripting.ClientListener;
import org.eclipse.rap.testrunner.jasmine.JasmineRunner;
import org.eclipse.rap.testrunner.jasmine.JasmineSysoutReporter;


public class JasmineTests {

  public static void main( String[] args ) {
    JasmineRunner jasmine = new JasmineRunner();
    ClassLoader viewerClassLoader = DropDownViewer.class.getClassLoader();
    ClassLoader scriptingClassLoader = ClientListener.class.getClassLoader();
    ClassLoader localClassLoader = JasmineTests.class.getClassLoader();
    jasmine.parseScript( localClassLoader, "jasmine/fixture/rap-mock.js" );
    jasmine.parseScript( localClassLoader, "jasmine/fixture/rwt-mock.js" );
    jasmine.parseScript( viewerClassLoader, "rwt/remote/Model.js" );
    jasmine.parseScript( localClassLoader, "jasmine/specs/ModelSpec.js" );
    jasmine.parseScript( localClassLoader, "jasmine/specs/ModelListenerSpec.js" );
    jasmine.parseScript( scriptingClassLoader, "org/eclipse/rap/clientscripting/SWT.js" );
    jasmine.parseScript( scriptingClassLoader, "org/eclipse/rap/clientscripting/Function.js" );
    jasmine.addResource(
      "ModelListener",
      viewerClassLoader,
      "org/eclipse/rap/addons/dropdown/viewer/internal/resources/RefreshListener.js"
    );
    jasmine.setReporter( new JasmineSysoutReporter() );
    jasmine.execute();
  }

}
