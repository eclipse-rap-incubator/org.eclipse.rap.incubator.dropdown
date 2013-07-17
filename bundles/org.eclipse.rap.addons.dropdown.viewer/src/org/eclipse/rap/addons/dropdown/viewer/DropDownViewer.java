/*******************************************************************************
 * Copyright (c) 2013 EclipseSource.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.addons.dropdown.viewer;

import org.eclipse.jface.viewers.*;
import org.eclipse.rap.addons.dropdown.DropDown;
import org.eclipse.rap.addons.dropdown.internal.*;
import org.eclipse.rap.addons.dropdown.internal.resources.ResourceLoaderUtil;
import org.eclipse.rap.clientscripting.ClientListener;
import org.eclipse.rap.clientscripting.WidgetDataWhiteList;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.protocol.JsonUtil;
import org.eclipse.rap.rwt.lifecycle.WidgetUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;


@SuppressWarnings( "restriction" )
public class DropDownViewer extends ContentViewer {

  private static final String ATTR_CLIENT_LISTNER_HOLDER
    = ClientListenerHolder.class.getName() + "#instance";
  private static final String SELECTION_CHANGED = "change:elementSelection";
  private static final String VIEWER_LINK = DropDownViewer.class.getName() + "#viewer";
  private static final String ELEMENTS_KEY = "elements";
  private final static String LISTENER_PREFIX
    = "org/eclipse/rap/addons/dropdown/internal/resources/";


  private final DropDown dropDown;
  private final Text text;
  private final ClientListenerHolder clientListeners;
  private final Model model = new Model();
  private final ModelListener modelSelectionListener = new ModelSelectionListener();


  private Object[] elements;

  public DropDownViewer( Text text ) {
    this.text = text;
    checkParent();
    dropDown = new DropDown( text );
    clientListeners = getClientListenerHolder();
    setClientElements( new String[ 0 ] );
    attachClientListener();
    connectClientObjects();
    hookControl( text );
  }

  public void setAutoComplete( boolean value ) {
    model.set( "autoComplete", value );
  }

  @Override
  public Control getControl() {
    return text;
  }

  @Override
  public ISelection getSelection() {
    return null;
  }

  @Override
  public void refresh() {
    updateElements();
  }

  @Override
  public void setSelection( ISelection selection, boolean reveal ) {
    throw new UnsupportedOperationException( "Setting the selection is currently not supported" );
  }

  @Override
  public void addSelectionChangedListener( ISelectionChangedListener listener ) {
    super.addSelectionChangedListener( listener );
    model.addListener( SELECTION_CHANGED, modelSelectionListener );
  }

  @Override
  protected void inputChanged( Object input, Object oldInput ) {
    updateElements();
  }

  @Override
  protected void handleDispose( DisposeEvent event ) {
    super.handleDispose( event );
    model.dispose();
  }

  ////////////////////////
  // Getter for tests only

  Model getModel() {
    return model;
  }

  ClientListener getWidgetDataBindingListener() {
    return clientListeners.getWidgetDataBindingListener();
  }

  ClientModelListener getModelDataBindingListener() {
    return clientListeners.getModelDataBindingListener();
  }

  ClientModelListener getModelListener() {
    return clientListeners.getModelListener();
  }

  DropDown getDropDown() {
    return dropDown;
  }

  ////////////
  // Internals

  private void checkParent() {
    if( text.getData( VIEWER_LINK ) != null ) {
      throw new IllegalStateException( "Text already has a " + getClass().getSimpleName() );
    }
  }

  private void updateElements() {
    IStructuredContentProvider contentProvider = ( IStructuredContentProvider )getContentProvider();
    Object input = getInput();
    if( contentProvider != null && input != null ) {
      elements = contentProvider.getElements( input );
      updateClientElements();
    }
  }

  private void updateClientElements() {
    ILabelProvider labelProvider = ( ILabelProvider )getLabelProvider();
    if( elements != null ) {
      String[] clientElements = new String[ elements.length ];
      for( int i = 0; i < elements.length; i++ ) {
        clientElements[ i ] = labelProvider.getText( elements[ i ] );
      }
      setClientElements( clientElements );
    }
  }

  private void setClientElements( String[] elements ) {
    // TODO : Using a separate client object (e.g. "RemoteList") for the elements might allow
    //        sharing and incremental updates
    model.set( ELEMENTS_KEY, JsonUtil.createJsonArray( elements ) );
  }

  private void attachClientListener() {
    text.addListener( SWT.Modify, getWidgetDataBindingListener() );
    text.addListener( SWT.Verify, getWidgetDataBindingListener() );
    dropDown.addListener( SWT.Selection, getWidgetDataBindingListener() );
    dropDown.addListener( SWT.DefaultSelection, getWidgetDataBindingListener() );
    dropDown.addListener( SWT.Show, getWidgetDataBindingListener() );
    dropDown.addListener( SWT.Hide, getWidgetDataBindingListener() );
    model.addListener( "change", getModelDataBindingListener() );
    model.addListener( "change", getModelListener() );
    model.addListener( "accept", getModelListener() );
  }

  private void connectClientObjects() {
    // NOTE : Order is relevant, DropDown renders immediately!
    WidgetDataWhiteList.addKey( VIEWER_LINK );
    text.setData( VIEWER_LINK, model.getId() );
    dropDown.setData( VIEWER_LINK, model.getId() );
    model.set( "dropDownWidgetId", WidgetUtil.getId( dropDown ) );
    model.set( "textWidgetId", WidgetUtil.getId( text ) );
  }

  private ClientListenerHolder getClientListenerHolder() {
    Object result = RWT.getUISession().getAttribute( ATTR_CLIENT_LISTNER_HOLDER );
    if( result == null ) {
      result = new ClientListenerHolder();
      RWT.getUISession().setAttribute( ATTR_CLIENT_LISTNER_HOLDER, result );
    }
    return ( ClientListenerHolder )result;
  }

  private void fireSelectionChanged( int index ) {
    Object element = elements[ index ];
    ISelection selection = new StructuredSelection( element );
    fireSelectionChanged( new SelectionChangedEvent( this, selection ) );
  }

  private class ModelSelectionListener implements ModelListener {
    public void handleEvent( JsonObject properties ) {
      int index = properties.get( "value" ).asInt();
      fireSelectionChanged( index );
    }
  }

  private static class ClientListenerHolder {

    private final ClientModelListener modelListener = createModelListener( "ModelListener.js" );
    private final ClientListener widgetDataBinding = createListener( "DataBinding.js" );
    private final ClientModelListener modelDataBinding = createModelListener( "DataBinding.js" );

    private ClientListener createListener( String name ) {
      return new ClientListener( ResourceLoaderUtil.readTextContent( LISTENER_PREFIX + name ) );
    }

    private ClientModelListener createModelListener( String name ) {
      return new ClientModelListener( ResourceLoaderUtil.readTextContent( LISTENER_PREFIX + name ) );
    }

    public ClientModelListener getModelListener() {
      return modelListener;
    }

      public ClientModelListener getModelDataBindingListener() {
        return modelDataBinding;
    }

    public ClientListener getWidgetDataBindingListener() {
      return widgetDataBinding;
    }

  }

}
