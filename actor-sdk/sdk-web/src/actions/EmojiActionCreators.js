/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatch, dispatchAsync } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';
import ActorClient from '../utils/ActorClient';

export default {
  open() {
    dispatch(ActionTypes.EMOJI_SHOW);
  },

  close()  {
    dispatch(ActionTypes.EMOJI_CLOSE)
  },

  insertEmoji(text, caretPosition, emoji) {
    dispatch(ActionTypes.EMOJI_INSERT, { text, caretPosition, emoji });
  }
};
