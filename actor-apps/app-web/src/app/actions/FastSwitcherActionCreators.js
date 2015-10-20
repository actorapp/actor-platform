/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatch, dispatchAsync } from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';
import ActorClient from 'utils/ActorClient';

export default {
  show() {
    dispatch(ActionTypes.FAST_SWITCHER_SHOW);
  },

  hide() {
    dispatch(ActionTypes.FAST_SWITCHER_HIDE);
  }
};
