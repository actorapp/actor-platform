/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatch, dispatchAsync } from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';
import ActorClient from 'utils/ActorClient';

export default {
  show() {
    dispatch(ActionTypes.MY_PROFILE_MODAL_SHOW);
  },

  hide() {
    dispatch(ActionTypes.MY_PROFILE_MODAL_HIDE);
  },

  onProfileChanged(profile) {
    dispatch(ActionTypes.MY_PROFILE_CHANGED, { profile });
  },

  // TODO: use dispatchAsync
  saveName(name) {
    dispatch(ActionTypes.MY_PROFILE_SAVE_NAME, { name });
  },
  // TODO: use dispatchAsync
  saveNickname(nick) {
    dispatch(ActionTypes.MY_PROFILE_SAVE_NICKNAME, { nick });
  },

  editMyAbout(about) {
    dispatchAsync(ActorClient.editMyAbout(about), {
      request: ActionTypes.MY_PROFILE_EDIT_ABOUT,
      success: ActionTypes.MY_PROFILE_EDIT_ABOUT_SUCCESS,
      failure: ActionTypes.MY_PROFILE_EDIT_ABOUT_ERROR
    }, { about });
  },

  changeMyAvatar(newAvatar) {
    ActorClient.changeMyAvatar(newAvatar);
  },

  removeMyAvatar() {
    ActorClient.removeMyAvatar();
  }
};
