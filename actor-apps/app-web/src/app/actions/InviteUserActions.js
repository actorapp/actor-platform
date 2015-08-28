/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatch, dispatchAsync } from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';
import ActorClient from 'utils/ActorClient';

export default {
  show: (group) => {
    dispatch(ActionTypes.INVITE_USER_MODAL_SHOW, {
      group
    });
  },

  hide: () => {
    dispatch(ActionTypes.INVITE_USER_MODAL_HIDE);
  },

  inviteUser: (gid, uid) => {
    dispatchAsync(ActorClient.inviteMember(gid, uid), {
      request: ActionTypes.INVITE_USER,
      success: ActionTypes.INVITE_USER_SUCCESS,
      failure: ActionTypes.INVITE_USER_ERROR
    }, { gid, uid });
  }
};
