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
package org.eclipse.rap.addons.dropdown.demo;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rap.addons.dropdown.demo.data.Movies;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.application.AbstractEntryPoint;
import org.eclipse.rap.rwt.widgets.DropDown;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;


public class DropDownDemo extends AbstractEntryPoint {

  private String[] currentTexts = new String[ 0 ] ;
  private String userText = "";

  @Override
  protected void createContents( Composite parent ) {
    parent.setLayout( new FormLayout() );
    final Text text = new Text( parent, SWT.BORDER );
    text.setLayoutData( createLayoutData() );
    text.setMessage( "80's Movies" );
    final DropDown dropdown = new DropDown( text );
    dropdown.setData( RWT.MARKUP_ENABLED, Boolean.TRUE );
    addModifyListener( text, dropdown );
    addSelectionListener( text, dropdown );
    addDefaultSelectionListener( text, dropdown );
    dropdown.setData( "columns", new int[] { 300, 60 } ); // provisional API!
  }

  public void addModifyListener( final Text text, final DropDown dropdown ) {
    text.addListener( SWT.Modify, new Listener() {
      public void handleEvent( Event event ) {
        if( text.getData( "selecting" ) != Boolean.TRUE ) {
          userText = text.getText();
          if( userText.length() >= 2 ) {
            dropdown.setVisible( true );
            String searchStr = userText.toLowerCase();
            currentTexts = filter( Movies.VALUES, searchStr, 10 );
            dropdown.setItems( format( currentTexts, searchStr ) );
            if( currentTexts.length > 10 ) {
              dropdown.setSelectionIndex( -1 );
            } else if( currentTexts.length == 1 ) {
              dropdown.setSelectionIndex( 0 );
            }
          } else {
            dropdown.setVisible( false );
          }
        }
      }
    } );
  }

  public void addSelectionListener( final Text text, final DropDown dropdown ) {
    dropdown.addListener( SWT.Selection, new Listener() {
      public void handleEvent( Event event ) {
        if( event.index != -1 ) {
          text.setData( "selecting", Boolean.TRUE );
          text.setText( currentTexts[ event.index ] );
          text.setData( "selecting", Boolean.FALSE );
          text.selectAll();
        } else {
          text.setText( userText );
          text.setSelection( userText.length(), userText.length() );
          text.setFocus();
        }
      }
    } );
  }

  public void addDefaultSelectionListener( final Text text, final DropDown dropdown ) {
    dropdown.addListener( SWT.DefaultSelection, new Listener() {
      public void handleEvent( Event event ) {
        if( event.index != -1 ) {
          text.setText( currentTexts[ event.index ] );
          text.setSelection( event.text.length() );
          dropdown.setVisible( false );
        }
      }
    } );
  }

  private static String[] filter( String[] values, String text, int limit ) {
    List<String> result = new ArrayList<String>( limit );
    for( int i = 0; result.size() < limit && i < values.length; i++ ) {
      String item = values[ i ];
      if( item.toLowerCase().startsWith( text ) ) {
        result.add( item );
      }
    }
    return result.toArray( new String[ result.size() ] );
  }

  private static String[] format( String[] values, String text ) {
    String[] result = new String[ values.length ];
    for( int i = 0; i < values.length; i++ ) {
      String item = values[ i ];
      int length = text.length();
      int rating = ( int )( Math.random() * 10 );
      result[ i ] =  "<b>"
                    + item.substring( 0, length )
                    + "</b>"
                    + item.substring( length )
                    + "\t"
                    + rating
                    + "/10";
    }
    return result;
  }

  private static FormData createLayoutData() {
    FormData formData = new FormData();
    formData.top = new FormAttachment( 0, 100 );
    formData.left = new FormAttachment( 50, -150 );
    formData.right = new FormAttachment( 50, 150 );
    return formData;
  }
}
