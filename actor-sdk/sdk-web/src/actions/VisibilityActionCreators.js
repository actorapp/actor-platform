/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatch } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';
import ActorClient from '../utils/ActorClient';

export default {
  createAppVisible: () => {
    ActorClient.onAppVisible();
    dispatch(ActionTypes.APP_VISIBLE);
  },

  createAppHidden: () => {
    ActorClient.onAppHidden();
    dispatch(ActionTypes.APP_HIDDEN);
  }
};
