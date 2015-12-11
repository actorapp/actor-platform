/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatch, dispatchAsync } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';
import ActorClient from '../utils/ActorClient';

const GroupListActionCreators = {
  open() {
    dispatch(ActionTypes.GROUP_LIST_SHOW);
    this.loadGroups();
  },

  close() {
    dispatch(ActionTypes.GROUP_LIST_HIDE)
  },

  search(query) {
    dispatch(ActionTypes.GROUP_LIST_SEARCH, { query })
  },

  loadGroups() {
    dispatchAsync(ActorClient.findGroups(), {
      request: ActionTypes.GROUP_LIST_LOAD,
      success: ActionTypes.GROUP_LIST_LOAD_SUCCESS,
      failure: ActionTypes.GROUP_LIST_LOAD_ERROR
    });
  }
};

export default GroupListActionCreators;
