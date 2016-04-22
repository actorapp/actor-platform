/*
 * Copyright (C) 2016 Actor LLC. <https://actor.im>
 */

import { dispatch, dispatchAsync } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';
import ActorClient from '../utils/ActorClient';
import ComposeActionCreators from '../actions/ComposeActionCreators';

export default {
  show() {
    dispatch(ActionTypes.PROFILE_SHOW);
    ComposeActionCreators.toggleAutoFocus(false);
  },

  hide() {
    dispatch(ActionTypes.PROFILE_HIDE);
    ComposeActionCreators.toggleAutoFocus(true);
  },

  setProfile(profile) {
    dispatch(ActionTypes.PROFILE_CHANGED, { profile });
  },

  editMyName(name) {
    dispatchAsync(ActorClient.editMyName(name), {
      request: ActionTypes.PROFILE_EDIT_NAME,
      success: ActionTypes.PROFILE_EDIT_NAME_SUCCESS,
      failure: ActionTypes.PROFILE_EDIT_NAME_ERROR
    }, { name });
  },

  editMyNick(nick) {
    dispatchAsync(ActorClient.editMyNick(nick), {
      request: ActionTypes.PROFILE_EDIT_NICK,
      success: ActionTypes.PROFILE_EDIT_NICK_SUCCESS,
      failure: ActionTypes.PROFILE_EDIT_NICK_ERROR
    }, { nick });
  },

  editMyAbout(about) {
    dispatchAsync(ActorClient.editMyAbout(about), {
      request: ActionTypes.PROFILE_EDIT_ABOUT,
      success: ActionTypes.PROFILE_EDIT_ABOUT_SUCCESS,
      failure: ActionTypes.PROFILE_EDIT_ABOUT_ERROR
    }, { about });
  },

  changeMyAvatar(newAvatar) {
    ActorClient.changeMyAvatar(newAvatar);
  },

  removeMyAvatar() {
    ActorClient.removeMyAvatar();
  }
};
