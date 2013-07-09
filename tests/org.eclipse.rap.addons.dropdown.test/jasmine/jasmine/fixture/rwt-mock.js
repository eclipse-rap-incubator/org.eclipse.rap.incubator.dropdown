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

rwt = {
  util : {
    Arrays : {
      removeAt : function(arr, i) {
        return arr.splice(i, 1)[0];
      }
    }
  },
  qx : {
    Class : {
      createNamespace : function( name, object ) {
        var splits = name.split(".");
        var parent = window;
        var part = splits[0];

        for (var i=0, l=splits.length-1; i<l; i++, part=splits[i])
        {
          if (!parent[part]) {
            parent = parent[part] = {};
          } else {
            parent = parent[part];
          }
        }
        if (parent[part] === undefined) {
          parent[part] = object;
        }
        return part;
      }
    }
  },
  dropdown : {
    DropDown : {
      searchItems : null,
      createQuery : null
    }
  }

};
