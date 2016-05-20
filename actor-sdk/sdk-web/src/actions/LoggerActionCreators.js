/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatch } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

export function loggerAppend(tag, type, message) {
  dispatch(ActionTypes.LOGGER_APPEND, {
    payload: { type, tag, message }
  });
}

export function loggerToggle() {
  dispatch(ActionTypes.LOGGER_TOGGLE);
}
