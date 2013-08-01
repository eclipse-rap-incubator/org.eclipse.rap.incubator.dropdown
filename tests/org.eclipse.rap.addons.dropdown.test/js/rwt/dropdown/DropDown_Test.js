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

var ITEM_HEIGHT = 25; // Depends on padding which is currently taken from list theming
var PADDING_LEFT = 10;
var PADDING_RIGHT = 10;

var shell;
var widget;
var dropdown;
var popup;
var grid;
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

    testConstructor_CreatesGridInPopup : function() {
      assertTrue( grid instanceof rwt.widgets.Grid );
      assertIdentical( popup, grid.getParent() );
    },

    testConstructor_PositionsGrid : function() {
      assertEquals( 0, grid.getLeft() );
      assertEquals( 0, grid.getTop() );
    },

    testConstructor_SetsMarkupEnabledFalse : function() {
      assertTrue( !grid.getRenderConfig().markupEnabled );
    },

    testConstructor_SetsMarkupEnabledTrue : function() {
      widget = new rwt.widgets.Composite();
      widget.setParent( shell );
      dropdown = new rwt.dropdown.DropDown( widget, true );
      grid = dropdown._.grid;

      assertTrue( grid.getRenderConfig().markupEnabled );
    },

    testConstructor_FakesGridFocus : function() {
      assertTrue( grid.getRenderConfig().focused );
    },

    testConstructor_FakesGridFocusAfterBlur : function() {
      grid.setFocused( false );
      assertTrue( grid.getRenderConfig().focused );
    },

    testConstructor_HideScrollbars : function() {
      assertFalse( grid.getVerticalBar().getVisibility() );
      assertFalse( grid.getHorizontalBar().getVisibility() );
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

    testShow_DoesNotMakePopUpSeeableIfNoItemsAreSet : function() {
      showDropDown( [] );

      assertFalse( popup.isSeeable() );
    },

    testShow_MakesPopUpSeeable : function() {
      showDropDown();

      assertTrue( popup.isSeeable() );
    },

    testSetItems_MakesPopUpSeeableIfVisibleIsTrueAndLengthIsGreaterZero : function() {
      showDropDown( [] );

      dropdown.setItems( [ "a" ] );
      TestUtil.flush();

      assertTrue( popup.isSeeable() );
    },

    testSetItems_MakesPopUpNotSeeableIfLengthIsZero : function() {
      showDropDown();

      dropdown.setItems( [] );
      TestUtil.flush();

      assertFalse( popup.isSeeable() );
      assertTrue( dropdown.getVisible() );
    },

    testShow_FocusesGrid : function() {
      showDropDown();

      assertTrue( grid.getFocused() );
    },

    testShow_LeavesParentFocused : function() {
      widget.setTabIndex( 1 );
      widget.focus();
      showDropDown();

      assertFalse( grid.getFocused() );
      assertTrue( widget.getFocused() );
    },

    testShow_LeavesSiblingFocused : function() {
      var focusable = new rwt.widgets.Button( "push" );
      focusable.setParent( widget );
      TestUtil.flush();
      focusable.focus();
      showDropDown();

      assertFalse( grid.getFocused() );
      assertTrue( focusable.getFocused() );
    },

    testDoNotHideOnParentClick : function() {
      var focusable = new rwt.widgets.Button( "push" );
      focusable.setParent( widget );
      showDropDown();

      TestUtil.click( focusable );
      forceTimer();

      assertTrue( popup.isSeeable() );
    },

    testDoNotHideOnFocusableParentClick : function() {
      widget.setTabIndex( 1 );
      widget.contains = rwt.util.Functions.returnFalse;
      showDropDown();

      TestUtil.click( widget );
      forceTimer();

      assertTrue( popup.isSeeable() );
    },

    testHideOnShellClick : function() {
      showDropDown();

      TestUtil.click( shell );
      forceTimer();

      assertFalse( popup.isSeeable() );
    },

    testHideOnEscape : function() {
      showDropDown();

      TestUtil.press( grid, "Escape" );

      assertFalse( popup.isSeeable() );
    },

    testHideOnEnter : function() {
      showDropDown();
      dropdown.setItems( [ "a", "b" ] );
      dropdown.setSelectionIndex( 0 );

      TestUtil.press( grid, "Enter" );
      TestUtil.forceTimerOnce();

      assertFalse( popup.isSeeable() );
    },

    testHideOnItemClick : function() {
      dropdown.setItems( [ "a", "b" ] );
      showDropDown();

      clickItem( 1 );

      assertFalse( popup.isSeeable() );
    },

    testDoNotHideOnScrollbarClick : function() {
      dropdown.setItems( [ "a", "b", "c" ] );
      dropdown.setVisibleItemCount( 2 );
      showDropDown();

      TestUtil.click( grid.getVerticalBar() );

      assertTrue( popup.isSeeable() );
    },

    testShellClickAfterDropDownDisposeDoesNotCrash : function() {
      showDropDown();
      dropdown.destroy();
      TestUtil.flush();

      TestUtil.click( shell );
      forceTimer();

      assertFalse( popup.isSeeable() );
    },

    testShellClickAfterWidgetDisposeDoesNotCrash : function() {
      widget.focus();
      widget.destroy();
      dropdown.destroy();
      TestUtil.flush();

      TestUtil.click( shell );
      forceTimer();

      assertFalse( popup.isSeeable() );
    },

    testShow_CalledBeforeCreatedMakesPopUpVisible : function() {
      dropdown.destroy();
      this.createExample();

      showDropDown();

      assertTrue( popup.isSeeable() );
    },

    testHide_MakesPopUpInvisible : function() {
      showDropDown();

      dropdown.hide();
      TestUtil.flush();

      assertFalse( popup.isSeeable() );
    },

    testHide_SendsVisible : function() {
      showDropDown();

      dropdown.hide();
      rwt.remote.Server.getInstance().send();

      var message = TestUtil.getMessageObject();
      assertFalse( message.findSetProperty( "w3", "visible" ) );
    },

    testHide_DoesNotSendVisibleInResponse : function() {
      showDropDown();
      TestUtil.clearRequestLog();

      TestUtil.fakeResponse( true );
      dropdown.hide();
      TestUtil.fakeResponse( false );
      rwt.remote.Server.getInstance().send();

      var message = TestUtil.getMessageObject();
      assertNull( message.findSetOperation( "w3", "visible" ) );
    },

    testHide_DoesNotSendVisibleIfAlreadyInvisible : function() {
      dropdown.hide();
      rwt.remote.Server.getInstance().send();

      var message = TestUtil.getMessageObject();
      assertNull( message.findSetOperation( "w3", "visible" ) );
    },

    testShow_PositionsPopUp : function() {
      showDropDown();

      assertEquals( 20, popup.getLeft() );
      assertEquals( 70, popup.getTop() );
    },

    testShow_PositionsPopUpWhileParentIsInLayoutQueueForTop : function() {
      widget.setTop( 50 );
      showDropDown();

      assertEquals( 20, popup.getLeft() );
      assertEquals( 100, popup.getTop() );
    },

    testShow_PositionsPopUpWhileParentIsInLayoutQueueForLeft : function() {
      widget.setLeft( 20 );
      showDropDown();

      assertEquals( 30, popup.getLeft() );
      assertEquals( 70, popup.getTop() );
    },

    testShow_PositionsPopUpWhileParentIsInLayoutQueueForHeight : function() {
      widget.setHeight( 50 );
      showDropDown();

      assertEquals( 20, popup.getLeft() );
      assertEquals( 90, popup.getTop() );
    },

    testShow_LayoutsPopUpWhileParentIsInLayoutQueueForWidth : function() {
      widget.setWidth( 150 );
      showDropDown();

      assertEquals( 150, popup.getWidth() );
    },

    testShow_SendsVisible : function() {
      showDropDown();
      rwt.remote.Server.getInstance().send();

      var message = TestUtil.getMessageObject();
      assertTrue( message.findSetProperty( "w3", "visible" ) );
    },

    testShow_DoesNotSendVisibleInResponse : function() {
      TestUtil.fakeResponse( true );
      showDropDown();
      TestUtil.fakeResponse( false );
      rwt.remote.Server.getInstance().send();

      var message = TestUtil.getMessageObject();
      assertNull( message.findSetOperation( "w3", "visible" ) );
    },

    testShow_DoesNotSendVisibleIfAlreadyVisible : function() {
      showDropDown();
      TestUtil.clearRequestLog();

      dropdown.show();
      rwt.remote.Server.getInstance().send();

      var message = TestUtil.getMessageObject();
      assertNull( message.findSetOperation( "w3", "visible" ) );
    },

    testShow_SetsPopUpWidth : function() {
      showDropDown();

      assertEquals( 100, popup.getWidth() );
    },

    testShow_PopUpInnerHeightOverVisibleItemCount : function() {
      var items = [ "a", "b", "c", "d", "a", "b", "c", "d", "a", "b", "c", "d", "a", "b", "c" ];
      dropdown.setItems( items );
      dropdown.setVisibleItemCount( 7 );
      showDropDown();

      assertEquals( 7 * ITEM_HEIGHT, popup.getInnerHeight() );
    },

    testShow_PopUpInnerHeightUnderVisibleItemCount : function() {
      var items = [ "a", "b" ];
      dropdown.setItems( items );
      dropdown.setVisibleItemCount( 7 );
      showDropDown();

      assertEquals( 2 * ITEM_HEIGHT, popup.getInnerHeight() );
    },

    testSetVisibleItemCount_UpdatesScrollbar : function() {
      dropdown.setItems( [ "a", "b", "c" ] );

      dropdown.setVisibleItemCount( 2 );

      assertTrue( grid.getVerticalBar().getDisplay() );
    },

    testSetVisibleItemCount_RendersLayout : function() {
      dropdown.setItems( [ "a", "b", "c" ] );
      dropdown.setVisibleItemCount( 3 );
      showDropDown();

      dropdown.setVisibleItemCount( 2 );
      TestUtil.flush();

      assertEquals( 2 * ITEM_HEIGHT, popup.getInnerHeight() );
      assertEquals( 2 * ITEM_HEIGHT, grid.getHeight() );
    },

    testSetItems_UpdatesScrollbar : function() {
      dropdown.setVisibleItemCount( 2 );

      dropdown.setItems( [ "a", "b", "c" ] );

      assertTrue( grid.getVerticalBar().getDisplay() );
    },

    testSetItems_RendersLayout : function() {
      dropdown.setItems( [ "a", "b", "c" ] );
      dropdown.setVisibleItemCount( 3 );
      showDropDown();

      dropdown.setItems( [ "a", "b" ] );
      TestUtil.flush();

      assertEquals( 2 * ITEM_HEIGHT, popup.getInnerHeight() );
      assertEquals( 2 * ITEM_HEIGHT, grid.getHeight() );
    },

    testShow_LayoutsGrid : function() {
      showDropDown();

      assertEquals( popup.getInnerWidth(), grid.getWidth() );
      assertEquals( popup.getInnerHeight(), grid.getHeight() );
    },

    testShow_setsItemMetrics: function() {
      showDropDown();

      var config = grid.getRenderConfig();
      assertEquals( 0, config.itemLeft[ 0 ] );
      assertEquals( grid.getWidth(), config.itemWidth[ 0 ] );
      assertEquals( PADDING_LEFT, config.itemTextLeft[ 0 ] );
      assertEquals( grid.getWidth() - PADDING_RIGHT - PADDING_LEFT, config.itemTextWidth[ 0 ] );
    },

    testShow_SetsGridFont : function() {
      var font = rwt.html.Font.fromString( "Arial 12px" );
      shell.setFont( font );
      widget.setFont( "inherit" );
      showDropDown();

      assertIdentical( font, grid.getFont() );
    },

    testShow_SetsItemHeightFor10pxFont : function() {
      var font = rwt.html.Font.fromString( "Arial 10px" );
      widget.setFont( font );
      showDropDown();

      assertEquals( 6 + 6 + 13, grid._itemHeight );
    },

    testShow_SetsItemHeightFor12pxFont : function() {
      var font = rwt.html.Font.fromString( "Arial 12px" );
      widget.setFont( font );
      showDropDown();

      assertEquals( 6 + 6 + 15, grid._itemHeight );
    },

    testSetItems_SetsItemsOnGrid : function() {
      showDropDown();

      dropdown.setItems( [ "a", "b", "c" ] );

      assertEquals( [ "a", "b", "c" ], getGridItems() );
    },

    testSetColumns_setsColumnCount : function() {
      dropdown.setColumns( [ 30, 40 ] );

      var config = grid.getRenderConfig();
      assertEquals( 2, config.columnCount );
    },

    testSetColumns_setsItemMetrics : function() {
      dropdown.setColumns( [ 30, 40 ] );
      showDropDown();

      var config = grid.getRenderConfig();
      assertEquals( 0, config.itemLeft[ 0 ] );
      assertEquals( 30, config.itemWidth[ 0 ] );
      assertEquals( 30, config.itemLeft[ 1 ] );
      assertEquals( 40, config.itemWidth[ 1 ] );
      assertEquals( PADDING_LEFT, config.itemTextLeft[ 0 ] );
      assertEquals( 30 - PADDING_RIGHT - PADDING_LEFT, config.itemTextWidth[ 0 ] );
      assertEquals( 30 + PADDING_LEFT, config.itemTextLeft[ 1 ] );
      assertEquals( 40 - PADDING_RIGHT - PADDING_LEFT, config.itemTextWidth[ 1 ] );
    },

    testSetColumns_setsPopUpWidth : function() {
      dropdown.setColumns( [ 100, 200, 100 ] );
      showDropDown();

      assertEquals( 400, popup.getInnerWidth() );
    },

    testSetColumns_setsDoesNotSetPopUpWidthIfSmaller : function() {
      dropdown.setColumns( [ 30 ] );
      showDropDown();

      assertEquals( 100, popup.getWidth() );
    },

    testSetColumns_separatesItemsByTabulator : function() {
      dropdown.setColumns( [ 30, 40 ] );
      var tab = String.fromCharCode( 9 );

      dropdown.setItems( [ "foo" + tab + "bar" ] );
      showDropDown();

      var gridItem = grid.getRootItem().getChild( 0 );
      assertEquals( "foo", gridItem.getText( 0 ) );
      assertEquals( "bar", gridItem.getText( 1 ) );
    },

    testGetItemCount : function() {
      showDropDown();

      dropdown.setItems( [ "a", "b", "c" ] );

      assertEquals( 3, dropdown.getItemCount() );
    },

    testGetVisible_returnsFalse : function() {
      assertFalse( dropdown.getVisible() );
    },

    testGetVisible_returnsTrue : function() {
      showDropDown();
      assertTrue( dropdown.getVisible() );
    },

    testAddSelectionListener : function() {
      dropdown.setItems( [ "a", "b", "c" ] );
      showDropDown();
      var logger = TestUtil.getLogger();

      dropdown.addListener( "Selection", logger.log );
      dropdown.setSelectionIndex( 1 );

      assertEquals( 1, logger.getLog().length );
    },

    testAddShowListener : function() {
      var logger = TestUtil.getLogger();
      dropdown.addListener( "Show", logger.log );

      showDropDown();

      assertEquals( 1, logger.getLog().length );
    },

    testRemoveShowListener : function() {
      var logger = TestUtil.getLogger();
      dropdown.addListener( "Show", logger.log );
      dropdown.removeListener( "Show", logger.log );

      showDropDown();

      assertEquals( 0, logger.getLog().length );
    },

    testAddHideListener : function() {
      showDropDown();
      var logger = TestUtil.getLogger();
      dropdown.addListener( "Hide", logger.log );

      dropdown.hide();

      assertEquals( 1, logger.getLog().length );
    },

    testRemoveHideListener : function() {
      showDropDown();
      var logger = TestUtil.getLogger();
      dropdown.addListener( "Hide", logger.log );
      dropdown.removeListener( "Hide", logger.log );

      dropdown.hide();

      assertEquals( 0, logger.getLog().length );
    },

    testRemoveSelectionListener : function() {
      dropdown.setItems( [ "a", "b", "c" ] );
      showDropDown();
      var logger = TestUtil.getLogger();

      dropdown.addListener( "Selection", logger.log );
      dropdown.removeListener( "Selection", logger.log );
      TestUtil.click( getItem( 0 ) );

      assertEquals( 0, logger.getLog().length );
    },

    testSelectionEventFields : function() {
      dropdown.setItems( [ "a", "b", "c" ] );
      showDropDown();
      var logger = TestUtil.getLogger();

      dropdown.addListener( "Selection", logger.log );
      dropdown.setSelectionIndex( 1 );

      var event = logger.getLog()[ 0 ];
      assertIdentical( dropdown, event.widget );
      assertIdentical( "b", event.text );
      assertIdentical( 1, event.index );
      assertIdentical( 13, event.type );
    },

    testSelectionEventFields_Unescaped : function() {
      dropdown.setItems( [ "a", "&bb ", "c" ] ); // the *trailing* space is important
      showDropDown();
      var logger = TestUtil.getLogger();

      dropdown.addListener( "Selection", logger.log );
      clickItem( 1 );

      var event = logger.getLog()[ 0 ];
      assertIdentical( "&bb ", event.text );
    },

    testSelectionEventFields_NoItemSelected : function() {
      dropdown.setItems( [ "a", "b", "c" ] );
      showDropDown();
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
      showDropDown();
      dropdown.setSelectionIndex( 1 );

      TestUtil.protocolListen( "w3", { "Selection" : true } );
      dropdown.setSelectionIndex( -1 );

      var message = TestUtil.getMessageObject();
      assertEquals( "", message.findNotifyProperty( "w3", "Selection", "text" ) );
      assertEquals( -1, message.findNotifyProperty( "w3", "Selection", "index" ) );
    },

    testSelectionEventNotify : function() {
      dropdown.setItems( [ "a", "b", "c" ] );
      showDropDown();

      TestUtil.protocolListen( "w3", { "Selection" : true } );
      dropdown.setSelectionIndex( 1 );

      var message = TestUtil.getMessageObject();
      assertEquals( "b", message.findNotifyProperty( "w3", "Selection", "text" ) );
      assertEquals( 1, message.findNotifyProperty( "w3", "Selection", "index" ) );
    },

    testAddDefaultSelectionListener_FiresOnEnter : function() {
      dropdown.setItems( [ "a", "b", "c" ] );
      showDropDown();
      var logger = TestUtil.getLogger();

      dropdown.addListener( "DefaultSelection", logger.log );
      dropdown.setSelectionIndex( 1 );
      TestUtil.pressOnce( grid, "Enter" );
      TestUtil.forceTimerOnce();

      assertEquals( 1, logger.getLog().length );
    },

    testAddDefaultSelectionListener_FiresOnClick : function() {
      dropdown.setItems( [ "a", "b", "c" ] );
      showDropDown();
      var logger = TestUtil.getLogger();

      dropdown.addListener( "DefaultSelection", logger.log );
      clickItem( 1 );
      TestUtil.forceTimerOnce();

      assertEquals( 1, logger.getLog().length );
    },

    testAddDefaultSelectionListener_FiresOnTextEnter : function() {
      dropdown.setItems( [ "a", "b", "c" ] );
      showDropDown();
      var logger = TestUtil.getLogger();

      dropdown.addListener( "DefaultSelection", logger.log );
      dropdown.setSelectionIndex( 1 );
      widget.focus();
      TestUtil.pressOnce( widget, "Enter" );
      TestUtil.forceTimerOnce();

      assertEquals( 1, logger.getLog().length );
    },

    testAddDefaultSelectionListener_HidesNotOnTextEnterWithoutSelection : function() {
      dropdown.setItems( [ "a", "b", "c" ] );
      showDropDown();
      var logger = TestUtil.getLogger();

      dropdown.addListener( "DefaultSelection", logger.log );
      widget.focus();
      TestUtil.pressOnce( widget, "Enter" );
      TestUtil.forceTimerOnce();

      assertTrue( dropdown.getVisible() );
    },

    testDefaultSelectionEventFields : function() {
      dropdown.setItems( [ "a", "b", "c" ] );
      showDropDown();
      var logger = TestUtil.getLogger();

      dropdown.addListener( "DefaultSelection", logger.log );
      clickItem( 1 );
      TestUtil.pressOnce( grid, "Enter" );
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

    testGetSelectionIndex_ValueAfterSelection : function() {
      dropdown.setItems( [ "a", "b", "c" ] );
      showDropDown();

      clickItem( 1 );

      assertEquals( 1, dropdown.getSelectionIndex() );
    },

    testSetSelectionIndex : function() {
      dropdown.setItems( [ "a", "b", "c" ] );

      dropdown.setSelectionIndex( 1 );

      assertEquals( 1, dropdown.getSelectionIndex() );
    },

    testSetSelectionIndex_ScrollToSelection : function() {
      dropdown.setVisibleItemCount( 3 );
      showDropDown();
      dropdown.setItems( [ "a", "b", "c", "d", "e", "f" ] );

      dropdown.setSelectionIndex( 5 );

      assertEquals( 3, grid.getTopItemIndex() );
    },


    testSetSelectionIndex_DoesNotFireEventIfValueIsUnchanged : function() {
      dropdown.setItems( [ "a", "b", "c" ] );
      showDropDown();
      var logger = TestUtil.getLogger();

      dropdown.addListener( "Selection", logger.log );
      dropdown.setSelectionIndex( 1 );
      dropdown.setSelectionIndex( 1 );

      assertEquals( 1, logger.getLog().length );
    },

    testResetSelectionIndex_ResetScrollPosition : function() {
      dropdown.setVisibleItemCount( 3 );
      showDropDown();
      dropdown.setItems( [ "a", "b", "c", "d", "e", "f" ] );
      dropdown.setSelectionIndex( 5 );

      dropdown.setSelectionIndex( -1 );

      assertEquals( 0, grid.getTopItemIndex() );
    },

    testSetItems_ResetsSelection : function() {
      dropdown.setItems( [ "a", "b", "c" ] );
      dropdown.setSelectionIndex( 1 );
      dropdown.setItems( [ "a", "b", "d" ] );

      assertEquals( -1, dropdown.getSelectionIndex() );
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

    testKeyEventForwarding_Escape : function() {
      showDropDown();
      var logger = TestUtil.getLogger();

      grid.addEventListener( "keypress", logger.log );
      widget.focus();
      TestUtil.pressOnce( widget, "Escape" );

      assertEquals( 1, logger.getLog().length );
      assertTrue( logger.getLog()[ 0 ].getDefaultPrevented() );
    },

    testKeyEventForwarding_Up : function() {
      showDropDown( [ "a", "b" ] );
      dropdown.setSelectionIndex( 1 );
      var logger = TestUtil.getLogger();

      grid.addEventListener( "keypress", logger.log );
      widget.focus();
      TestUtil.pressOnce( widget, "Up" );

      assertEquals( 1, logger.getLog().length );
      assertTrue( logger.getLog()[ 0 ].getDefaultPrevented() );
    },

    testKeyEventForwarding_UpNotVisible : function() {
      var logger = TestUtil.getLogger();

      grid.addEventListener( "keypress", logger.log );
      widget.focus();
      TestUtil.pressOnce( widget, "Up" );

      assertEquals( 0, logger.getLog().length );
    },

    testKeyEventForwarding_Down : function() {
      dropdown.setItems( [ "a", "b", "c" ] );
      dropdown.setSelectionIndex( 1 );
      showDropDown();
      var logger = TestUtil.getLogger();

      grid.addEventListener( "keypress", logger.log );
      widget.focus();
      TestUtil.pressOnce( widget, "Down" );

      assertEquals( 1, logger.getLog().length );
      assertTrue( logger.getLog()[ 0 ].getDefaultPrevented() );
    },

    testKeyEventForwarding_PageUp : function() {
      dropdown.setItems( [ "a", "b", "c" ] );
      dropdown.setSelectionIndex( 1 );
      showDropDown();
      var logger = TestUtil.getLogger();

      grid.addEventListener( "keypress", logger.log );
      widget.focus();
      TestUtil.pressOnce( widget, "PageUp" );

      assertEquals( 1, logger.getLog().length );
      assertTrue( logger.getLog()[ 0 ].getDefaultPrevented() );
    },

    testKeyEventForwarding_PageDown : function() {
      dropdown.setItems( [ "a", "b", "c" ] );
      dropdown.setSelectionIndex( 1 );
      showDropDown();
      var logger = TestUtil.getLogger();

      grid.addEventListener( "keypress", logger.log );
      widget.focus();
      TestUtil.pressOnce( widget, "PageDown" );

      assertEquals( 1, logger.getLog().length );
      assertTrue( logger.getLog()[ 0 ].getDefaultPrevented() );
    },

    testPressDownAfterSelectionResetSelectsFirstItem : function() {
      dropdown.setItems( [ "a", "b", "c" ] );
      dropdown.setSelectionIndex( 1 );
      showDropDown();
      dropdown.setSelectionIndex( -1 );
      TestUtil.flush();

      widget.focus();
      TestUtil.pressOnce( widget, "Down" );

      assertEquals( 0, dropdown.getSelectionIndex() );
    },

    testPressDownAfterSelectionResetFocusesFirstItem : function() {
      dropdown.setItems( [ "a", "b", "c" ] );
      dropdown.setSelectionIndex( 1 );
      showDropDown();
      dropdown.setSelectionIndex( -1 );
      TestUtil.flush();

      widget.focus();
      TestUtil.pressOnce( widget, "Down" );

      assertTrue( grid.isFocusItem( grid.getRootItem().getChild( 0 ) ) );
    },

    testPressUpAfterSelectionResetsSelectsLastItem : function() {
      dropdown.setItems( [ "a", "b", "c" ] );
      showDropDown();
      dropdown.setSelectionIndex( -1 );
      TestUtil.flush();

      widget.focus();
      TestUtil.pressOnce( widget, "Up" );

      assertEquals( 2, dropdown.getSelectionIndex() );
    },

    testPressUpAfterSelectionFirstItemResetsFocus : function() {
      dropdown.setItems( [ "a", "b", "c" ] );
      showDropDown();
      TestUtil.flush();

      widget.focus();
      TestUtil.pressOnce( widget, "Down" );
      TestUtil.pressOnce( widget, "Up" );

      assertFalse( grid.isFocusItem( grid.getRootItem().getChild( 0 ) ) );
    },

    testPressUpOnFirstItemResetsSelection : function() {
      dropdown.setItems( [ "a", "b", "c" ] );
      dropdown.setSelectionIndex( 0 );
      showDropDown();
      TestUtil.flush();

      widget.focus();
      TestUtil.pressOnce( widget, "Up" );

      assertEquals( -1, dropdown.getSelectionIndex() );
    },

    testPressDownOnLastItemResetsSelection : function() {
      dropdown.setItems( [ "a", "b", "c" ] );
      dropdown.setSelectionIndex( 2 );
      showDropDown();
      TestUtil.flush();

      widget.focus();
      TestUtil.pressOnce( widget, "Down" );

      assertEquals( -1, dropdown.getSelectionIndex() );
    },

    testSelectionResetResetsLeadItem : function() {
      dropdown.setItems( [ "a", "b", "c" ] );
      showDropDown();
      widget.focus();
      TestUtil.pressOnce( widget, "Down" );// calling setSelectionIndex would not change lead item

      dropdown.setSelectionIndex( -1 );
      TestUtil.pressOnce( widget, "Down" );

      assertEquals( 0, dropdown.getSelectionIndex() );
    },

    testPressDownAfterItemResetSelectsFirstItem : function() {
      dropdown.setItems( [ "a", "b", "c" ] );
      showDropDown();
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

    testDestroy_AfterControlDispose : function() {
      widget.destroy();
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

    testDestroy_DeregistersFlushListener : function() {
      dropdown.destroy();
      widget.setTop( 45 );
      TestUtil.flush();
      // Succeeds by not crashing
    },

    testDestroy_RemoveListenerDoesNotCrash : function() {
      var listener = function(){};
      dropdown.addListener( "Selection", listener );
      dropdown.destroy();

      dropdown.removeListener( "Selection", listener );
    },

    testDestroy_DocumentClick : function() {
      showDropDown();
      dropdown.destroy();
      TestUtil.click( TestUtil.getDocument() );

      assertFalse( popup.isSeeable() );
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
      grid = dropdown._.grid;
      hideTimer = dropdown._.hideTimer;
    }

  }

} );

var showDropDown = function( items ) {
  if( items ) {
    dropdown.setItems( items );
  } else if( dropdown.getItemCount() === 0 ) {
    dropdown.setItems( [ "a" ] );
  }
  dropdown.show();
  TestUtil.flush();
  TestUtil.flush();
};

var getGridItems = function() {
  var result = [];
  var items = grid.getRootItem()._children;
  for( var i = 0; i < items.length; i++ ) {
    result[ i ] = items[ i ].getText( 0 );
  }
  return result;
};

var clickItem = function( index ) {
  TestUtil.click( getItem( index ) );
};

var doubleClickItem = function( index ) {
  TestUtil.doubleClick( getItem( index ) );
};

var getItem = function( index ) {
  return grid.getRowContainer()._findRowByItem( grid.getRootItem().getChild( index ) );
};

var forceTimer = function() {
  TestUtil.forceInterval( hideTimer );
};


}());
