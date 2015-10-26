/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { Store } from 'flux/utils';
import Dispatcher from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';

let _modalOpen = false;

class CreateGroupStore extends Store {
  isModalOpen() {
    return _modalOpen;
  }

  __onDispatch = (action) => {
    switch (action.type) {
      case ActionTypes.GROUP_CREATE_MODAL_OPEN:
        _modalOpen = true;
        this.__emitChange();
        break;
      case ActionTypes.GROUP_CREATE_MODAL_CLOSE:
        _modalOpen = false;
        this.__emitChange();
        break;

      case ActionTypes.GROUP_CREATE:
      case ActionTypes.GROUP_CREATE_SUCCESS:
        this.__emitChange();
        break;
      case ActionTypes.GROUP_CREATE_ERROR:
        console.error('Failed to create group', action.error);
        this.__emitChange();
        break;
    }
  }
}

export default new CreateGroupStore(Dispatcher);
