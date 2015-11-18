/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { Store } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes, AsyncActionStates } from '../constants/ActorAppConstants';
import ActorClient from '../utils/ActorClient';

let _isOpen = false,
    _groupList = [],
    _query = '';

class GroupStore extends Store {
  constructor(Dispatcher) {
    super(Dispatcher);
  }

  isGroupsOpen() {
    return _isOpen;
  }

  getList() {
    return _groupList;
  }

  getSearchQuery() {
    return _query;
  }

  __onDispatch = (action) => {
    switch (action.type) {
      case ActionTypes.GROUP_LIST_HIDE:
        _isOpen  = false;
        _query = '';
        this.__emitChange();
        break;

      case ActionTypes.GROUP_LIST_LOAD:
        _isOpen  = true;
        this.__emitChange();
        break;
      case ActionTypes.GROUP_LIST_LOAD_SUCCESS:
        _groupList = action.response;
        this.__emitChange();
        break;
      case ActionTypes.GROUP_LIST_LOAD_ERROR:
        console.erro(action.error);
        this.__emitChange();
        break;

      case ActionTypes.GROUP_LIST_SEARCH:
        _query = action.query;
        this.__emitChange();
        break;
    }
  }
}

export default new GroupStore(Dispatcher);
