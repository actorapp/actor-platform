/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatch } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

const OnlineActionCreators = {
  setUserOnline({ ...args }) {
    dispatch(ActionTypes.USER_ONLINE_CHANGE, { ...args });
  },

  setGroupOnline({ ...args }) {
    dispatch(ActionTypes.GROUP_ONLINE_CHANGE, { ...args });
  }
};

export default OnlineActionCreators;
