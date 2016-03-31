/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { Store } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

let isVisible = false;

class VisibilityStore extends Store {
  constructor(Dispatcher) {
    super(Dispatcher);
  }

  isAppVisible() {
    return isVisible;
  }

  __onDispatch(action) {
    switch (action.type) {
      case ActionTypes.APP_VISIBLE:
        isVisible = true;
        this.__emitChange();
        break;
      case ActionTypes.APP_HIDDEN:
        isVisible = false;
        this.__emitChange();
        break;
      default:
    }
  }
}

export default new VisibilityStore(Dispatcher);
