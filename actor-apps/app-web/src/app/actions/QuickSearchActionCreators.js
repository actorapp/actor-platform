/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatch, dispatchAsync } from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';
import ActorClient from 'utils/ActorClient';

export default {
  show() {
    dispatch(ActionTypes.QUICK_SEARCH_SHOW);
  },

  hide() {
    dispatch(ActionTypes.QUICK_SEARCH_HIDE);
  },

  search(query) {
    dispatch(ActionTypes.QUICK_SEARCH, { query });
  }
};
