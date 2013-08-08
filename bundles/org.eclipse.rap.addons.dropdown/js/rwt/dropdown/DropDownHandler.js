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
  'use strict';

  rap.registerTypeHandler( "rwt.dropdown.DropDown", {

    factory : function( properties ) {
      var control = rwt.remote.ObjectRegistry.getObject( properties.parent );
      return new rwt.dropdown.DropDown( control, properties.markupEnabled );
    },

    properties : [ "items", "visible", "visibleItemCount", "columns" ],

    events : [ "Selection", "DefaultSelection" ],

    methods : [ "setData", "addListener", "removeListener" ],

    methodHandler: {
      "addListener": function( widget, properties ) {
        rwt.remote.HandlerUtil.callWithTarget( properties.listenerId, function( targetFunction ) {
          widget.addListener( properties.eventType, targetFunction );
        } );
      },
      "removeListener": function( widget, properties ) {
        rwt.remote.HandlerUtil.callWithTarget( properties.listenerId, function( targetFunction ) {
          widget.removeListener( properties.eventType, targetFunction );
        } );
      }
    },

    destructor : "destroy"

  } );

}());
