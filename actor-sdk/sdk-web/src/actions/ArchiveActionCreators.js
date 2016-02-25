/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatchAsync } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';
import ActorClient from '../utils/ActorClient';

export default {
  archiveChat(peer) {
    dispatchAsync(ActorClient.archiveChat(peer), {
      request: ActionTypes.ARCHIVE_ADD,
      success: ActionTypes.ARCHIVE_ADD_SUCCESS,
      failure: ActionTypes.ARCHIVE_ADD_ERROR
    }, { peer });
  },

  loadArchivedDialogs() {
    dispatchAsync(ActorClient.loadArchivedDialogs(), {
      request: ActionTypes.ARCHIVE_LOAD,
      success: ActionTypes.ARCHIVE_LOAD_SUCCESS,
      failure: ActionTypes.ARCHIVE_LOAD_ERROR
    });
  },

  loadMoreArchivedDialogs() {
    dispatchAsync(ActorClient.loadMoreArchivedDialogs(), {
      request: ActionTypes.ARCHIVE_LOAD_MORE,
      success: ActionTypes.ARCHIVE_LOAD_MORE_SUCCESS,
      failure: ActionTypes.ARCHIVE_LOAD_MORE_ERROR
    });
  }
};
