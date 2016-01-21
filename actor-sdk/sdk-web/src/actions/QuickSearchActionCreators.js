/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatch, dispatchAsync } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';
import ActorClient from '../utils/ActorClient';
import ComposeActionCreators from '../actions/ComposeActionCreators';

export default {
  show() {
    dispatch(ActionTypes.QUICK_SEARCH_SHOW);
    ComposeActionCreators.toggleAutoFocus(false);
  },

  hide() {
    dispatch(ActionTypes.QUICK_SEARCH_HIDE);
    ComposeActionCreators.toggleAutoFocus(true);
  },

  setQuickSearchList(list) {
    dispatch(ActionTypes.QUICK_SEARCH_CHANGED, { list });
  },

  search(query) {
    dispatch(ActionTypes.QUICK_SEARCH, { query });
  }
};
