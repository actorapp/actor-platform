/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { Store } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

class DropdownStore extends Store {
  constructor(dispatcher) {
    super(dispatcher);

    this._isMessageDropdownOpen = false;
    this._isRecentContextOpen = false;
    this._targetRect = {};
    this._contextPos = {}
    this._message = {};
    this._peer = {};
  }

  isMessageDropdownOpen(rid) {
    if (rid === this._message.rid) {
      return this._isMessageDropdownOpen;
    } else {
      return false;
    }
  }

  isRecentContextOpen() {
    return this._isRecentContextOpen;
  }

  getMessage() {
    return this._message;
  }

  getTargetRect() {
    return this._targetRect;
  }

  getContextPos() {
    return this._contextPos;
  }

  getPeer() {
    return this._peer;
  }

  __onDispatch(action) {
    switch(action.type) {
      case ActionTypes.MESSAGE_DROPDOWN_SHOW:
        this._isMessageDropdownOpen = true;
        this._isRecentContextOpen = false;
        this._message = action.message;
        this._targetRect = action.targetRect;
        this.__emitChange();
        break;
      case ActionTypes.MESSAGE_DROPDOWN_HIDE:
        this._isMessageDropdownOpen = false;
        this.__emitChange();
        break;

      case ActionTypes.RECENT_CONTEXT_MENU_SHOW:
        this._isRecentContextOpen = true;
        this._isMessageDropdownOpen = false;
        this._contextPos = action.contextPos;
        this._peer = action.peer;
        this.__emitChange();
        break;
      case ActionTypes.RECENT_CONTEXT_MENU_HIDE:
        this._isRecentContextOpen = false;
        this.__emitChange();
        break;
      default:
    }
  }
}

export default new DropdownStore(Dispatcher);
