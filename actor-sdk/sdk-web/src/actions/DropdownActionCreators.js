/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatch } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';
import ComposeActionCreators from '../actions/ComposeActionCreators';

export default {
  openMessageActions(targetRect, message) {
    dispatch(ActionTypes.MESSAGE_DROPDOWN_SHOW, { targetRect, message });
    ComposeActionCreators.toggleAutoFocus(false);
  },

  openRecentContextMenu(contextPos, peer) {
    dispatch(ActionTypes.RECENT_CONTEXT_MENU_SHOW, { contextPos, peer });
    ComposeActionCreators.toggleAutoFocus(false);
  },

  hideMessageDropdown() {
    dispatch(ActionTypes.MESSAGE_DROPDOWN_HIDE);
    ComposeActionCreators.toggleAutoFocus(true);
  },

  hideRecentContext() {
    dispatch(ActionTypes.RECENT_CONTEXT_MENU_HIDE);
    ComposeActionCreators.toggleAutoFocus(true);
  }
}
