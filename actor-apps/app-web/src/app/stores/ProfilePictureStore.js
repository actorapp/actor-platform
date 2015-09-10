/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { Store } from 'flux/utils';
import Dispatcher from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';

let _isOpen = false;

class ProfilePictureStore extends Store {
  isOpen() {
    return _isOpen;
  }
  __onDispatch = (action) => {
    switch(action.type) {
      case ActionTypes.PROFILE_PICTURE_MODAL_SHOW:
        _isOpen = true;
        this.__emitChange();
        break;
      case ActionTypes.PROFILE_PICTURE_MODAL_HIDE:
        _isOpen = false;
        this.__emitChange();
        break;
      default:
        // no op
    }
  }
}

export default new ProfilePictureStore(Dispatcher);
