/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import Immutable from 'immutable';

import { ReduceStore } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes, CreateGroupSteps } from '../constants/ActorAppConstants';

class CreateGroupStore extends ReduceStore {
  getInitialState() {
    return {
      step: CreateGroupSteps.NAME_INPUT,
      name: null,
      selectedUserIds: new Immutable.Set()
    };
  }

  reduce(state, action) {
    switch (action.type) {
      case ActionTypes.GROUP_CREATE_SET_NAME:
        return {
          ...state,
          step: CreateGroupSteps.CONTACTS_SELECTION,
          name: action.name
        }

      case ActionTypes.GROUP_CREATE_SET_MEMBERS:
        return {
          ...state,
          selectedUserIds: action.selectedUserIds
        }

      case ActionTypes.GROUP_CREATE:
        return {
          ...state,
          step: CreateGroupSteps.CREATION_STARTED
        }

      // TODO: Show create group error success messages in modal
      case ActionTypes.GROUP_CREATE_SUCCESS:
        return this.getInitialState();

      case ActionTypes.GROUP_CREATE_ERROR:
        console.error('Failed to create group', action.error);
        return state;

      case ActionTypes.GROUP_CREATE_MODAL_HIDE:
        return this.getInitialState();


      default:
        return state;
    }
  }


    getCurrentStep() {
      return this.getState().step;
    }

    getGroupName() {
      return this.getState().name;
    }

    getSelectedUserIds() {
      return this.getState().selectedUserIds;
    }
}

export default new CreateGroupStore(Dispatcher);
