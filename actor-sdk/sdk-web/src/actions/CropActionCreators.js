/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatch } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';
import ComposeActionCreators from '../actions/ComposeActionCreators';

export default {
  show(source, prevModal, callback) {
    dispatch(ActionTypes.CROP_MODAL_SHOW, { source, prevModal, callback });
    ComposeActionCreators.toggleAutoFocus(false);
  },

  hide() {
    dispatch(ActionTypes.CROP_MODAL_HIDE);
  }
}
