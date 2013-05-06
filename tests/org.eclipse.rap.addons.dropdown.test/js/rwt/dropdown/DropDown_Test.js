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
var DropDown = rwt.dropdown.DropDown;

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

    testShow_LeavesParentFocused : function() {
      widget.setTabIndex( 1 );
      widget.focus();
      prepare();

      assertFalse( viewer.getFocused() );
      assertTrue( widget.getFocused() );
    },

    testShow_LeavesSiblingFocused : function() {
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

    testHideOnEscape : function() {
      prepare();

      TestUtil.press( viewer, "Escape" );

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
      dropdown.destroy();
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

    testHide_SendsVisibility : function() {
      prepare();

      dropdown.hide();
      rwt.remote.Server.getInstance().send();

      var message = TestUtil.getMessageObject();
      assertFalse( message.findSetProperty( "w3", "visibility" ) );
    },

    testHide_DoesNotSendVisibilityInResponse : function() {
      prepare();
      TestUtil.clearRequestLog();

      TestUtil.fakeResponse( true );
      dropdown.hide();
      TestUtil.fakeResponse( false );
      rwt.remote.Server.getInstance().send();

      var message = TestUtil.getMessageObject();
      assertNull( message.findSetOperation( "w3", "visibility" ) );
    },

    testHide_DoesNotSendVisibilityIfAlreadyInvisible : function() {
      dropdown.hide();
      rwt.remote.Server.getInstance().send();

      var message = TestUtil.getMessageObject();
      assertNull( message.findSetOperation( "w3", "visibility" ) );
    },

    testShow_PositionsPopUp : function() {
      prepare();

      assertEquals( 20, popup.getLeft() );
      assertEquals( 70, popup.getTop() );
    },

    testShow_SendsVisibility : function() {
      prepare();
      rwt.remote.Server.getInstance().send();

      var message = TestUtil.getMessageObject();
      assertTrue( message.findSetProperty( "w3", "visibility" ) );
    },

    testShow_DoesNotSendVisibilityInResponse : function() {
      TestUtil.fakeResponse( true );
      prepare();
      TestUtil.fakeResponse( false );
      rwt.remote.Server.getInstance().send();

      var message = TestUtil.getMessageObject();
      assertNull( message.findSetOperation( "w3", "visibility" ) );
    },

    testShow_DoesNotSendVisibilityIfAlreadyVisible : function() {
      prepare();
      TestUtil.clearRequestLog();

      dropdown.show();
      rwt.remote.Server.getInstance().send();

      var message = TestUtil.getMessageObject();
      assertNull( message.findSetOperation( "w3", "visibility" ) );
    },

    testShow_SetsPopUpWidth : function() {
      prepare();

      assertEquals( 100, popup.getWidth() );
    },

    testShow_SetVisibleItemCount : function() {
      dropdown.setVisibleItemCount( 7 );
      prepare();

      assertEquals( 7 * 23, popup.getInnerHeight() );
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

    testShow_SetsViewerFont : function() {
      var font = rwt.html.Font.fromString( "Arial 12px" );
      shell.setFont( font );
      widget.setFont( "inherit" );
      prepare();

      assertIdentical( font, viewer.getFont() );
    },

    testShow_SetsItemHeightFor10pxFont : function() {
      var font = rwt.html.Font.fromString( "Arial 10px" );
      widget.setFont( font );
      prepare();

      assertEquals( 5 + 5 + 13, viewer._itemHeight );
    },

    testShow_SetsItemHeightFor12pxFont : function() {
      var font = rwt.html.Font.fromString( "Arial 12px" );
      widget.setFont( font );
      prepare();

      assertEquals( 5 + 5 + 15, viewer._itemHeight );
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

    testAddShowListener : function() {
      var logger = TestUtil.getLogger();
      dropdown.addListener( "Show", logger.log );

      prepare();

      assertEquals( 1, logger.getLog().length );
    },

    testRemoveShowListener : function() {
      var logger = TestUtil.getLogger();
      dropdown.addListener( "Show", logger.log );
      dropdown.removeListener( "Show", logger.log );

      prepare();

      assertEquals( 0, logger.getLog().length );
    },

    testAddHideListener : function() {
      prepare();
      var logger = TestUtil.getLogger();
      dropdown.addListener( "Hide", logger.log );

      dropdown.hide();

      assertEquals( 1, logger.getLog().length );
    },

    testRemoveHideListener : function() {
      prepare();
      var logger = TestUtil.getLogger();
      dropdown.addListener( "Hide", logger.log );
      dropdown.removeListener( "Hide", logger.log );

      dropdown.hide();

      assertEquals( 0, logger.getLog().length );
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
      assertIdentical( "b", event.text );
      assertIdentical( 1, event.index );
      assertIdentical( 13, event.type );
    },

    testSelectionEventFields_Unescaped : function() {
      dropdown.setItems( [ "a", "&bb ", "c" ] ); // the *trailing* space is important
      prepare();
      var logger = TestUtil.getLogger();

      dropdown.addListener( "Selection", logger.log );
      clickItem( 1 );

      var event = logger.getLog()[ 0 ];
      assertIdentical( "&bb ", event.text );
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
      assertEquals( "", event.text );
    },

    testSelectionEventNotify_NoItemSelected : function() {
      dropdown.setItems( [ "a", "b", "c" ] );
      prepare();
      dropdown.setSelectionIndex( 1 );

      TestUtil.protocolListen( "w3", { "Selection" : true } );
      dropdown.setSelectionIndex( -1 );

      var message = TestUtil.getMessageObject();
      assertEquals( "", message.findNotifyProperty( "w3", "Selection", "text" ) );
      assertEquals( -1, message.findNotifyProperty( "w3", "Selection", "index" ) );
    },

    testSelectionEventNotify : function() {
      dropdown.setItems( [ "a", "b", "c" ] );
      prepare();

      TestUtil.protocolListen( "w3", { "Selection" : true } );
      dropdown.setSelectionIndex( 1 );

      var message = TestUtil.getMessageObject();
      assertEquals( "b", message.findNotifyProperty( "w3", "Selection", "text" ) );
      assertEquals( 1, message.findNotifyProperty( "w3", "Selection", "index" ) );
    },

    testAddDefaultSelectionListener_FiresOnEnter : function() {
      dropdown.setItems( [ "a", "b", "c" ] );
      prepare();
      var logger = TestUtil.getLogger();

      dropdown.addListener( "DefaultSelection", logger.log );
      clickItem( 1 );
      TestUtil.pressOnce( viewer, "Enter" );
      TestUtil.forceTimerOnce();

      assertEquals( 1, logger.getLog().length );
    },

    testAddDefaultSelectionListener_FiresOnTextEnter : function() {
      dropdown.setItems( [ "a", "b", "c" ] );
      prepare();
      var logger = TestUtil.getLogger();

      dropdown.addListener( "DefaultSelection", logger.log );
      clickItem( 1 );
      widget.focus();
      TestUtil.pressOnce( widget, "Enter" );
      TestUtil.forceTimerOnce();

      assertEquals( 1, logger.getLog().length );
    },

    testAddDefaultSelectionListener_FiresNotOnTextEnterWithoutSelection : function() {
      dropdown.setItems( [ "a", "b", "c" ] );
      prepare();
      var logger = TestUtil.getLogger();

      dropdown.addListener( "DefaultSelection", logger.log );
      widget.focus();
      TestUtil.pressOnce( widget, "Enter" );
      TestUtil.forceTimerOnce();

      assertEquals( 0, logger.getLog().length );
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
      TestUtil.forceTimerOnce();

      var event = logger.getLog()[ 0 ];
      assertIdentical( dropdown, event.widget );
      assertIdentical( "b", event.text );
      assertIdentical( 1, event.index );
      assertIdentical( 14, event.type );
    },

    testGetSelectionIndex_InitialValueIsMinusOne : function() {
      dropdown.setItems( [ "a", "b", "c" ] );

      assertEquals( -1, dropdown.getSelectionIndex() );
    },

    testGetSelectionIndex_ValueIsMinusOneForNoItems : function() {
      assertEquals( -1, dropdown.getSelectionIndex() );
    },

    testGetSelectionIndex_ResetValueIsMinusOne : function() {
      dropdown.setItems( [ "a", "b", "c" ] );
      dropdown.setSelectionIndex( 1 );
      dropdown.setItems( [ "a", "b", "c" ] );

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

    testSetSelectionIndex_RemoteSet : function() {
      dropdown.setItems( [ "a", "b", "c" ] );

      dropdown.setSelectionIndex( 1 );
      rwt.remote.Server.getInstance().send();

      var message = TestUtil.getMessageObject();
      assertEquals( 1, message.findSetProperty( "w3", "selectionIndex" ) );
    },

    testSetItemsFromServerDoesNotRemoteSetIndex : function() {
      dropdown.setItems( [ "a", "b", "c" ] );
      dropdown.setSelectionIndex( 1 );
      TestUtil.clearRequestLog();

      TestUtil.fakeResponse( true );
      dropdown.setItems( [ "a", "b", "c" ] );
      TestUtil.fakeResponse( false );
      rwt.remote.Server.getInstance().send();

      var message = TestUtil.getMessageObject();
      assertNull( message.findSetOperation( "w3", "selectionIndex" ) );
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

    testKeyEventForawarding_Escape : function() {
      prepare();
      var logger = TestUtil.getLogger();

      viewer.addEventListener( "keydown", logger.log );
      widget.focus();
      TestUtil.pressOnce( widget, "Escape" );

      assertEquals( 1, logger.getLog().length );
      assertTrue( logger.getLog()[ 0 ].getDefaultPrevented() );
    },

    testKeyEventForawarding_Up : function() {
      prepare();
      var logger = TestUtil.getLogger();

      viewer.addEventListener( "keydown", logger.log );
      viewer.addEventListener( "keypress", logger.log );
      widget.focus();
      TestUtil.pressOnce( widget, "Up" );

      assertEquals( 2, logger.getLog().length );
      assertTrue( logger.getLog()[ 0 ].getDefaultPrevented() );
      assertTrue( logger.getLog()[ 1 ].getDefaultPrevented() );
    },

    testKeyEventForawarding_UpNotVisible : function() {
      var logger = TestUtil.getLogger();

      viewer.addEventListener( "keypress", logger.log );
      viewer.addEventListener( "keydown", logger.log );
      widget.focus();
      TestUtil.pressOnce( widget, "Up" );

      assertEquals( 0, logger.getLog().length );
    },

    testKeyEventForawarding_Down : function() {
      prepare();
      var logger = TestUtil.getLogger();

      viewer.addEventListener( "keydown", logger.log );
      viewer.addEventListener( "keypress", logger.log );
      widget.focus();
      TestUtil.pressOnce( widget, "Down" );

      assertEquals( 2, logger.getLog().length );
      assertTrue( logger.getLog()[ 0 ].getDefaultPrevented() );
      assertTrue( logger.getLog()[ 1 ].getDefaultPrevented() );
    },

    testKeyEventForawarding_PageUp : function() {
      prepare();
      var logger = TestUtil.getLogger();

      viewer.addEventListener( "keydown", logger.log );
      viewer.addEventListener( "keypress", logger.log );
      widget.focus();
      TestUtil.pressOnce( widget, "PageUp" );

      assertEquals( 2, logger.getLog().length );
      assertTrue( logger.getLog()[ 0 ].getDefaultPrevented() );
      assertTrue( logger.getLog()[ 1 ].getDefaultPrevented() );
    },

    testKeyEventForawarding_PageDown : function() {
      prepare();
      var logger = TestUtil.getLogger();

      viewer.addEventListener( "keydown", logger.log );
      viewer.addEventListener( "keypress", logger.log );
      widget.focus();
      TestUtil.pressOnce( widget, "PageDown" );

      assertEquals( 2, logger.getLog().length );
      assertTrue( logger.getLog()[ 0 ].getDefaultPrevented() );
      assertTrue( logger.getLog()[ 1 ].getDefaultPrevented() );
    },

    testPressDownAfterSelectionResetSelectsFirstItem : function() {
      dropdown.setItems( [ "a", "b", "c" ] );
      dropdown.setSelectionIndex( 1 );
      prepare();

      dropdown.setSelectionIndex( -1 );
      TestUtil.flush();

      widget.focus();
      TestUtil.pressOnce( widget, "Down" );

      assertEquals( 0, dropdown.getSelectionIndex() );
    },

    testSelectionResetResetsLeadItem : function() {
      dropdown.setItems( [ "a", "b", "c" ] );
      prepare();
      widget.focus();
      TestUtil.pressOnce( widget, "Down" );// calling setSelectionIndex would not change lead item

      dropdown.setSelectionIndex( -1 );
      TestUtil.pressOnce( widget, "Down" );

      assertEquals( 0, dropdown.getSelectionIndex() );
    },

    testPressDownAfterItemResetSelectsFirstItem : function() {
      dropdown.setItems( [ "a", "b", "c" ] );
      prepare();
      dropdown.setSelectionIndex( 2 );

      dropdown.setItems( [ "x", "y" ] );
      TestUtil.flush();

      widget.focus();
      TestUtil.pressOnce( widget, "Down" );

      assertEquals( 0, dropdown.getSelectionIndex() );
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
      dropdown.setData( "foo", {} );
      var privateObj = dropdown._;
      var props = privateObj.widgetData;
      dropdown.destroy();

      assertTrue( TestUtil.hasNoObjects( dropdown, true ) );
      assertTrue( TestUtil.hasNoObjects( privateObj ) );
      assertTrue( TestUtil.hasNoObjects( props ) );
    },

    testDestroy_DeregistersAppearListener : function() {
      widget.setVisibility( false );

      dropdown.destroy();
      widget.setVisibility( true );
      // Succeeds by not crashing
    },

    testDestroy_RemoveListenerDoesNotCrash : function() {
      var listener = function(){};
      dropdown.addListener( "Selection", listener );
      dropdown.destroy();

      dropdown.removeListener( "Selection", listener );
    },

    testDestroy_DocumentClick : function() {
      prepare();
      dropdown.destroy();
      TestUtil.click( TestUtil.getDocument() );

      assertFalse( popup.isSeeable() );
    },

    testSearchItems_EmptyArrayReturnsEmptyResult : function() {
      var results = DropDown.searchItems( [], /foo/ );

      assertEquals( "/foo/", results.query.toString() );
      assertEquals( [], results.items );
      assertEquals( [], results.indicies );
    },

    testSearchItems_FindMultipleItems : function() {
      var items = [ "afoo", "bar", "food", "abc" ];
      var results = DropDown.searchItems( items, /foo/ );

      assertEquals( "/foo/", results.query.toString() );
      assertEquals( [ "afoo", "food" ], results.items );
      assertEquals( [ 0, 2 ], results.indicies );
      assertEquals( 0, results.limit );
    },

    testSearchItems_FindMultipleItemsWithLimit : function() {
      var items = [ "afoo", "bfoo", "x", "cfoo", "foor", "four" ];
      var results = DropDown.searchItems( items, /foo/, 3 );

      assertEquals( "/foo/", results.query.toString() );
      assertEquals( [ "afoo", "bfoo", "cfoo" ], results.items );
      assertEquals( [ 0, 1, 3 ], results.indicies );
      assertEquals( 3, results.limit );
    },

    testSearchItems_FindItemsStartingWith : function() {
      var items = [ "afoo", "bar", "food", "abc" ];
      var results = DropDown.searchItems( items, /^foo/ );

      assertEquals( "/^foo/", results.query.toString() );
      assertEquals( [ "food" ], results.items );
      assertEquals( [ 2 ], results.indicies );
    },

    testSearchItems_FromCreateQuery : function() {
      var items = [ "^foox", "bar", "food", "abc" ];
      var results = DropDown.searchItems( items, DropDown.createQuery( "^foo" ) );

      assertEquals( [ "^foox" ], results.items );
      assertEquals( [ 0 ], results.indicies );
      assertEquals( "/^\\^foo/i", results.query.toString() );
    },

    testSearchItems_FromCreateQueryCaseInsensitive : function() {
      var items = [ "ooxd", "bar", "food", "abc" ];
      var results = DropDown.searchItems( items, DropDown.createQuery( "OoX" ) );

      assertEquals( [ "ooxd" ], results.items );
      assertEquals( [ 0 ], results.indicies );
      assertEquals( "/^OoX/i", results.query.toString() );
    },

    testSearchItems_FromCreateQueryCaseSensitive : function() {
      var items = [ "fOoX", "bar", "food", "abc" ];
      var results = DropDown.searchItems( items, DropDown.createQuery( "foo", true ) );

      assertEquals( [ "food" ], results.items );
      assertEquals( [ 2 ], results.indicies );
      assertEquals( "/^foo/", results.query.toString() );
    },

    testSearchItems_FromCreateQueryIgnorePosition : function() {
      var items = [ "a^fOoX", "bar", "_food", "abc" ];
      var results = DropDown.searchItems( items, DropDown.createQuery( "foo", true, true ) );

      assertEquals( [ "_food" ], results.items );
      assertEquals( [ 2 ], results.indicies );
      assertEquals( "/foo/", results.query.toString() );
    },

    ///////////
    // Helper

    createExample : function() {
      widget = new rwt.widgets.Composite();
      widget.setFont( rwt.html.Font.fromString( "Arial 10px" ) );
      widget.setParent( shell );
      widget.setLocation( 10, 20 );
      widget.setDimension( 100, 30 );
      dropdown = new rwt.dropdown.DropDown( widget );
      rwt.remote.ObjectRegistry.add(
        "w3",
        dropdown,
        rwt.remote.HandlerRegistry.getHandler( "rwt.dropdown.DropDown" )
      );
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
