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
package org.eclipse.rap.addons.autosuggest;

import static org.eclipse.rap.rwt.lifecycle.WidgetUtil.getId;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rap.addons.autosuggest.internal.resources.AutoSuggestListener;
import org.eclipse.rap.addons.autosuggest.internal.resources.EventDelegatorListener;
import org.eclipse.rap.addons.autosuggest.internal.resources.ModelResources;
import org.eclipse.rap.addons.dropdown.DropDown;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.scripting.ClientListenerUtil;
import org.eclipse.rap.rwt.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.remote.AbstractOperationHandler;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.rap.rwt.scripting.ClientListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * Instances of this class provide a complete text input suggestion mechanism
 * for a given {@link Text} widget. Suggestions may be displayed below the <code>Text</code> widget
 * via a {@link DropDown} widget and optionally inserted automatically on uniqueness.
 *
 * <p>
 *   All possible suggestions have to be provided by a {@link DataSource}. The
 *   <code>DataSource</code> also determines how the suggestions are presented (e.g. as a simple
 *   list or as a table), and how suggestions are filtered for any given input.
 * <p>
 *
 * <p>
 *   A {@link SuggestionSelectedListener} may be registered to detect when the user accepts
 *   a suggestion.
 * <p>
 *
 * <p>
 *   This class may be subclassed to provide a different JavaScript implementation.
 * </p>
 */
@SuppressWarnings( "restriction" )
public class AutoSuggest {

  private static final String EVENT_TYPE_SELECTION = "suggestionSelected";
  private static final String REMOTE_TYPE = "rwt.remote.Model";
  private static final String MODEL_ID_KEY = "org.eclipse.rap.addons.autosuggest#Model";

  private final Text text;
  private final DropDown dropDown;
  private final List<SuggestionSelectedListener> selectionListeners;
  private ClientListener textClientListener;
  private int[] textClientListenerTypes;
  private boolean isDisposed;

  /**
   * Represents the client side object containing all data required by AutoSuggest to work
   */
  protected final RemoteObject remoteObject;

  /**
   * Constructs a new instance of this class given a <code>Text</code> instance.
   *
   * @param text the <code>Text</code> widget for which suggestions are provided (cannot be null)
   *
   * @exception NullPointerException when text is null
   * @exception IllegalArgumentException when text is disposed
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created text</li>
   * </ul>
   *
   * @see Text
   **/
  public AutoSuggest( Text text ) {
    if( text == null ) {
      throw new NullPointerException( "Text must not be null" );
    }
    if( text.isDisposed() ) {
      throw new IllegalArgumentException( "Text must not be disposed" );
    }
    this.text = text;
    dropDown = new DropDown( text );
    ModelResources.ensure();
    remoteObject = RWT.getUISession().getConnection().createRemoteObject( REMOTE_TYPE );
    remoteObject.setHandler( new AbstractOperationHandler() {
      @Override
      public void handleNotify( String event, JsonObject properties ) {
        if( EVENT_TYPE_SELECTION.equals( event ) ) {
          notifySelectionListeners();
        }
      }
    } );
    selectionListeners = new ArrayList<SuggestionSelectedListener>( 1 );
    connectClientObjects();
    attachClientListeners();
    text.addListener( SWT.Dispose, new Listener() {
      public void handleEvent( Event event ) {
        dispose();
      }
    } );
  }
  /**
   * Sets the receiver's dataSource that provides, filters, and formats suggestions
   *
   * @param dataSource the DataSource (can be null)
   *
   * @exception IllegalStateException when the receiver is disposed
   *
   * <p>
   * NOTE: The dataSource may be changed at any time
   * </p>
   */
  public void setDataSource( DataSource dataSource ) {
    checkDisposed();
    remoteObject.set( "dataSourceId", dataSource != null ? dataSource.getId() : null );
    if( dataSource != null ) {
      ColumnTemplate template = dataSource.getTemplate();
      if( template != null ) {
        dropDown.setData( "columns", template.getColumnWidths() );
      }
    }
  }

  /**
   * Sets the maximum number of suggestion items that can be visible simultaneously
   *
   * @param itemCount the new number of items to be visible (default is 5)
   *
   * @exception IllegalStateException when the receiver is disposed
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setVisibleItemCount( int itemCount ) {
    checkDisposed();
    dropDown.setVisibleItemCount( itemCount );
  }

  /**
   * Gets the maximum number of suggestion items that can be visible simultaneously
   *
   * @return the number of items to be visible
   *
   * @exception IllegalStateException when the receiver is disposed
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public int getVisibleItemCount() {
    checkDisposed();
    return dropDown.getVisibleItemCount();
  }

  /**
   * Controls whether a single remaining suggestion or the common part of multiple remaining
   * suggestions are to be inserted into the text automatically. The inserted part will be selected.
   *
   * @param value true to enable the feature (default is false)
   *
   * @exception IllegalStateException when the receiver is disposed
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setAutoComplete( boolean value ) {
    checkDisposed();
    remoteObject.set( "autoComplete", value );
  }

  /**
   * Registers a {@link SuggestionSelectedListener} to be notified when the user selects a
   * suggestion to be inserted into the text. Adding the same listener multiple times has no effect.
   *
   * @param listener the listener to be notified (may not be null)
   *
   * @exception IllegalStateException when the receiver is disposed
   * @exception NullPointerException when listener is null
   *
   * @see AutoSuggest#removeSelectionListener(SuggestionSelectedListener)
   */
  public void addSelectionListener( SuggestionSelectedListener listener ) {
    checkDisposed();
    if( listener == null ) {
      throw new NullPointerException( "Parameter was null: listener" );
    }
    if( !selectionListeners.contains( listener ) ) {
      selectionListeners.add( listener );
    }
    if( selectionListeners.size() == 1 ) {
      remoteObject.listen( EVENT_TYPE_SELECTION, true );
    }
  }

  /**
   * Unregisters a {@link SuggestionSelectedListener} to no longer be notified by the receiver.
   * If the listener is not registered, nothing happens.
   *
   * @param listener the listener to be removed (may not be null)
   *
   * @exception IllegalStateException when the receiver is disposed
   * @exception NullPointerException when listener is null
   *
   * @see AutoSuggest#addSelectionListener(SuggestionSelectedListener)
   */
  public void removeSelectionListener( SuggestionSelectedListener listener ) {
    checkDisposed();
    if( listener == null ) {
      throw new NullPointerException( "Parameter was null: listener" );
    }
    selectionListeners.remove( listener );
    if( selectionListeners.size() == 0 ) {
      remoteObject.listen( EVENT_TYPE_SELECTION, false );
    }
  }

  /**
   * Disposes the receiver with all resources it created, <em>but not the
   * <code>Text</code> instance it is attached to or the <code>DataSource</code> that may be
   * attached to it.</em> If the instance is already disposed, nothing happens.
   */
  public void dispose() {
    if( !isDisposed ) {
      isDisposed = true;
      dropDown.dispose();
      remoteObject.destroy();
      removeTextClientListeners();
    }
  }

  /**
   * Indicates whether the receiver has been disposed.
   *
   * @return true if the receiver is disposed
   */
  public boolean isDisposed() {
    return isDisposed;
  }

  DropDown getDropDown() {
    return dropDown;
  }

  void notifySelectionListeners() {
    for( SuggestionSelectedListener listener : selectionListeners ) {
      listener.suggestionSelected();
    }
  }

  private void checkDisposed() {
    if( isDisposed ) {
      throw new IllegalStateException( "AutoSuggest is disposed" );
    }
  }

  private void attachClientListeners() {
    int[] dropDownEventTypes = new int[] { SWT.Show, SWT.Hide, SWT.Selection, SWT.DefaultSelection };
    attachClientListenerToDropDown( EventDelegatorListener.getInstance(), dropDownEventTypes );
    attachClientListenerToText( EventDelegatorListener.getInstance(), getTextEventTypes() );
    attachClientListenerToModel( getAutoSuggestListener(), "change", "accept" );
  }

  /**
   * May be overwritten to control which event types the internal ClientListner receives from
   * the <code>Text</code> widget. Default are <code>SWT.Modify</code> and <code>SWT.Verify</code>
   *
   * @see AutoSuggest#getAutoSuggestListener()
   */
  protected int[] getTextEventTypes() {
    return new int[]{ SWT.Modify, SWT.Verify };
  }

  /**
   * May be overwritten to provide a different ClientListener to handle all client-side
   * events fired by <code>Text</code>, <code>DropDown</code> or the <code>AutoSuggest</code>
   * itself.
   */
  protected ClientListener getAutoSuggestListener() {
    return AutoSuggestListener.getInstance();
  }

  private void attachClientListenerToText( ClientListener listener, int... types ) {
    textClientListenerTypes = types;
    textClientListener = listener;
    for( int type : types ) {
      text.addListener( type, listener );
    }
  }

  private void attachClientListenerToDropDown( ClientListener listener, int... types ) {
    for( int type : types ) {
      dropDown.addListener( type, listener );
    }
  }

  private void attachClientListenerToModel( ClientListener listener, String... types ) {
    String listenerId = ClientListenerUtil.getRemoteId( listener );
    for( String type : types ) {
      remoteObject.call( "addListener",
                         new JsonObject().add( "listener", listenerId ).add( "type", type ) );
    }
    remoteObject.set( "autoSuggestListenerId", listenerId );
  }

  private void connectClientObjects() {
    WidgetUtil.registerDataKeys( MODEL_ID_KEY );
    remoteObject.set( "textWidgetId", getId( text ) );
    remoteObject.set( "dropDownWidgetId", getId( dropDown ) );
    dropDown.setData( MODEL_ID_KEY, remoteObject.getId() );
    text.setData( MODEL_ID_KEY, remoteObject.getId() );
  }

  private void removeTextClientListeners() {
    for( int type : textClientListenerTypes ) {
      text.removeListener( type, textClientListener );
    }
  }

}
