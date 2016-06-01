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

  AUTH_START: null,
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

  BIND_DIALOG_PEER: null,
  UNBIND_DIALOG_PEER: null,

  DIALOGS_CHANGED: null,
  DIALOG_INFO_CHANGED: null,
  TYPING_CHANGED: null,
  MESSAGES_CHANGED: null,
  CONNECTION_STATE_CHANGED: null,

  COMPOSE_CLEAN: null,
  COMPOSE_TYPING: null,
  COMPOSE_MENTION_INSERT: null,
  COMPOSE_MENTION_CLOSE: null,
  COMPOSE_PASTE: null,
  COMPOSE_TOGGLE_AUTO_FOCUS: null,

  EMOJI_INSERT: null,
  EMOJI_SHOW: null,
  EMOJI_CLOSE: null,

  STICKERS_SET: null,

  MESSAGE_SEND_TEXT: null,
  MESSAGE_SEND_FILE: null,
  MESSAGE_SEND_PHOTO: null,
  MESSAGE_SEND_VOICE: null,
  MESSAGE_DELETE: null,
  MESSAGE_LIKE_ADD: null,
  MESSAGE_LIKE_REMOVE: null,
  MESSAGES_SET_SELECTED: null,
  MESSAGES_TOGGLE_SELECTED: null,
  MESSAGES_LOAD_MORE: null,
  MESSAGES_EDIT_START: null,
  MESSAGES_EDIT_END: null,

  ACTIVITY_SHOW: null,
  ACTIVITY_HIDE: null,

  // Contact actions
  CONTACT_LIST_SHOW: null,
  CONTACT_LIST_HIDE: null,
  CONTACT_LIST_CHANGED: null,
  CONTACT_ADD_MODAL_SHOW: null,
  CONTACT_ADD_MODAL_HIDE: null,
  CONTACT_ADD: null,
  CONTACT_ADD_SUCCESS: null,
  CONTACT_ADD_ERROR: null,
  CONTACT_REMOVE: null,
  CONTACT_REMOVE_SUCCESS: null,
  CONTACT_REMOVE_ERROR: null,
  CONTACT_FIND: null,
  CONTACT_FIND_SUCCESS: null,
  CONTACT_FIND_ERROR: null,

  // Group actions
  GROUP_CREATE_MODAL_SHOW: null,
  GROUP_CREATE_MODAL_HIDE: null,
  GROUP_CREATE_SET_NAME: null,
  GROUP_CREATE_SET_MEMBERS: null,
  GROUP_CREATE: null,
  GROUP_CREATE_SUCCESS: null,
  GROUP_CREATE_ERROR: null,
  GROUP_EDIT_MODAL_SHOW: null,
  GROUP_EDIT_MODAL_HIDE: null,
  GROUP_EDIT_TITLE: null,
  GROUP_EDIT_TITLE_SUCCESS: null,
  GROUP_EDIT_TITLE_ERROR: null,
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
  GROUP_INFO_CHANGED: null,

  GROUP_LIST_SHOW: null,
  GROUP_LIST_HIDE: null,
  GROUP_LIST_LOAD: null,
  GROUP_LIST_LOAD_SUCCESS: null,
  GROUP_LIST_LOAD_ERROR: null,

  NOTIFICATION_CHANGE: null,

  DRAFT_LOAD: null,
  DRAFT_SAVE: null,
  DRAFT_CHANGE: null,

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

  PREFERENCES_MODAL_SHOW: null,
  PREFERENCES_MODAL_HIDE: null,
  PREFERENCES_SAVE: null,
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

  // Profile

  PROFILE_SHOW: null,
  PROFILE_HIDE: null,
  PROFILE_CHANGED: null,
  PROFILE_EDIT_NAME: null,
  PROFILE_EDIT_NAME_SUCCESS: null,
  PROFILE_EDIT_NAME_ERROR: null,
  PROFILE_EDIT_NICK: null,
  PROFILE_EDIT_NICK_SUCCESS: null,
  PROFILE_EDIT_NICK_ERROR: null,
  PROFILE_EDIT_ABOUT: null,
  PROFILE_EDIT_ABOUT_SUCCESS: null,
  PROFILE_EDIT_ABOUT_ERROR: null,

  CROP_MODAL_SHOW: null,
  CROP_MODAL_HIDE: null,

  KICK_USER: null,
  KICK_USER_SUCCESS: null,
  KICK_USER_ERROR: null,

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

  MESSAGE_DROPDOWN_SHOW: null,
  MESSAGE_DROPDOWN_HIDE: null,

  CALL_MODAL_OPEN: null,
  CALL_MODAL_HIDE: null,
  CALL: null,
  CALL_SUCCESS: null,
  CALL_ERROR: null,
  CALL_CHANGED: null,
  CALL_TIME_CHANGED: null,
  CALL_ANSWER: null,
  CALL_END: null,
  CALL_MUTE_TOGGLE: null,
  CALL_FLOAT_TOGGLE: null,

  ARCHIVE_ADD: null,
  ARCHIVE_ADD_SUCCESS: null,
  ARCHIVE_ADD_ERROR: null,
  ARCHIVE_LOAD: null,
  ARCHIVE_LOAD_SUCCESS: null,
  ARCHIVE_LOAD_ERROR: null,
  ARCHIVE_LOAD_MORE: null,
  ARCHIVE_LOAD_MORE_SUCCESS: null,
  ARCHIVE_LOAD_MORE_ERROR: null,

  FAVORITE_ADD: null,
  FAVORITE_ADD_SUCCESS: null,
  FAVORITE_ADD_ERROR: null,
  FAVORITE_REMOVE: null,
  FAVORITE_REMOVE_SUCCESS: null,
  FAVORITE_REMOVE_ERROR: null,

  RECENT_CONTEXT_MENU_SHOW: null,
  RECENT_CONTEXT_MENU_HIDE: null,

  LOGGER_APPEND: null,
  LOGGER_TOGGLE: null,

  SEARCH_BLUR: null,
  SEARCH_CLEAR: null,
  SEARCH_FOCUS: null,
  SEARCH_SET_QUERY: null,
  SEARCH_SET_RESULTS: null,

  SEARCH_MESSAGES_SHOW: null,
  SEARCH_MESSAGES_HIDE: null,
  SEARCH_MESSAGES_CLEAR: null,
  SEARCH_MESSAGES_SET_QUERY: null,

  DIALOG_SEARCH_SHOW: null,
  DIALOG_SEARCH_HIDE: null,
  DIALOG_SEARCH_CHANGE_QUERY: null,

  DIALOG_SEARCH_TEXT: null,
  DIALOG_SEARCH_TEXT_SUCCESS: null,
  DIALOG_SEARCH_TEXT_ERROR: null,
  DIALOG_SEARCH_DOCS: null,
  DIALOG_SEARCH_DOCS_SUCCESS: null,
  DIALOG_SEARCH_DOCS_ERROR: null,
  DIALOG_SEARCH_LINKS: null,
  DIALOG_SEARCH_LINKS_SUCCESS: null,
  DIALOG_SEARCH_LINKS_ERROR: null,
  DIALOG_SEARCH_PHOTO: null,
  DIALOG_SEARCH_PHOTO_SUCCESS: null,
  DIALOG_SEARCH_PHOTO_ERROR: null,


  // SEARCH_SHOW: null,
  // SEARCH_HIDE: null,
  SEARCH_TOGGLE_FOCUS: null,
  SEARCH_TOGGLE_EXPAND: null,
  SEARCH_TEXT: null,
  SEARCH_TEXT_SUCCESS: null,
  SEARCH_TEXT_ERROR: null,
  SEARCH_DOCS: null,
  SEARCH_DOCS_SUCCESS: null,
  SEARCH_DOCS_ERROR: null,
  SEARCH_LINKS: null,
  SEARCH_LINKS_SUCCESS: null,
  SEARCH_LINKS_ERROR: null,
  SEARCH_PHOTO: null,
  SEARCH_PHOTO_SUCCESS: null,
  SEARCH_PHOTO_ERROR: null,

  MESSAGE_ART_SHOW: null,
  MESSAGE_ART_CLOSE: null,

  BLOCKED_USERS_LOAD: null,
  BLOCKED_USERS_SET: null,
  BLOCKED_USERS_LOAD_FAILED: null,
  BLOCKED_USERS_SET_QUERY: null
});

export const EventTypes = {
  CALLS: 'calls'
};

export const CallTypes = {
  STARTED: 'started',
  ENDED: 'ended'
};

export const CallStates = {
  CALLING: 'calling',
  CONNECTING: 'connecting',
  IN_PROGRESS: 'in_progress',
  ENDED: 'ended'
};

export const MessageArtPopupState = {
  EMOJI: 'emoji',
  STICKER: 'sticker'
};

export const ConnectionStates = {
  ONLINE: 'online',
  CONNECTING: 'connecting',
  UPDATING: 'updating'
};

export const MessageStates = {
  PENDING: 'pending',
  SENT: 'sent',
  RECEIVED: 'received',
  READ: 'read',
  ERROR: 'error',
  UNKNOWN: 'unknown'
};

export const MessageChangeReason = {
  UNKNOWN: 'UNKNOWN',
  PUSH: 'PUSH',
  UNSHIFT: 'UNSHIFT',
  UPDATE: 'UPDATE'
};

export const PeerTypes = {
  USER: 'user',
  GROUP: 'group'
};

export const PeerTypePrefixes = {
  USER: 'u',
  GROUP: 'g'
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
  ANIMATION: 'animation',
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

export const LoggerTypes = {
  INFO: 'i',
  ERROR: 'e',
  WARNING: 'w',
  DEBUG: 'd'
};

export const ModalTypes = keymirror({
  PROFILE: null,
  CROP: null,
  GROUP_LIST: null,
  PEOPLE_LIST: null,
  ADD_CONTACT: null,
  CREATE_GROUP: null,
  EDIT_GROUP: null,
  PREFERENCES: null,
  INVITE: null,
  INVITE_BY_LINK: null,
  QUICK_SEARCH: null,
  ATTACHMENTS: null
});

export const PreferencesTabTypes = keymirror({
  GENERAL: null,
  NOTIFICATIONS: null,
  SECURITY: null,
  BLOCKED: null,
  ABOUT: null
});


export const Path = {
  toImages: 'assets/images',
  toEmoji: 'assets/images/emoji'
};

export const endpoints = [
  'wss://front1-ws-mtproto-api-rev2.actor.im',
  'wss://front2-ws-mtproto-api-rev2.actor.im'
];

export const appName = 'Actor';
export const rootElement = 'actor-web-app';
export const helpPhone = '75551234567';

export default {
  AuthSteps,
  ActionTypes,
  EventTypes,
  CallTypes,
  CallStates,
  MessageArtPopupState,
  ConnectionStates,
  PeerTypes,
  ActivityTypes,
  CreateGroupSteps,
  MessageContentTypes,
  KeyCodes,
  AsyncActionStates,
  ModalTypes,
  PreferencesTabTypes,
  Path,
  endpoints,
  appName,
  rootElement,
  helpPhone
};
