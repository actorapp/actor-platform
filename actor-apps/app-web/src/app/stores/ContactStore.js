/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { EventEmitter } from 'events';
import assign from 'object-assign';
import { register , waitFor } from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';
import ActorClient from 'utils/ActorClient';

import ContactActionCreators from 'actions/ContactActionCreators';
import LoginStore from 'stores/LoginStore';

const CONTACTS_CHANGE_EVENT = 'contacts_change';

let _contacts = [];
let _isContactsOpen = false;

var ContactStore = assign({}, EventEmitter.prototype, {
  emitChange: function() {
    this.emit(CONTACTS_CHANGE_EVENT);
  },

  addChangeListener: function(callback) {
    this.on(CONTACTS_CHANGE_EVENT, callback);
  },

  removeChangeListener: function(callback) {
    this.removeListener(CONTACTS_CHANGE_EVENT, callback);
  },

  getContacts: function() {
    return _contacts;
  },

  isContactsOpen: function() {
    return _isContactsOpen;
  }
});

const setContacts = (contacts) => {
  console.debug(contacts);
  setTimeout(function() {
    ContactActionCreators.setContacts(contacts);
  }, 0);
};

ContactStore.dispatchToken = register(function(action) {
  switch(action.type) {
    case ActionTypes.SET_LOGGED_IN:
      waitFor([LoginStore.dispatchToken]);
      ActorClient.bindContacts(setContacts);
      break;

    case ActionTypes.CONTACT_LIST_SHOW:
      _isContactsOpen = true;
      ContactStore.emitChange();
      break;

    case ActionTypes.CONTACT_LIST_HIDE:
      _isContactsOpen = false;
      ContactStore.emitChange();
      break;

    case ActionTypes.CONTACT_LIST_CHANGED:
      _contacts = action.contacts;
      ContactStore.emitChange();
      break;

    case ActionTypes.CONTACT_ADD:
      ActorClient.addContact(action.uid);
      ContactStore.emitChange();
      break;

    case ActionTypes.CONTACT_REMOVE:
      ActorClient.removeContact(action.uid);
      ContactStore.emitChange();
      break;
    default:
      return;
  }
});

export default ContactStore;
