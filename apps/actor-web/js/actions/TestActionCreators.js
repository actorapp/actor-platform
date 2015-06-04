var ActorClient = require('../utils/ActorClient');

var TestActionCreators = {
  editMyName: function(string) {
    ActorClient.editMyName(string);
  }
};

module.exports = TestActionCreators;
