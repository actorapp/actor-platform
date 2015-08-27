/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatchAsync } from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';
import ActorClient from 'utils/ActorClient';

export default {
  kickMember(gid, uid) {
    dispatchAsync(ActorClient.kickMember(gid, uid), {
      request: ActionTypes.KICK_USER,
      success: ActionTypes.KICK_USER_SUCCESS,
      failure: ActionTypes.KICK_USER_ERROR
    }, { gid, uid });
  }
};
