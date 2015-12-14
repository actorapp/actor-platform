/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatch } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

const DialogInfoActionCreators = {
  changeNotificationsEnabled(peer, isEnabled) {
    ActorClient.changeNotificationsEnabled(peer, isEnabled);
    dispatch(ActionTypes.NOTIFICATION_CHANGE, {peer, isEnabled});
  }
};

export default DialogInfoActionCreators;
