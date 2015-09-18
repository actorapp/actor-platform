/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { Store } from 'flux/utils';
import Dispatcher from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';

let _isOpen = false,
    _pictureSource = null;

class CropAvatarStore extends Store {
  isOpen() {
    return _isOpen;
  }

  getPictureSource() {
    return _pictureSource;
  }

  __onDispatch = (action) => {
    switch(action.type) {
      case ActionTypes.CROP_AVATAR_MODAL_SHOW:
        _isOpen = true;
        _pictureSource = action.source;
        this.__emitChange();
        break;
      case ActionTypes.CROP_AVATAR_MODAL_HIDE:
        _isOpen = false;
        _pictureSource = null;
        this.__emitChange();
        break;
      default:
        // no op
    }
  }
}

export default new CropAvatarStore(Dispatcher);
