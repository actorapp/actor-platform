/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatch } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

export default {
  openMessageActions(targetRect, message) {
    dispatch(ActionTypes.DROPDOWN_SHOW, { targetRect, message })
  },

  hide() {
    dispatch(ActionTypes.DROPDOWN_HIDE)
  }
}
