/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatch } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';
import ComposeActionCreators from '../actions/ComposeActionCreators';

export default {
  open() {
    dispatch(ActionTypes.EMOJI_SHOW);
    ComposeActionCreators.toggleAutoFocus(false);
  },

  close() {
    dispatch(ActionTypes.EMOJI_CLOSE);
    ComposeActionCreators.toggleAutoFocus(true);
  },

  insertEmoji(text, caretPosition, emoji) {
    dispatch(ActionTypes.EMOJI_INSERT, { text, caretPosition, emoji });
  }
};
