/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { Store } from 'flux/utils';
import Dispatcher from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';

let _isOpen = false;

class FastSwitcherStore extends Store {
  isOpen() {
    return _isOpen;
  }

  __onDispatch = (action) => {
    switch (action.type) {
      case ActionTypes.FAST_SWITCHER_SHOW:
        _isOpen = true;
        this.__emitChange();
        break;
      case ActionTypes.FAST_SWITCHER_HIDE:
        _isOpen = false;
        this.__emitChange();
        break;
    }
  }
}

export default new FastSwitcherStore(Dispatcher);
