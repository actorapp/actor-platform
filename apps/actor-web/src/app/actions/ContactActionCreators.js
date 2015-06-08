'use strict';

var ActorAppDispatcher = require('../dispatcher/ActorAppDispatcher');
var ActorAppConstants = require('../constants/ActorAppConstants');
var ActionTypes = ActorAppConstants.ActionTypes;

var ContactActionCreators = {
  addContact: function(uid) {
    //console.warn('ContactActionCreators addContact', uid);
    ActorAppDispatcher.dispatch({
      type: ActionTypes.CONTACT_ADD,
      uid: uid
    })
  },

  removeContact: function(uid) {
    //console.warn('ContactActionCreators removeContact', uid);
    ActorAppDispatcher.dispatch({
      type: ActionTypes.CONTACT_REMOVE,
      uid: uid
    })
  }
};

module.exports = ContactActionCreators;
