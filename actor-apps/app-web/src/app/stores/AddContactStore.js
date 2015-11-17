/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { Store } from 'flux/utils';
import Dispatcher from 'dispatcher/ActorAppDispatcher';
import { ActionTypes, AddContactMessages } from 'constants/ActorAppConstants';

let _isOpen = false,
    _message = null,
    _query = '';

class AddContactStore extends Store {
  isModalOpen() {
    return _isOpen;
  }

  getMessage() {
    return _message;
  }

  getQuery() {
    return _query;
  }

  __onDispatch = (action) => {
    switch(action.type) {
      case ActionTypes.CONTACT_ADD_MODAL_SHOW:
        _isOpen = true;
        this.__emitChange();
        break;
      case ActionTypes.CONTACT_ADD_MODAL_HIDE:
        _isOpen = false;
        _message = null;
        _query = '';
        this.__emitChange();
        break;

      //case ActionTypes.CONTACT_FIND:
      //case ActionTypes.CONTACT_FIND_SUCCESS:
      //case ActionTypes.CONTACT_FIND_ERROR:
      //  this.__emitChange();
      //  break;

      case ActionTypes.CONTACT_ADD_MODAL_FIND_USER_OK:
        _isOpen = false;
        _message = null;
        this.__emitChange();
        break;
      case ActionTypes.CONTACT_ADD_MODAL_FIND_USER_UNREGISTERED:
        _message = AddContactMessages.PHONE_NOT_REGISTERED;
        this.__emitChange();
        break;
      case ActionTypes.CONTACT_ADD_MODAL_FIND_USER_IN_CONTACT:
        _message = AddContactMessages.ALREADY_HAVE;
        this.__emitChange();
        break;
      default:
    }
  }
}

export default new AddContactStore(Dispatcher);
