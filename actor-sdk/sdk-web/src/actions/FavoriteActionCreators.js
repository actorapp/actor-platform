/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { dispatchAsync } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';
import ActorClient from '../utils/ActorClient';

export default {
  favoriteChat(peer) {
    dispatchAsync(ActorClient.favoriteChat(peer), {
      request: ActionTypes.FAVORITE_ADD,
      success: ActionTypes.FAVORITE_ADD_SUCCESS,
      failure: ActionTypes.FAVORITE_ADD_ERROR
    }, { peer });
  },

  unfavoriteChat(peer) {
    dispatchAsync(ActorClient.unfavoriteChat(peer), {
      request: ActionTypes.FAVORITE_REMOVE,
      success: ActionTypes.FAVORITE_REMOVE_SUCCESS,
      failure: ActionTypes.FAVORITE_REMOVE_ERROR
    }, { peer });
  }
}
