/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { Store } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';
import ActorClient from '../utils/ActorClient';

let _isOpen = false;
let _message = {};
let _targetRect = {};

class DropdownStore extends Store {
  constructor(dispatcher) {
    super(dispatcher);
  }

  isOpen(rid) {
    if (rid === _message.rid) {
      return _isOpen;
    } else {
      return false;
    }
  }

  getMessage() {
    return _message;
  }

  getTargetRect() {
    return _targetRect;
  }

  __onDispatch(action) {
    switch(action.type) {
      case ActionTypes.DROPDOWN_SHOW:
        _isOpen = true;
        _message = action.message;
        _targetRect = action.targetRect;
        this.__emitChange();
        break;
      case ActionTypes.DROPDOWN_HIDE:
        _isOpen = false;
        this.__emitChange();
        break;
      default:
    }
  }
}

export default new DropdownStore(Dispatcher);
