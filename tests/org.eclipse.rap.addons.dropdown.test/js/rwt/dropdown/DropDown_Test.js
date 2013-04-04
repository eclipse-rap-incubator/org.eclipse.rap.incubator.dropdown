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
var hideTimer;

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

    testConstructor_HideScrollbars : function() {
      assertFalse( viewer.getVerticalBar().getDisplay() );
      assertFalse( viewer.getHorizontalBar().getDisplay() );
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

    testShow_FocusesViewer : function() {
      prepare();

      assertTrue( viewer.getFocused() );
    },

    testShow_LeavesLinkedControlFocused : function() {
      widget.setTabIndex( 1 );
      widget.focus();
      prepare();

      assertFalse( viewer.getFocused() );
      assertTrue( widget.getFocused() );
    },

    testShow_LeavesLinkedControlChildFocused : function() {
      var focusable = new rwt.widgets.Button( "push" );
      focusable.setParent( widget );
      TestUtil.flush();
      focusable.focus();
      prepare();

      assertFalse( viewer.getFocused() );
      assertTrue( focusable.getFocused() );
    },

    testDoNotHideOnParentClick : function() {
      var focusable = new rwt.widgets.Button( "push" );
      focusable.setParent( widget );
      prepare();

      TestUtil.click( focusable );
      forceTimer();

      assertTrue( popup.isSeeable() );
    },

    testDoNotHideOnFocusableParentClick : function() {
      widget.setTabIndex( 1 );
      widget.contains = rwt.util.Functions.returnFalse;
      prepare();

      TestUtil.click( widget );
      forceTimer();

      assertTrue( popup.isSeeable() );
    },

    testDoNotHideOnChildClick : function() {
      prepare();

      TestUtil.click( viewer );
      forceTimer();

      assertTrue( popup.isSeeable() );
    },

    testHideOnShellClick : function() {
      prepare();

      TestUtil.click( shell );
      forceTimer();

      assertFalse( popup.isSeeable() );
    },

    testShellClickAfterDisposeDoesNotCrash : function() {
      prepare();
      dropdown.destroy();
      TestUtil.flush();

      TestUtil.click( shell );
      forceTimer();

      assertFalse( popup.isSeeable() );
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

    testSetVisibleItemCount_UpdatesScrollbar : function() {
      dropdown.setItems( [ "a", "b", "c" ] );

      dropdown.setVisibleItemCount( 2 );

      assertTrue( viewer.getVerticalBar().getDisplay() );
    },

    testSetItems_UpdatesScrollbar : function() {
      dropdown.setVisibleItemCount( 2 );

      dropdown.setItems( [ "a", "b", "c" ] );

      assertTrue( viewer.getVerticalBar().getDisplay() );
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

    testGetItemCount : function() {
      prepare();

      dropdown.setItems( [ "a", "b", "c" ] );

      assertEquals( 3, dropdown.getItemCount() );
    },

    testGetVisibility_returnsFalse : function() {
      assertFalse( dropdown.getVisibility() );
    },

    testGetVisibility_returnsTrue : function() {
      prepare();
      assertTrue( dropdown.getVisibility() );
    },

    testAddSelectionListener : function() {
      dropdown.setItems( [ "a", "b", "c" ] );
      prepare();
      var logger = TestUtil.getLogger();

      dropdown.addListener( "Selection", logger.log );
      clickItem( 1 );

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
      clickItem( 1 );

      var event = logger.getLog()[ 0 ];
      assertIdentical( dropdown, event.widget );
      assertIdentical( "b", event.element );
      assertIdentical( 13, event.type );
    },

    testSelectionEventFields_NoItemSelected : function() {
      dropdown.setItems( [ "a", "b", "c" ] );
      prepare();
      dropdown.setSelectionIndex( 1 );
      var logger = TestUtil.getLogger();

      dropdown.addListener( "Selection", logger.log );
      dropdown.setSelectionIndex( -1 );

      var event = logger.getLog()[ 0 ];
      assertIdentical( dropdown, event.widget );
      assertIdentical( null, event.element );
    },

    testAddDefaultSelectionListener_FiresOnEnter : function() {
      dropdown.setItems( [ "a", "b", "c" ] );
      prepare();
      var logger = TestUtil.getLogger();

      dropdown.addListener( "DefaultSelection", logger.log );
      clickItem( 1 );
      TestUtil.pressOnce( viewer, "Enter" );

      assertEquals( 1, logger.getLog().length );
    },

    testAddDefaultSelectionListener_FiresOnDoubleClick : function() {
      dropdown.setItems( [ "a", "b", "c" ] );
      prepare();
      var logger = TestUtil.getLogger();

      dropdown.addListener( "DefaultSelection", logger.log );
      doubleClickItem( 1 );

      assertEquals( 1, logger.getLog().length );
    },

    testDefaultSelectionEventFields : function() {
      dropdown.setItems( [ "a", "b", "c" ] );
      prepare();
      var logger = TestUtil.getLogger();

      dropdown.addListener( "DefaultSelection", logger.log );
      clickItem( 1 );
      TestUtil.pressOnce( viewer, "Enter" );

      var event = logger.getLog()[ 0 ];
      assertIdentical( dropdown, event.widget );
      assertIdentical( "b", event.element );
      assertIdentical( 14, event.type );
    },

    testDefaultSelectionEventFields_NoItemSelected : function() {
      dropdown.setItems( [ "a", "b", "c" ] );
      prepare();
      var logger = TestUtil.getLogger();

      dropdown.addListener( "DefaultSelection", logger.log );
      dropdown.setSelectionIndex( -1 );
      TestUtil.pressOnce( viewer, "Enter" );

      var event = logger.getLog()[ 0 ];
      assertIdentical( dropdown, event.widget );
      assertIdentical( null, event.element );
    },

    testGetSelectionIndex_InitialValueIsMinusOne : function() {
      dropdown.setItems( [ "a", "b", "c" ] );

      assertEquals( -1, dropdown.getSelectionIndex() );
    },

    testGetSelectionIndex_ValueIsMinusOneForNoItems : function() {
      assertEquals( -1, dropdown.getSelectionIndex() );
    },

    testGetSelectionIndex_ValueAfterSelection : function() {
      dropdown.setItems( [ "a", "b", "c" ] );
      prepare();

      clickItem( 1 );

      assertEquals( 1, dropdown.getSelectionIndex() );
    },

    testSetSelectionIndex : function() {
      dropdown.setItems( [ "a", "b", "c" ] );

      dropdown.setSelectionIndex( 1 );

      assertEquals( 1, dropdown.getSelectionIndex() );
    },

    testSetSelectionIndex_ValueIsMinusOne : function() {
      dropdown.setItems( [ "a", "b", "c" ] );

      dropdown.setSelectionIndex( -1 );

      assertEquals( -1, dropdown.getSelectionIndex() );
    },

    testSetSelectionIndex_ValueOutOfBoundsThrowsException : function() {
      dropdown.setItems( [ "a", "b", "c" ] );

      try {
        dropdown.setSelectionIndex( 4 );
        fail();
      } catch( ex ) {
        // expected
      }
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

    testDestroy_DeregistersAppearListener : function() {
      widget.setVisibility( false );

      dropdown.destroy();
      widget.setVisibility( true );
      // Succeeds by not crashing
    },

    testDestroy_DocumentClick : function() {
      prepare();
      dropdown.destroy();
      TestUtil.click( TestUtil.getDocument() );

      assertFalse( popup.isSeeable() );
    },

    createExample : function() {
      widget = new rwt.widgets.Composite();
      widget.setParent( shell );
      widget.setLocation( 10, 20 );
      widget.setDimension( 100, 30 );
      dropdown = new rwt.dropdown.DropDown( widget );
      popup = dropdown._.popup;
      viewer = dropdown._.viewer;
      hideTimer = dropdown._.hideTimer;
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

var clickItem = function( index ) {
  TestUtil.click( viewer.getItems()[ 1 ] );
};

var doubleClickItem = function( index ) {
  TestUtil.doubleClick( viewer.getItems()[ 1 ] );
};

var forceTimer = function() {
  TestUtil.forceInterval( hideTimer );
};


}());
