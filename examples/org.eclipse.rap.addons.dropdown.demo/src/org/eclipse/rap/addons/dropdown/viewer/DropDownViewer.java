package org.eclipse.rap.addons.dropdown.viewer;

import static org.eclipse.rap.addons.dropdown.viewer.ResourceLoaderUtil.readTextContent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.rap.addons.dropdown.DropDown;
import org.eclipse.rap.clientscripting.ClientListener;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.JavaScriptLoader;
import org.eclipse.rap.rwt.internal.client.WidgetDataWhiteList;
import org.eclipse.rap.rwt.internal.client.WidgetDataWhiteListImpl;
import org.eclipse.rap.rwt.internal.remote.RemoteObjectImpl;
import org.eclipse.rap.rwt.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.remote.AbstractOperationHandler;
import org.eclipse.rap.rwt.service.ResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;


@SuppressWarnings("restriction")
public class DropDownViewer {

  private static final String SELECTION_CHANGED = "SelectionChanged";
  private String PATH_PREFIX = "/org/eclipse/rap/addons/dropdown/viewer/";
  private static final String LINKED_CONTROL_KEY = "org.eclipse.rap.addons.dropdown.viewer.linkedControl";
  private static final String INPUT_KEY = "org.eclipse.rap.addons.dropdown.viewer.input";
  private static final String VIEWER_KEY = "org.eclipse.rap.addons.dropdown.viewer";
  private static final String REMOTE_TYPE = "rwt.dropdown.DropDownViewer";
  private static final String DROP_DOWN_VIWER_JS = "DropDownViewer.js";
  private DropDown dropdown;
  private Object[] input;
  private LabelProvider labelProvider;
  private List<SelectionChangedListener> selectionChangedListeners
    = new ArrayList<SelectionChangedListener>();
  private RemoteObjectImpl remoteObject;

  public DropDownViewer( Text text ) {
    dropdown = new DropDown( text );
    ensureClientViewer();
    remoteObject
      = ( RemoteObjectImpl )RWT.getUISession().getConnection().createRemoteObject( REMOTE_TYPE );
    addWidgetDataKey( INPUT_KEY );
    addWidgetDataKey( LINKED_CONTROL_KEY );
    addWidgetDataKey( VIEWER_KEY );
    // TODO : Split client listeners and make replaceable
    addDropDownClientListener( dropdown );
    addTextClientListener( text );
    dropdown.setData( LINKED_CONTROL_KEY, WidgetUtil.getId( text ) );
    dropdown.setData( VIEWER_KEY, remoteObject.getId() );
    text.setData( LINKED_CONTROL_KEY, WidgetUtil.getId( dropdown ) );
    remoteObject.setHandler( new AbstractOperationHandler() {
      @Override
      public void handleNotify( String event, Map<String, Object> properties ) {
        DropDownViewer.this.handleNotify( event, properties );
      }
    } );
  }

  public void setInput( List<?> elements ) {
    input = elements.toArray();
    updateInput();
  }

  public void setLabelProvider( LabelProvider provider ) {
    labelProvider = provider;
  }

  public void addSelectionChangedListener( SelectionChangedListener listener ) {
    selectionChangedListeners.add( listener );
    remoteObject.listen( SELECTION_CHANGED, true );
  }

  private void updateInput() {
    String[] labels = new String[ input.length ];
    for( int i = 0; i < labels.length; i++ ) {
      labels[ i ] = labelProvider.getText( input[ i ] );
    }
    dropdown.setData( INPUT_KEY, labels );
  }

  private void handleNotify( String eventType, Map<String, Object> properties ) {
    if( SELECTION_CHANGED.equals( eventType ) ) {
      int index = ( Integer )properties.get( "index" );
      SelectionChangedEvent event = new SelectionChangedEvent( input[ index ] );
      for( SelectionChangedListener listener : selectionChangedListeners ) {
        listener.selectionChanged( event );
      }
    }
  }

  // TODO : implement this in RAP core
  private static void addWidgetDataKey( String key ) {
    WidgetDataWhiteListImpl service
      = ( WidgetDataWhiteListImpl )RWT.getClient().getService( WidgetDataWhiteList.class );
    List<String> list = new ArrayList<String>( Arrays.asList( service.getKeys() ) );
    if( !list.contains( key ) ) {
      list.add( key );
      service.setKeys( list.toArray( new String[ list.size() ]) );
    }
  }

  private void addDropDownClientListener( DropDown dropdown ) {
    String script = readTextContent( PATH_PREFIX + "DropDownEventHandler.js" );
    ClientListener listener = new ClientListener( script );
    listener.addTo( dropdown, SWT.Selection );
    listener.addTo( dropdown, SWT.DefaultSelection );
  }

  private void addTextClientListener( Text text ) {
    String script = readTextContent( PATH_PREFIX + "TextEventHandler.js" );
     // TODO: should take inputStream or loader + path
    ClientListener listener = new ClientListener( script );
    listener.addTo( text, SWT.Modify );
    listener.addTo( text, SWT.Verify );
    listener.addTo( text, SWT.KeyDown );
  }

 private void ensureClientViewer() {
   ResourceManager manager = RWT.getResourceManager();
   if( !manager.isRegistered( DROP_DOWN_VIWER_JS ) ) {
     manager.register( DROP_DOWN_VIWER_JS, getClass().getResourceAsStream( DROP_DOWN_VIWER_JS ) );
   }
   JavaScriptLoader jsl = RWT.getClient().getService( JavaScriptLoader.class );
   jsl.require( manager.getLocation( DROP_DOWN_VIWER_JS ) );
 }

}
