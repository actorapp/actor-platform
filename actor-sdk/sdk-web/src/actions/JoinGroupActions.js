/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { dispatch } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';
import ActorClient from '../utils/ActorClient';
import history from '../utils/history';
import JoinGroupStore from '../stores/JoinGroupStore';

function joinSuccess(peer) {
  dispatch(ActionTypes.GROUP_JOIN_VIA_LINK_SUCCESS);
  setTimeout(() => history.replace(`/im/${peer.key}`), 1000);
}

function joinFailed(error) {
  dispatch(ActionTypes.GROUP_JOIN_VIA_LINK_ERROR, { error });
}

export default {
  joinAfterLogin() {
    const { token } = JoinGroupStore.getState();
    if (token) {
      history.push(`/join/${token}`);
    }
  },
  joinGroupViaLink(token) {
    dispatch(ActionTypes.GROUP_JOIN_VIA_LINK, { token });
    ActorClient.joinGroupViaToken(token).then(joinSuccess, joinFailed);
  }
};
