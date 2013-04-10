package org.eclipse.rap.addons.dropdown.viewer;

import static org.eclipse.rap.addons.dropdown.viewer.ResourceLoaderUtil.readTextContent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.rap.addons.dropdown.DropDown;
import org.eclipse.rap.clientscripting.ClientListener;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.client.WidgetDataWhiteList;
import org.eclipse.rap.rwt.internal.client.WidgetDataWhiteListImpl;
import org.eclipse.rap.rwt.lifecycle.WidgetUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;


@SuppressWarnings("restriction")
public class DropDownViewer {

  private String PATH_PREFIX = "/org/eclipse/rap/addons/dropdown/viewer/";
  private static final String LINKED_CONTROL_KEY = "org.eclipse.rap.addons.dropdown.viewer.linkedControl";
  private static final String INPUT_KEY = "org.eclipse.rap.addons.dropdown.viewer.input";
  private DropDown dropdown;
  private Object[] input;
  private LabelProvider labelProvider;

  public DropDownViewer( Text text ) {
    dropdown = new DropDown( text );
    addWidgetDataKey( INPUT_KEY );
    addWidgetDataKey( LINKED_CONTROL_KEY );
    // TODO : Split client listeners and make replaceable
    addDropDownClientListener( dropdown );
    addTextClientListener( text );
    dropdown.setData( LINKED_CONTROL_KEY, WidgetUtil.getId( text ) );
    text.setData( LINKED_CONTROL_KEY, WidgetUtil.getId( dropdown ) );
  }

  public void setInput( List<?> elements ) {
    input = elements.toArray();
    updateInput();
  }

  public void setLabelProvider( LabelProvider provider ) {
    labelProvider = provider;
  }

  private void updateInput() {
    String[] labels = new String[ input.length ];
    for( int i = 0; i < labels.length; i++ ) {
      labels[ i ] = labelProvider.getText( input[ i ] );
    }
    dropdown.setData( INPUT_KEY, labels );
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

}
