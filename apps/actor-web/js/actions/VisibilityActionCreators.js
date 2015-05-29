var ActorClient = require('../utils/ActorClient');

var VisibilityActionCreators = {
  createAppHidden: function() {
    ActorClient.onAppHidden();
  },

  createAppVisible: function() {
    ActorClient.onAppVisible();
  }
};

module.exports = VisibilityActionCreators;
