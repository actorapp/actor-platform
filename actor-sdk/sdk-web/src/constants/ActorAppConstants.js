/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import keymirror from 'keymirror';

export const AuthSteps = keymirror({
  LOGIN_WAIT: null,
  CODE_WAIT: null,
  NAME_WAIT: null,
  COMPLETED: null
});

export const ActionTypes = keymirror({
  APP_HIDDEN: null,
  APP_VISIBLE: null,

  AUTH_CHANGE_LOGIN: null,
  AUTH_CHANGE_CODE: null,
  AUTH_CHANGE_NAME: null,
  AUTH_RESTART: null,
  AUTH_CODE_REQUEST: null,
  AUTH_CODE_REQUEST_SUCCESS: null,
  AUTH_CODE_REQUEST_FAILURE: null,
  AUTH_CODE_SEND: null,
  AUTH_CODE_SEND_SUCCESS: null,
  AUTH_CODE_SEND_FAILURE: null,
  AUTH_SIGNUP_START: null,
  AUTH_SIGNUP: null,
  AUTH_SIGNUP_SUCCESS: null,
  AUTH_SIGNUP_FAILURE: null,
  AUTH_SET_LOGGED_IN: null,
  AUTH_SET_LOGGED_OUT: null,

  DIALOGS_CHANGED: null,
  DIALOG_INFO_CHANGED: null,
  TYPING_CHANGED: null,
  MY_PROFILE_CHANGED: null,
  SELECT_DIALOG_PEER: null,
  MESSAGES_CHANGED: null,
  CONNECTION_STATE_CHANGED: null,

  COMPOSE_CLEAN: null,
  COMPOSE_TYPING: null,
  COMPOSE_MENTION_INSERT: null,
  COMPOSE_MENTION_CLOSE: null,
  COMPOSE_PASTE: null,

  EMOJI_INSERT: null,
  EMOJI_SHOW: null,
  EMOJI_CLOSE: null,

  MESSAGE_SEND_TEXT: null,
  MESSAGE_SEND_FILE: null,
  MESSAGE_SEND_PHOTO: null,
  MESSAGE_DELETE: null,
  MESSAGES_SET_SELECTED: null,

  ACTIVITY_SHOW: null,
  ACTIVITY_HIDE: null,

  // Contact actions
  CONTACT_ADD: null,
  CONTACT_REMOVE: null,
  CONTACT_LIST_SHOW: null,
  CONTACT_LIST_HIDE: null,
  CONTACT_LIST_CHANGED: null,
  CONTACT_ADD_MODAL_SHOW: null,
  CONTACT_ADD_MODAL_HIDE: null,
  CONTACT_FIND: null,
  CONTACT_FIND_SUCCESS: null,
  CONTACT_FIND_ERROR: null,
  CONTACT_LIST_SEARCH: null,

  // Group actions
  GROUP_CREATE_MODAL_OPEN: null,
  GROUP_CREATE_MODAL_CLOSE: null,
  GROUP_CREATE_SET_NAME: null,
  //GROUP_CREATE_SET_AVATAR: null,
  GROUP_CREATE_SET_MEMBERS: null,
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
  GROUP_GET_TOKEN: null,
  GROUP_GET_TOKEN_SUCCESS: null,
  GROUP_GET_TOKEN_ERROR: null,
  GROUP_LEAVE: null,
  GROUP_LEAVE_SUCCESS: null,
  GROUP_LEAVE_ERROR: null,
  GROUP_DELETE: null,
  GROUP_DELETE_SUCCESS: null,
  GROUP_DELETE_ERROR: null,
  GROUP_CLEAR: null,
  GROUP_CLEAR_SUCCESS: null,
  GROUP_CLEAR_ERROR: null,
  GROUP_HIDE: null,
  GROUP_HIDE_SUCCESS: null,
  GROUP_HIDE_ERROR: null,
  GROUP_JOIN_VIA_LINK: null,
  GROUP_JOIN_VIA_LINK_SUCCESS: null,
  GROUP_JOIN_VIA_LINK_ERROR: null,

  GROUP_LIST_SHOW: null,
  GROUP_LIST_HIDE: null,
  GROUP_LIST_LOAD: null,
  GROUP_LIST_LOAD_SUCCESS: null,
  GROUP_LIST_LOAD_ERROR: null,
  GROUP_LIST_SEARCH: null,

  NOTIFICATION_CHANGE: null,

  DRAFT_LOAD: null,
  DRAFT_SAVE: null,

  APP_UPDATE_MODAL_SHOW: null,
  APP_UPDATE_MODAL_HIDE: null,
  APP_UPDATE_CONFIRM: null,

  FAVICON_SET: null,

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

  CROP_AVATAR_MODAL_SHOW: null,
  CROP_AVATAR_MODAL_HIDE: null,

  QUICK_SEARCH_SHOW: null,
  QUICK_SEARCH_HIDE: null,
  QUICK_SEARCH_CHANGED: null,
  QUICK_SEARCH: null,

  ATTACHMENT_MODAL_SHOW: null,
  ATTACHMENT_MODAL_HIDE: null,
  ATTACHMENT_SELECT: null,
  ATTACHMENT_CHANGE: null,
  ATTACHMENT_DELETE: null,
  ATTACHMENT_SEND: null,
  ATTACHMENT_SEND_ALL: null,

  GROUP_ONLINE_CHANGE: null,
  USER_ONLINE_CHANGE: null,

  DROPDOWN_SHOW: null,
  DROPDOWN_HIDE: null
});

export const PeerTypes = {
  USER: 'user',
  GROUP: 'group'
};

export const ActivityTypes = keymirror({
  USER_PROFILE: null,
  GROUP_PROFILE: null
});

export const CreateGroupSteps = keymirror({
  NAME_INPUT: null,
  CONTACTS_SELECTION: null,
  CREATION_STARTED: null
});

export const MessageContentTypes = {
  SERVICE: 'service',
  TEXT: 'text',
  TEXT_MODERN: 'text_modern',
  PHOTO: 'photo',
  DOCUMENT: 'document',
  VOICE: 'voice',
  CONTACT: 'contact',
  LOCATION: 'location',
  UNSUPPORTED: 'unsupported',
  STICKER: 'sticker'
};

export const KeyCodes = {
  TAB: 9,
  ESC: 27,
  ENTER: 13,
  ARROW_UP: 38,
  ARROW_DOWN: 40,

  K: 75,
  G: 71,
  P: 80
};

export const AsyncActionStates = {
  PENDING: 0,
  PROCESSING: 1,
  SUCCESS: 2,
  FAILURE: 3
};

export const Support = {
  id: 576465533,
  phone: '+75551234567'
};

export const Path = {
  toImages: 'assets/images',
  toEmoji: 'assets/images/emoji'
};

export const endpoints = [
  'wss://front1-ws-mtproto-api-rev2.actor.im',
  'wss://front2-ws-mtproto-api-rev2.actor.im'
];

export default {
  AuthSteps,
  ActionTypes,
  PeerTypes,
  ActivityTypes,
  CreateGroupSteps,
  MessageContentTypes,
  KeyCodes,
  AsyncActionStates,
  Support,
  Path,
  endpoints
};
