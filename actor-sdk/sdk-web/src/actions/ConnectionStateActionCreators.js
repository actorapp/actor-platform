/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatch } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

const ConnectionStateActionCreators = {
  setState(state) {
    dispatch(ActionTypes.CONNECTION_STATE_CHANGED, { state });
  }
};

export default ConnectionStateActionCreators;
