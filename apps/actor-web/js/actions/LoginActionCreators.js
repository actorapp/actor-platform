var ActorClient = require('../utils/ActorClient');

var ActorAppDispatcher = require('../dispatcher/ActorAppDispatcher');
var ActorAppConstants = require('../constants/ActorAppConstants');

var ActionTypes = ActorAppConstants.ActionTypes;

module.exports = {
  setLoggedIn: function() {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.SET_LOGGED_IN
    });
  }
};
