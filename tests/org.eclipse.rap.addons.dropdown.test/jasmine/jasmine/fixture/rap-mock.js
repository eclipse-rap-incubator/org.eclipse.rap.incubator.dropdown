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

RapMock = function() {

  this.fakeRemoteObject = {
    set : function(){},
    notify : function(){},
    call : function(){}
  };


};

RapMock.prototype = {

  typeHandler : {},

  on: function() {},

  off: function() {},

  registerTypeHandler : function( type, handler ) {
  this.typeHandler[ type ] = handler;
  },

  getObject : function() {
    return this.fakeComposite;
  },

  getRemoteObject : function() {
    return this.fakeRemoteObject;
  }

};

rap = new RapMock();
