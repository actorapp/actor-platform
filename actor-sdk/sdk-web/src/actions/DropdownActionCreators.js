/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatch } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';
import ComposeActionCreators from '../actions/ComposeActionCreators';

export default {
  openMessageActions(targetRect, message) {
    dispatch(ActionTypes.DROPDOWN_SHOW, { targetRect, message });
    ComposeActionCreators.toggleAutoFocus(false);
  },

  hide() {
    dispatch(ActionTypes.DROPDOWN_HIDE);
    ComposeActionCreators.toggleAutoFocus(true);
  }
}
