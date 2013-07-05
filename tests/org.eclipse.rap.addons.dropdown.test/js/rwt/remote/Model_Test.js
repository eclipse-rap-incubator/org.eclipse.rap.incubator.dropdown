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
  "rwt.remote.Model",
  {}
];

var TestUtil = org.eclipse.rwt.test.fixture.TestUtil;
var MessageProcessor = rwt.remote.MessageProcessor;

var model = null;

rwt.qx.Class.define( "rwt.remote.Model_Test", {

  extend : rwt.qx.Object,

  members : {

    setUp : function() {
      MessageProcessor.processOperationArray( CREATE_OPERATION );
      model = rwt.remote.ObjectRegistry.getObject( "r11" );
    },

    tearDown : function() {
      model = null;
    },

    testCreateByProtocol : function() {
      assertNotNull( model );
    },

    testSetByProtocol : function() {
      TestUtil.protocolSet( "r11", { "foo" : "bar" } );

      assertEquals( "bar", model.get( "foo" ) );
    },

    testNotify : function() {
      TestUtil.protocolListen( "r11", { "Selection" : true } );

      model.notify( "Selection", { "foo" : "bar" } );

      var message = TestUtil.getMessageObject();
      assertEquals( "bar", message.findNotifyProperty( "r11", "Selection", "foo" ) );
    },

    testNotify_CallWithParameterMap : function() {
      TestUtil.protocolListen( "r11", { "Selection" : true } );

      model.notify( { "event" : "Selection", "properties" : { "foo" : "bar" } } );

      var message = TestUtil.getMessageObject();
      assertEquals( "bar", message.findNotifyProperty( "r11", "Selection", "foo" ) );
    },

    testNotify_DoNotSendWithNoSync : function() {
      TestUtil.protocolListen( "r11", { "Selection" : true } );

      model.notify( { "event" : "Selection", "properties" : { "foo" : "bar" }, "nosync" : true } );

      assertEquals( 0, TestUtil.getRequestsSend() );
    },

    testDestroy : function() {
      TestUtil.protocolSet( "r11", { "foo" : {} } );
      var internals = model._;

      model.destroy();

      assertTrue( TestUtil.hasNoObjects( internals ) );
    }

  }

} );



}());
