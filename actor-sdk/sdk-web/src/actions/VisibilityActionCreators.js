/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatch } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';
import ConnectionStateActionCreators from '../actions/ConnectionStateActionCreators';
import DraftActionCreators from '../actions/DraftActionCreators';
import ActorClient from '../utils/ActorClient';
import DialogStore from '../stores/DialogStore';

export default {
  createAppVisible() {
    dispatch(ActionTypes.APP_VISIBLE);
    ActorClient.onAppVisible();
    ActorClient.bindConnectState(ConnectionStateActionCreators.setState);
  },

  createAppHidden() {
    dispatch(ActionTypes.APP_HIDDEN);
    const currentPeer = DialogStore.getCurrentPeer();
    ActorClient.onAppHidden();
    ActorClient.unbindConnectState(ConnectionStateActionCreators.setState);
    DraftActionCreators.saveDraft(currentPeer);
  }
};
