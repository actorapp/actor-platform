/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { forEach } from 'lodash';
import { Store } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

let _isOpen = false,
    _list = [],
    _results = [];

/**
 * Class representing a store for searchable group list.
 */
class GroupStore extends Store {
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
        const title = result.peerInfo.title.toLowerCase();
        if (title.includes(query.toLowerCase())) {
          results.push(result);
        }
      })
    }

    _results = results;
  }

  __onDispatch(action) {
    switch (action.type) {
      case ActionTypes.GROUP_LIST_SHOW:
        _isOpen  = true;
        this.handleSearchQuery('');
        this.__emitChange();
        break;
      case ActionTypes.GROUP_LIST_HIDE:
        _isOpen  = false;
        _results = [];
        this.__emitChange();
        break;

      case ActionTypes.GROUP_LIST_LOAD_SUCCESS:
        _list = action.response;
        this.handleSearchQuery('');
        this.__emitChange();
        break;
      case ActionTypes.GROUP_LIST_LOAD_ERROR:
        console.error(action.error);
        this.__emitChange();
        break;

      case ActionTypes.GROUP_LIST_SEARCH:
        this.handleSearchQuery(action.query);
        this.__emitChange();
        break;

      default:
    }
  }
}

export default new GroupStore(Dispatcher);
