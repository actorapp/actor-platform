/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import Immutable from 'immutable';
import keymirror from 'keymirror';

import { Store } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes, CreateGroupSteps } from '../constants/ActorAppConstants';

let _modalOpen = false,
    _currentStep = CreateGroupSteps.NAME_INPUT,
    _groupName = '',
    _selectedUserIds = new Immutable.Set();

class CreateGroupStore extends Store {
  constructor(Dispatcher) {
    super(Dispatcher);
  }

  isModalOpen() {
    return _modalOpen;
  }

  getCurrentStep() {
    return _currentStep;
  }

  getGroupName() {
    return _groupName;
  }

  getSelectedUserIds() {
    return _selectedUserIds;
  }

  resetStore() {
    _modalOpen = false;
    _currentStep = CreateGroupSteps.NAME_INPUT;
    _groupName = '';
    _selectedUserIds = new Immutable.Set();
  }

  __onDispatch = (action) => {
    switch (action.type) {

      case ActionTypes.GROUP_CREATE_MODAL_OPEN:
        _modalOpen = true;
        this.__emitChange();
        break;
      case ActionTypes.GROUP_CREATE_MODAL_CLOSE:
        this.resetStore();
        this.__emitChange();
        break;

      case ActionTypes.GROUP_CREATE_SET_NAME:
        _currentStep = CreateGroupSteps.CONTACTS_SELECTION;
        _groupName = action.name;
        this.__emitChange();
        break;

      //case ActionTypes.GROUP_CREATE_SET_AVATAR:
      //  _avatar = action.avatar;
      //  this.__emitChange();
      //  break;

      case ActionTypes.GROUP_CREATE_SET_MEMBERS:
        _selectedUserIds = action.selectedUserIds;
        this.__emitChange();
        break;

      case ActionTypes.GROUP_CREATE:
        _currentStep = CreateGroupSteps.CREATION_STARTED;
        this.__emitChange();
        break;
      case ActionTypes.GROUP_CREATE_SUCCESS:
        this.resetStore();
        this.__emitChange();
        break;
      case ActionTypes.GROUP_CREATE_ERROR:
        console.error('Failed to create group', action.error);
        this.__emitChange();
        break;
    }
  };
}

export default new CreateGroupStore(Dispatcher);
