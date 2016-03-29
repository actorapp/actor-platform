/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatch } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';
import ActionCreators from './ActionCreators';
import ConnectionStateActionCreators from '../actions/ConnectionStateActionCreators';
import DraftActionCreators from '../actions/DraftActionCreators';
import ActorClient from '../utils/ActorClient';
import DialogStore from '../stores/DialogStore';

class VisibilityActionCreators extends ActionCreators {
  createAppVisible() {
    dispatch(ActionTypes.APP_VISIBLE);
    ActorClient.onAppVisible();
    this.setBindings('connect', [
      ActorClient.bindConnectState(ConnectionStateActionCreators.setState)
    ]);
  }

  createAppHidden() {
    dispatch(ActionTypes.APP_HIDDEN);

    const currentPeer = DialogStore.getCurrentPeer();
    DraftActionCreators.saveDraft(currentPeer);

    ActorClient.onAppHidden();
    this.removeBindings('connect');
  }
}

export default new VisibilityActionCreators();
