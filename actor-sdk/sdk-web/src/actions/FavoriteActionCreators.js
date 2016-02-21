/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatch, dispatchAsync } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';
import ActorClient from '../utils/ActorClient';

export default {
  favoriteChat(peer) {
    dispatchAsync(ActorClient.favoriteChat(peer), {
      request: ActionTypes.GROUP_FAVORITE,
      success: ActionTypes.GROUP_FAVORITE_SUCCESS,
      failure: ActionTypes.GROUP_FAVORITE_ERROR
    }, { peer });
  },

  unfavoriteChat(peer) {
    dispatchAsync(ActorClient.unfavoriteChat(peer), {
      request: ActionTypes.GROUP_UNFAVORITE,
      success: ActionTypes.GROUP_UNFAVORITE_SUCCESS,
      failure: ActionTypes.GROUP_UNFAVORITE_ERROR
    }, { peer });
  }
}
