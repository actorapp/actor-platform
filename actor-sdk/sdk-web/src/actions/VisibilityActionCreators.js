/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatch } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';
import ConnectionStateActionCreators from '../actions/ConnectionStateActionCreators';
import ActorClient from '../utils/ActorClient';

export default {
  createAppVisible() {
    ActorClient.onAppVisible();
    ActorClient.bindConnectState(ConnectionStateActionCreators.setState);
    dispatch(ActionTypes.APP_VISIBLE);
  },

  createAppHidden() {
    ActorClient.onAppHidden();
    ActorClient.unbindConnectState(ConnectionStateActionCreators.setState);
    dispatch(ActionTypes.APP_HIDDEN);
  }
};
