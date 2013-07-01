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
package org.eclipse.rap.addons.dropdown.demo.examples;

import org.eclipse.rap.addons.dropdown.demo.examples.CountryInfo.City;
import org.eclipse.swt.browser.Browser;


public class CityMap {

  private static final String HTML
    = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"\n"
    + "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n"
    + "<html>\n"
    + "<head>\n"
    + "  <style type=\"text/css\">\n"
    + "html { height: 100% }\n"
    + "body { height: 100%; margin: 0px; padding: 0px; }\n"
    + "#map_canvas { height: 100% }\n"
    + "  </style>\n"
    + "  <script type=\"text/javascript\" "
    + "     src=\"http://maps.google.com/maps/api/js?sensor=false\" ></script>\n"
    + "  <script typte=\"text/javascript\" >\n"
    + "   function init() {\n"
    + "     var parent = document.getElementById( \"map_canvas\" );\n"
    + "     window.gmap = new google.maps.Map( parent, {\n"
    + "       disableDefaultUI : true,\n"
    + "       center : new google.maps.LatLng( 0, 0 ),\n"
    + "       zoom : 2,\n"
    + "       mapTypeId : google.maps.MapTypeId.ROADMAP\n"
    + "     } );\n"
    + "   };\n"
    + "  </script>"
    + "</head>\n"
    + "\n"
    + "<body onload=\"init()\">\n"
    + "  <div id=\"map_canvas\" style=\"width:100%;height:100%\" ></div>\n"
    + "</body>\n"
    + "\n"
    + "</html>";
  private Browser browser;

  CityMap( Browser browser ) {
    this.browser = browser;
    browser.setText( HTML );
  }

  public void visit( City city ) {
    browser.evaluate(
        "gmap.panTo( new google.maps.LatLng( " + city.latitude + ", " + city.longitude + " ) );\n"
      + "gmap.setZoom( 12 );\n"
    );
  }

}
