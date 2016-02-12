/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { dispatchAsync } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';
import ActorClient from '../utils/ActorClient';
import history from '../utils/history';

import DialogActionCreators from './DialogActionCreators';

const urlBase = 'https://quit.email';

export default {
  joinGroupViaLink(token) {
    const url = urlBase + '/join/' + token;

    const joinViaLink = () => dispatchAsync(ActorClient.joinGroupViaLink(url), {
      request: ActionTypes.GROUP_JOIN_VIA_LINK,
      success: ActionTypes.GROUP_JOIN_VIA_LINK_SUCCESS,
      failure: ActionTypes.GROUP_JOIN_VIA_LINK_ERROR
    }, { token });

    const selectJoined = (peer) => {
      if (peer) {
        DialogActionCreators.selectDialogPeer(peer);
      } else {
        throw new Error();
      }
    };

    const goHome = () => history.replace('/');

    joinViaLink()
      .then(selectJoined)
      .catch(goHome)
  }
};
