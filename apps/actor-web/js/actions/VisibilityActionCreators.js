var ActorClient = require('../utils/ActorClient');

var VisibilityActionCreators = {
  createAppHidden: function() {
    console.warn('onAppHidden');
    ActorClient.onAppHidden();
  },

  createAppVisible: function() {
    console.warn('onAppVisible');
    ActorClient.onAppVisible();
  }
};

module.exports = VisibilityActionCreators;
