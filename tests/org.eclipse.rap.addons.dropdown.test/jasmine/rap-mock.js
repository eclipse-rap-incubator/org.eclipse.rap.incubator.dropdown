RapMock = function() {

  this.fakeRemoteObject = {
    set : function(){},
    notify : function(){},
    call : function(){}
  };

  this.typeHandler = {};


};

RapMock.prototype = {

  on: function() {},

  off: function() {},

  registerTypeHandler : function( type, handler ) {
  this.typeHandler[ type ] = handler;
  },

  getObject : function() {
    return this.fakeComposite;
  },

  getRemoteObject : function() {
    return this.fakeRemoteObject;
  }

};

rap = new RapMock();
