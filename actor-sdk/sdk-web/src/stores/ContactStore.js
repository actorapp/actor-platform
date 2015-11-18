/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { filter } from 'lodash';
import { EventEmitter } from 'events';
import assign from 'object-assign';
import { register , waitFor } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';
import ActorClient from '../utils/ActorClient';

import ContactActionCreators from '../actions/ContactActionCreators';

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

ContactStore.dispatchToken = register(function(action) {
  switch(action.type) {
    case ActionTypes.CONTACT_LIST_SHOW:
      _isContactsOpen = true;
      ContactStore.emitChange();
      break;

    case ActionTypes.CONTACT_LIST_HIDE:
      _isContactsOpen = false;
      ContactStore.emitChange();
      break;

    case ActionTypes.CONTACT_LIST_CHANGED:
      // Remove current user from contacts list
      _contacts = filter(action.contacts, (contact) => {
        if (contact.uid != ActorClient.getUid()) {
          return contact;
        }
      });
      ContactStore.emitChange();
      break;

    case ActionTypes.CONTACT_ADD:
    case ActionTypes.CONTACT_REMOVE:
      ContactStore.emitChange();
      break;
    default:
  }
});

export default ContactStore;
