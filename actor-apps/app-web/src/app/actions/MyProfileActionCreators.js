/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import ActorAppDispatcher from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';

export default {
  show() {
    dispatch(ActionTypes.MY_PROFILE_MODAL_SHOW);
  },

  hide() {
    dispatch(ActionTypes.MY_PROFILE_MODAL_HIDE);
  },

  saveName(name) {
    dispatch(ActionTypes.MY_PROFILE_SAVE_NAME, {
      name
    });
  },

  saveNickname(nick) {
    dispatch(ActionTypes.MY_PROFILE_SAVE_NICKNAME, {
      nick
    });
  }
};
