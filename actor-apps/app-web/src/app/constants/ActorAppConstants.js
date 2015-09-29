/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import keymirror from 'keymirror';
import app from '../../app.json';

export const AuthSteps = {
  PHONE_WAIT: 1,
  CODE_WAIT: 2,
  SIGNUP_NAME_WAIT: 3,
  COMPLETED: 4
};

export const ActionTypes = keymirror({
  APP_HIDDEN: null,
  APP_VISIBLE: null,

  AUTH_SMS_REQUEST_SUCCESS: null,
  AUTH_SMS_REQUEST_FAILURE: null,

  SEND_CODE_SUCCESS: null,
  SEND_CODE_FAILURE: null,

  SEND_SIGNUP_SUCCESS: null,
  SEND_SIGNUP_FAILURE: null,

  SET_LOGGED_IN: null,
  SET_LOGGED_OUT: null,
  START_SIGNUP: null,

  AUTH_WRONG_NUMBER_CLICK: null,

  DIALOGS_CHANGED: null,
  SELECTED_DIALOG_INFO_CHANGED: null,
  MY_PROFILE_CHANGED: null,
  SELECT_DIALOG_PEER: null,

  COMPOSE_CLEAN: null,
  COMPOSE_TYPING: null,
  COMPOSE_MENTION_INSERT: null,
  COMPOSE_MENTION_CLOSE: null,
  COMPOSE_EMOJI_INSERT: null,

  SEND_MESSAGE_TEXT: null,
  SEND_MESSAGE_FILE: null,
  SEND_MESSAGE_PHOTO: null,

  ACTIVITY_SHOW: null,
  ACTIVITY_HIDE: null,

  CONTACT_ADD: null,
  CONTACT_REMOVE: null,
  CONTACT_LIST_SHOW: null,
  CONTACT_LIST_HIDE: null,
  CONTACT_LIST_CHANGED: null,
  CONTACT_ADD_MODAL_SHOW: null,
  CONTACT_ADD_MODAL_HIDE: null,
  CONTACT_ADD_MODAL_FIND_USER_OK: null,
  CONTACT_ADD_MODAL_FIND_USER_UNREGISTERED: null,
  CONTACT_ADD_MODAL_FIND_USER_IN_CONTACT: null,

  // Group actions
  GROUP_CREATE_MODAL_OPEN: null,
  GROUP_CREATE_MODAL_CLOSE: null,
  GROUP_CREATE: null,
  GROUP_CREATE_SUCCESS: null,
  GROUP_CREATE_ERROR: null,
  GROUP_EDIT_MODAL_SHOW: null,
  GROUP_EDIT_MODAL_HIDE: null,
  GROUP_EDIT_TITLE: null,
  GROUP_EDIT_TITLE_SUCCESS: null,
  GROUP_EDIT_TITLE_ERROR: null,
  GROUP_INFO_CHANGED: null,
  GROUP_EDIT_ABOUT: null,
  GROUP_EDIT_ABOUT_SUCCESS: null,
  GROUP_EDIT_ABOUT_ERROR: null,

  NOTIFICATION_CHANGE: null,

  DRAFT_LOAD: null,
  DRAFT_SAVE: null,

  APP_UPDATE_MODAL_SHOW: null,
  APP_UPDATE_MODAL_HIDE: null,
  APP_UPDATE_CONFIRM: null,

  GET_INTEGRATION_TOKEN: null,
  GET_INTEGRATION_TOKEN_SUCCESS: null,
  GET_INTEGRATION_TOKEN_ERROR: null,

  FAVICON_SET_DEFAULT: null,
  FAVICON_SET_NOTIFICATION: null,

  INVITE_USER_MODAL_SHOW: null,
  INVITE_USER_MODAL_HIDE: null,
  INVITE_USER_BY_LINK_MODAL_SHOW: null,
  INVITE_USER_BY_LINK_MODAL_HIDE: null,
  INVITE_USER: null,
  INVITE_USER_SUCCESS: null,
  INVITE_USER_ERROR: null,

  PREFERENCES_SAVE: null,
  PREFERENCES_MODAL_HIDE: null,
  PREFERENCES_MODAL_SHOW: null,
  PREFERENCES_CHANGE_TAB: null,
  PREFERENCES_SESSION_LOAD: null,
  PREFERENCES_SESSION_LOAD_SUCCESS: null,
  PREFERENCES_SESSION_LOAD_ERROR: null,
  PREFERENCES_SESSION_TERMINATE: null,
  PREFERENCES_SESSION_TERMINATE_SUCCESS: null,
  PREFERENCES_SESSION_TERMINATE_ERROR: null,
  PREFERENCES_SESSION_TERMINATE_ALL: null,
  PREFERENCES_SESSION_TERMINATE_ALL_SUCCESS: null,
  PREFERENCES_SESSION_TERMINATE_ALL_ERROR: null,

  MY_PROFILE_MODAL_SHOW: null,
  MY_PROFILE_MODAL_HIDE: null,
  MY_PROFILE_SAVE_NAME: null,
  MY_PROFILE_SAVE_NICKNAME: null,
  MY_PROFILE_EDIT_ABOUT: null,
  MY_PROFILE_EDIT_ABOUT_SUCCESS: null,
  MY_PROFILE_EDIT_ABOUT_ERROR: null,

  KICK_USER: null,
  KICK_USER_SUCCESS: null,
  KICK_USER_ERROR: null,

  CHAT_LEAVE: null,
  CHAT_LEAVE_SUCCESS: null,
  CHAT_LEAVE_ERROR: null,
  CHAT_DELETE: null,
  CHAT_DELETE_SUCCESS: null,
  CHAT_DELETE_ERROR: null,
  CHAT_CLEAR: null,
  CHAT_CLEAR_SUCCESS: null,
  CHAT_CLEAR_ERROR: null,

  CROP_AVATAR_MODAL_SHOW: null,
  CROP_AVATAR_MODAL_HIDE: null
});

export const PeerTypes = {
  USER: 'user',
  GROUP: 'group'
};

export const ActivityTypes = keymirror({
  USER_PROFILE: null,
  GROUP_PROFILE: null
});

export const MessageContentTypes = {
  SERVICE: 'service',
  TEXT: 'text',
  PHOTO: 'photo',
  DOCUMENT: 'document',
  UNSUPPORTED: 'unsupported'
};

export const KeyCodes = {
  TAB: 9,
  ESC: 27,
  ENTER: 13,
  ARROW_UP: 38,
  ARROW_DOWN: 40
};

export const AsyncActionStates = {
  PENDING: 0,
  PROCESSING: 1,
  SUCCESS: 2,
  FAILURE: 3
};

export const version = app.base_version;

export const Mixpanel = app.mixpanel;

export const Support = {
  id: 576465533,
  phone: '+75551234567'
};

export const Path = {
  toImages: 'assets/img',
  toEmoji: 'assets/img/emoji'
};

export const endpoints = app.endpoints;

export const AddContactMessages = {
  PHONE_NOT_REGISTERED: 1,
  ALREADY_HAVE: 2
};

export default {
  AuthSteps: AuthSteps,

  PeerTypes: PeerTypes,

  ActionTypes: ActionTypes,

  ActivityTypes: ActivityTypes,

  MessageContentTypes: MessageContentTypes,

  KeyCodes: KeyCodes,

  ChangeState: AsyncActionStates,

  Mixpanel: Mixpanel,

  version: version,

  endpoints: endpoints,

  Support: Support,

  Path: Path,

  AddContactMessages
};
