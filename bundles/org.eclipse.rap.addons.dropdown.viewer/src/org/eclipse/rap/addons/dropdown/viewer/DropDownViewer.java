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

import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.viewers.*;
import org.eclipse.rap.addons.dropdown.DropDown;
import org.eclipse.rap.addons.dropdown.internal.*;
import org.eclipse.rap.addons.dropdown.viewer.internal.resources.ResourceLoaderUtil;
import org.eclipse.rap.clientscripting.ClientListener;
import org.eclipse.rap.clientscripting.WidgetDataWhiteList;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.JavaScriptLoader;
import org.eclipse.rap.rwt.internal.protocol.JsonUtil;
import org.eclipse.rap.rwt.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.service.ResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.internal.widgets.ControlDecorator;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;


@SuppressWarnings( "restriction" )
public class DropDownViewer extends ContentViewer {

  private static final String MODEL_REMOTE_OBJECT_JS
    = "rwt/remote/Model.js";
  private static final String ATTR_CLIENT_LISTNER_HOLDER
    = ClientListenerHolder.class.getName() + "#instance";
  private static final String SELECTION_CHANGED = "SelectionChanged";
  private static final String VIEWER_LINK = DropDownViewer.class.getName() + "#viewer";
  private static final String DROPDOWN_KEY = "dropDown";
  private static final String TEXT_KEY = "text";
  private static final String DECORATOR_KEY = "decorator";
  private static final String ELEMENTS_KEY = "elements";
  private static final String SELECTION_KEY = "selection";

  private final DropDown dropDown;
  private final Text text;
  private final ClientListenerHolder clientListeners;
  private final Model model = new Model();
  private final ModelListener modelSelectionListener = new ModelSelectionListener();


  private Object[] elements;
  private ControlDecorator decorator;

  public DropDownViewer( Text text ) {
    this.text = text;
    checkParent();
    ensureTypeHandler();
    dropDown = new DropDown( text );
    clientListeners = getClientListenerHolder();
    setClientElements( new String[ 0 ] );
    createControlDecorator();
    attachClientListener();
    linkClientObjects();
    hookControl( text );
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
    model.destroy();
  }

  public void setReadOnly( boolean value ) {
    model.set( "readOnly", JsonValue.valueOf( value ) );
  }

  public void showDropDown() {
    dropDown.show();
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
    model.set( SELECTION_KEY, -1 );
    model.notify( "refresh" );
  }

  private void createControlDecorator() {
    decorator = new ControlDecorator( text, SWT.LEFT | SWT.TOP, null );
    decorator.setMarginWidth( 2 );
    decorator.setImage( getDecorationImage( FieldDecorationRegistry.DEC_ERROR ) );
    decorator.hide();
  }

  private static Image getDecorationImage( String id ) {
    FieldDecorationRegistry registry = FieldDecorationRegistry.getDefault();
    FieldDecoration decoration = registry.getFieldDecoration( id );
    return decoration.getImage();
  }

  private void attachClientListener() {
    text.addListener( ClientListener.Modify, getTextModifyListener() );
    text.addListener( ClientListener.Verify, getTextVerifyListener() );
    text.addListener( ClientListener.KeyDown, getTextKeyDownListener() );
    text.addListener( ClientListener.MouseDown, getTextMouseDownListener() );
    dropDown.addListener( ClientListener.Selection, getDropDownSelectionListener() );
    dropDown.addListener( ClientListener.DefaultSelection, getDropDownDefaultSelectionListener() );
    dropDown.addListener( ClientListener.Show, getDropDownShowListener() );
    dropDown.addListener( ClientListener.Hide, getDropDownHideListener() );
    model.addListener( "refresh", getRefreshListener() );
  }

  private void linkClientObjects() {
    // NOTE : Order is relevant, DropDown renders immediately!
    WidgetDataWhiteList.addKey( VIEWER_LINK );
    text.setData( VIEWER_LINK, model.getId() );
    dropDown.setData( VIEWER_LINK, model.getId() );
    model.set( DROPDOWN_KEY, WidgetUtil.getId( dropDown ) );
    model.set( TEXT_KEY, WidgetUtil.getId( text ) );
    model.set( DECORATOR_KEY, WidgetUtil.getId( decorator ) );
  }

  Model getModel() {
    return model;
  }

  ClientListener getTextModifyListener() {
    return clientListeners.getTextModifyListener();
  }

  ClientListener getTextVerifyListener() {
    return clientListeners.getTextVerifyListener();
  }

  ClientListener getTextKeyDownListener() {
    return clientListeners.getTextKeyDownListener();
  }

  ClientListener getTextMouseDownListener() {
    return clientListeners.getTextMouseDownListener();
  }

  ClientModelListener getRefreshListener() {
    return clientListeners.getRefreshListener();
  }

  DropDown getDropDown() {
    return dropDown;
  }

  ControlDecorator getDecorator() {
    return decorator;
  }

  ClientListener getDropDownSelectionListener() {
    return clientListeners.getDropDownSelectionListener();
  }

  ClientListener getDropDownDefaultSelectionListener() {
    return clientListeners.getDropDownDefaultSelectionListener();
  }

  ClientListener getDropDownShowListener() {
    return clientListeners.getDropDownShowListener();
  }

  ClientListener getDropDownHideListener() {
    return clientListeners.getDropDownHideListener();
  }

  // TODO : no longer planned to be replaceable, streamline.
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

  private void ensureTypeHandler() {
    ResourceManager manager = RWT.getResourceManager();
    if( !manager.isRegistered( MODEL_REMOTE_OBJECT_JS ) ) {
      manager.register(
        MODEL_REMOTE_OBJECT_JS,
        getClass().getClassLoader().getResourceAsStream( MODEL_REMOTE_OBJECT_JS )
      );
    }
    JavaScriptLoader jsl = RWT.getClient().getService( JavaScriptLoader.class );
    jsl.require( manager.getLocation( MODEL_REMOTE_OBJECT_JS ) );
  }

  private class ModelSelectionListener implements ModelListener {
    public void handleEvent( JsonObject properties ) {
      int index = properties.get( "index" ).asInt();
      fireSelectionChanged( index );
    }
  }

  private static class ClientListenerHolder {

    private final String PREFIX = "org/eclipse/rap/addons/dropdown/viewer/internal/resources/";

    private final ClientListener textListener = createListener( "TextEventListener.js" );
    private final ClientListener dropDownListener = createListener( "DropDownEventListener.js" );
    private final ClientModelListener refreshListener
      = createModelListener( "RefreshListener.js" );

    private ClientListener createListener( String name ) {
      return new ClientListener( ResourceLoaderUtil.readTextContent( PREFIX + name ) );
    }

    private ClientModelListener createModelListener( String name ) {
      return new ClientModelListener( ResourceLoaderUtil.readTextContent( PREFIX + name ) );
    }

    public ClientModelListener getRefreshListener() {
      return refreshListener;
    }

    public ClientListener getDropDownShowListener() {
      return dropDownListener;
    }

    public ClientListener getDropDownHideListener() {
      return dropDownListener;
    }

    public ClientListener getTextModifyListener() {
      return textListener;
    }

    public ClientListener getDropDownDefaultSelectionListener() {
      return dropDownListener;
    }

    public ClientListener getDropDownSelectionListener() {
      return dropDownListener;
    }

    public ClientListener getTextKeyDownListener() {
      return textListener;
    }

    public ClientListener getTextMouseDownListener() {
      return textListener;
    }

    public ClientListener getTextVerifyListener() {
      return textListener;
    }

  }

}
