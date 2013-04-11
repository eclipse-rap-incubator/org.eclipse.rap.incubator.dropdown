(function() {

  // TODO : could a RemoteObject be created without a type, i.e. the type is the RemoteObject?
  rap.registerTypeHandler( "rwt.dropdown.DropDownViewer", {

    factory : function() {
      return new Viewer();
    },

    events : [ "SelectionChanged" ]

  } );

  var Viewer = function(){};
  Viewer.prototype = {
    notifySelectionChanged : function( index ) {
      rap.getRemoteObject( this ).notify( "SelectionChanged", {
        "index" : index
      } );
    }
  };

}());