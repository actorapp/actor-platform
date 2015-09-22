/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatch } from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';

export default {
  show() {
    dispatch(ActionTypes.ACTIVITY_SHOW);
  },

  hide() {
    dispatch(ActionTypes.ACTIVITY_HIDE);
  }
};
