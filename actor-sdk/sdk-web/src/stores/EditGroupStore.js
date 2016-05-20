/*
 * Copyright (C) 2016 Actor LLC. <https://actor.im>
 */

import { ReduceStore } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';
import ActorClient from '../utils/ActorClient';

class EditGroupStore extends ReduceStore {
  getInitialState() {
    return {
      group: null
    };
  }

  reduce(state, action) {
    switch (action.type) {
      case ActionTypes.GROUP_EDIT_MODAL_SHOW:
      case ActionTypes.GROUP_INFO_CHANGED:
        return {
          ...state,
          group: action.group
        }

      case ActionTypes.GROUP_EDIT_MODAL_HIDE:
        return this.getInitialState();

      default:
        return state;
    }
  }

  getGroup() {
    return this.getState().group;
  }

  getAbout() {
    return this.getState().group.about;
  }

  getTitle() {
    return this.getState().group.name;
  }

  isAdmin() {
    const myID = ActorClient.getUid();
    return this.getState().group.adminId === myID;
  }

}
export default new EditGroupStore(Dispatcher);
