/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { forEach, filter } from 'lodash';
import { Store } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';
import ActorClient from '../utils/ActorClient';

let _isOpen = false,
    _list = [],
    _results = [];

/**
 * Class representing a store for searchable people list.
 */
class PeopleStore extends Store {
  constructor(dispatcher) {
    super(dispatcher);
  }

  /**
   * @returns {boolean}
   */
  isOpen() {
    return _isOpen;
  }

  /**
   * @returns {Array}
   */
  getList() {
    return _list;
  }

  /**
   * @returns {Array}
   */
  getResults() {
    return _results;
  }


  handleSearchQuery(query) {
    let results = [];

    if (query === '') {
      results = _list;
    } else {
      forEach(_list, (result) => {
        const name = contact.name.toLowerCase();
        if (name.includes(query.toLowerCase())) {
          results.push(result);
        }
      })
    }

    _results = results;
  }

  __onDispatch(action) {
    switch (action.type) {
      case ActionTypes.CONTACT_LIST_SHOW:
        _isOpen  = true;
        this.handleSearchQuery('');
        this.__emitChange();
        break;
      case ActionTypes.CONTACT_LIST_HIDE:
        _isOpen  = false;
        _results = [];
        this.__emitChange();
        break;

      case ActionTypes.CONTACT_LIST_CHANGED:
        // Remove current user from contacts list
        _list = filter(action.contacts, (contact) => {
          if (contact.uid != ActorClient.getUid()) {
            return contact;
          }
        });
        this.__emitChange();
        break;

      case ActionTypes.CONTACT_LIST_SEARCH:
        this.handleSearchQuery(action.query);
        this.__emitChange();
        break;

      case ActionTypes.CONTACT_ADD:
      case ActionTypes.CONTACT_REMOVE:
        this.__emitChange();
        break;
      default:
    }
  }
}

export default new PeopleStore(Dispatcher);
