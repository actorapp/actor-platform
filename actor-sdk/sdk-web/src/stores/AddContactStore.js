/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { Store } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

let _isOpen = false,
    _query = '',
    _isSearching = false,
    _results = [];

class AddContactStore extends Store {
  constructor(Dispatcher) {
    super(Dispatcher);
  }

  isOpen() {
    return _isOpen;
  }

  isSearching() {
    return _isSearching;
  }

  getQuery() {
    return _query;
  }

  getResults() {
    return _results;
  }

  setResults(results) {
    _results = results;
  }

  resetStore() {
    _isOpen = false;
    _query = '';
    _isSearching = false;
    _results = [];
  }

  __onDispatch = (action) => {
    switch(action.type) {
      case ActionTypes.CONTACT_ADD_MODAL_SHOW:
        _isOpen = true;
        this.__emitChange();
        break;
      case ActionTypes.CONTACT_ADD_MODAL_HIDE:
        this.resetStore();
        this.__emitChange();
        break;

      case ActionTypes.CONTACT_FIND:
        _query = action.query;
        _isSearching = true;
        this.__emitChange();
        break;
      case ActionTypes.CONTACT_FIND_SUCCESS:
        _isSearching = false;
        if (action.query === '') {
          this.setResults([]);
        } else {
          this.setResults(action.response)
        }
        this.__emitChange();
        break;
      case ActionTypes.CONTACT_FIND_ERROR:
        _isSearching = false;
        this.__emitChange();
        break;
      default:
    }
  };
}

export default new AddContactStore(Dispatcher);
