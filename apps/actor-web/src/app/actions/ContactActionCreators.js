'use strict';

var ActorAppDispatcher = require('../dispatcher/ActorAppDispatcher');
var ActorAppConstants = require('../constants/ActorAppConstants');
var ActionTypes = ActorAppConstants.ActionTypes;

var ContactActionCreators = {
  showContactList: function() {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.CONTACT_LIST_SHOW
    })
  },

  hideContactList: function() {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.CONTACT_LIST_HIDE
    })
  },

  setContacts: function(contacts) {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.CONTACT_LIST_CHANGED,
      contacts: contacts
    });
  },

  addContact: function(uid) {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.CONTACT_ADD,
      uid: uid
    })
  },

  removeContact: function(uid) {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.CONTACT_REMOVE,
      uid: uid
    })
  }
};

module.exports = ContactActionCreators;
