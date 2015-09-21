/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { Store } from 'flux/utils';
import Dispatcher from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';
import ActorClient from 'utils/ActorClient';

let _isOpen = false,
    _title = '',
    _about = '',
    _group = {};

class EditGroupStore extends Store {
  isOpen() {
    return _isOpen;
  }

  getGroup() {
    return _group;
  }

  getAbout() {
    return _about;
  }

  getTitle() {
    return _title;
  }

  setGroup(group) {
    _group = group;
    _title = _group.name;
    _about = _group.about;
  }
  __onDispatch = action => {
    switch (action.type) {
      case ActionTypes.GROUP_EDIT_MODAL_SHOW:
        _isOpen = true;
        this.setGroup(action.group);
        this.__emitChange();
        break;

      case ActionTypes.GROUP_INFO_CHANGED:
        this.setGroup(action.group);
        this.__emitChange();
        break;

      case ActionTypes.GROUP_EDIT_MODAL_HIDE:
        _isOpen = false;
        this.__emitChange();
        break;
    }
  }
}
export default new EditGroupStore(Dispatcher);
