/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { forEach } from 'lodash';

import { Store } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes, PeerTypes } from '../constants/ActorAppConstants';

let _isOpen = false,
    _list = [],
    _results = [];

class QuickSearchStore extends Store {
  constructor(dispatcher) {
    super(dispatcher);
  }

  isOpen() {
    return _isOpen;
  }

  getResults() {
    return _results;
  }

  handleSearchQuery(query) {
    let results = [];

    if (query === '') {
      results = _list;
    } else {
      forEach(_list, (result) => {
        if (result.peerInfo.title.toLowerCase().includes(query.toLowerCase())) {
          results.push(result);
        }
      })
    }

    _results = results;
    this.__emitChange();
  }

  __onDispatch = (action) => {
    switch (action.type) {
      case ActionTypes.QUICK_SEARCH_SHOW:
        _isOpen = true;
        this.handleSearchQuery('');
        this.__emitChange();
        break;

      case ActionTypes.QUICK_SEARCH_HIDE:
        _isOpen = false;
        _results = [];
        this.__emitChange();
        break;

      case ActionTypes.QUICK_SEARCH_CHANGED:
        _list = action.list;
        this.__emitChange();
        break;

      case ActionTypes.QUICK_SEARCH:
        this.handleSearchQuery(action.query);
        break;
    }
  };
}

export default new QuickSearchStore(Dispatcher);
