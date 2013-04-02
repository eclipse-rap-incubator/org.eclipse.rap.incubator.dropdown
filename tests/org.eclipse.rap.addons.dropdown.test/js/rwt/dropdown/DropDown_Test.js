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

var TestUtil = org.eclipse.rwt.test.fixture.TestUtil;

var shell;
var widget;
var dropdown;
var popup;
var viewer;

rwt.qx.Class.define( "rwt.dropdown.DropDown_Test", {

  extend : rwt.qx.Object,

  members : {

    setUp : function() {
      shell = TestUtil.createShellByProtocol( "w2" );
      shell.open();
      shell.setBorder( null );
      shell.setLocation( 10, 20 );
      this.createExample();
      TestUtil.flush();
    },

    tearDown : function() {
      shell.destroy();
      dropdown.destroy();
    },

    testConstructor_AddsPopupToDocument : function() {
      assertIdentical( rwt.widgets.base.ClientDocument.getInstance(), popup.getParent() );
    },

    testConstructor_SetsDefaultPopUpStyling : function() {
      assertEquals( "solid", popup.getBorder().getStyle() );
      assertEquals( "#000000", popup.getBorder().getColor() );
      assertEquals( [ 1, 1, 1, 1] , popup.getBorder().getWidths() );
      assertEquals( "#ffffff", popup.getBackgroundColor() );
    },

    testConstructor_DoesNotMakePopUpVisible : function() {
      TestUtil.flush();
      assertFalse( popup.isSeeable() );
    },

    testConstructor_CreatesViewerInPopup : function() {
      assertTrue( viewer instanceof rwt.widgets.base.BasicList );
      assertIdentical( popup, viewer.getParent() );
    },

    testConstructor_PositionsViewer : function() {
      assertEquals( 0, viewer.getLeft() );
      assertEquals( 0, viewer.getTop() );
    },

    testSetData_SetDataWithTwoParameters : function() {
      dropdown.setData( "foo", "bar" );

      assertEquals( "bar", dropdown.getData( "foo" ) );
    },

    testSetData_SetDataWithMap : function() {
      dropdown.setData( "x", "y" );
      dropdown.setData( { "foo" : "bar" } );

      assertEquals( "y", dropdown.getData( "x" ) );
      assertEquals( "bar", dropdown.getData( "foo" ) );
    },

    testShow_MakesPopUpVisible : function() {
      prepare();

      assertTrue( popup.isSeeable() );
    },

    testShow_CalledBeforeCreatedMakesPopUpVisible : function() {
      this.createExample();

      prepare();

      assertTrue( popup.isSeeable() );
    },

    testHide_MakesPopUpInvisible : function() {
      prepare();

      dropdown.hide();
      TestUtil.flush();

      assertFalse( popup.isSeeable() );
    },

    testShow_PositionsPopUp : function() {
      prepare();

      assertEquals( 20, popup.getLeft() );
      assertEquals( 70, popup.getTop() );
    },

    testShow_SetsPopUpWidth : function() {
      prepare();

      assertEquals( 100, popup.getWidth() );
    },

    testShow_SetVisibleItemCount : function() {
      dropdown.setVisibleItemCount( 7 );
      prepare();

      assertEquals( 7 * 20, popup.getInnerHeight() );
    },

    testShow_LayoutsViewer : function() {
      prepare();

      assertEquals( popup.getInnerWidth(), viewer.getWidth() );
      assertEquals( popup.getInnerHeight(), viewer.getHeight() );
    },

    testSetItems_SetsItemsOnViewer : function() {
      prepare();

      dropdown.setItems( [ "a", "b", "c" ] );

      assertEquals( [ "a", "b", "c" ], getViewerItems() );
    },

    testAddSelectionListener : function() {
      dropdown.setItems( [ "a", "b", "c" ] );
      prepare();
      var logger = TestUtil.getLogger();

      dropdown.addListener( "Selection", logger.log );
      selectItem( 1 );

      assertEquals( 1, logger.getLog().length );
    },

    testRemoveSelectionListener : function() {
      dropdown.setItems( [ "a", "b", "c" ] );
      prepare();
      var logger = TestUtil.getLogger();

      dropdown.addListener( "Selection", logger.log );
      dropdown.removeListener( "Selection", logger.log );
      TestUtil.click( viewer.getItems()[ 1 ] );

      assertEquals( 0, logger.getLog().length );
    },

    testSelectionEventFields : function() {
      dropdown.setItems( [ "a", "b", "c" ] );
      prepare();
      var logger = TestUtil.getLogger();

      dropdown.addListener( "Selection", logger.log );
      selectItem( 1 );

      var event = logger.getLog()[ 0 ];
      assertIdentical( dropdown, event.widget );
      assertIdentical( "b", event.element );
    },

    testDestroy_DisposesDropDown : function() {
      dropdown.destroy();

      assertTrue( dropdown.isDisposed() );
    },

    testDestroy_DisposesPopup : function() {
      dropdown.destroy();
      TestUtil.flush();

      assertTrue( popup.isDisposed() );
    },

    testDestroy_ClearsReferences : function() {
      var privateObj = dropdown._;
      dropdown.destroy();

      assertTrue( TestUtil.hasNoObjects( dropdown, true ) );
      assertTrue( TestUtil.hasNoObjects( privateObj ) );
    },

    createExample : function() {
      widget = new rwt.widgets.Composite();
      widget.setParent( shell );
      widget.setLocation( 10, 20 );
      widget.setDimension( 100, 30 );
      dropdown = new rwt.dropdown.DropDown( widget );
      popup = dropdown._.popup;
      viewer = dropdown._.viewer;
    }

  }

} );

var prepare = function() {
  dropdown.show();
  TestUtil.flush();
  TestUtil.flush();
};

var getViewerItems = function() {
  var result = [];
  var items = viewer.getItems();
  for( var i = 0; i < items.length; i++ ) {
    result[ i ] = items[ i ].getLabel();
  }
  return result;
};

var selectItem = function( index ) {
  TestUtil.click( viewer.getItems()[ 1 ] );
};



}());
