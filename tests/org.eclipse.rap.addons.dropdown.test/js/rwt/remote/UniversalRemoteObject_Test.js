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

var CREATE_OPERATION = [
  "create",
  "r11",
  "rwt.remote.UniversalRemoteObject",
  {}
];

var TestUtil = org.eclipse.rwt.test.fixture.TestUtil;
var MessageProcessor = rwt.remote.MessageProcessor;

var uro = null;

rwt.qx.Class.define( "rwt.remote.UniversalRemoteObject_Test", {

  extend : rwt.qx.Object,

  members : {

    setUp : function() {
      MessageProcessor.processOperationArray( CREATE_OPERATION );
      uro = rwt.remote.ObjectRegistry.getObject( "r11" );
    },

    tearDown : function() {
      uro = null;
    },

    testCreateByProtocol : function() {
      assertNotNull( uro );
    },

    testSetByProtocol : function() {
      TestUtil.protocolSet( "r11", { "foo" : "bar" } );

      assertEquals( "bar", uro.get( "foo" ) );
    },

    testNotify : function() {
      TestUtil.protocolListen( "r11", { "Selection" : true } );

      uro.notify( "Selection", { "foo" : "bar" } );

      var message = TestUtil.getMessageObject();
      assertEquals( "bar", message.findNotifyProperty( "r11", "Selection", "foo" ) );
    }


  }

} );



}());
