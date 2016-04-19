/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatch } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';
import ActorClient from '../utils/ActorClient';

const DialogInfoActionCreators = {
  changeNotificationsEnabled(peer, isEnabled) {
    ActorClient.changeNotificationsEnabled(peer, isEnabled);
    dispatch(ActionTypes.NOTIFICATION_CHANGE, { peer, isEnabled });
  }
};

export default DialogInfoActionCreators;
