/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import ActorAppDispatcher from 'dispatcher/ActorAppDispatcher';
import { ActionTypes, PeerTypes } from 'constants/ActorAppConstants';

import DialogActionCreators from 'actions/DialogActionCreators';
import GroupProfileActionCreators from 'actions/GroupProfileActionCreators';

import { EventEmitter } from 'events';
import assign from 'object-assign';

import ActorClient from 'utils/ActorClient';
import LoginStore from 'stores/LoginStore';

const CHANGE_EVENT = 'change',
      SELECT_EVENT = 'select',
      SELECTED_CHANGE_EVENT = 'selected_change',
      TYPING_EVENT = 'typing',
      NOTIFICATION_CHANGE_EVENT = 'notification_change';

let _dialogs = [],
    _selectedDialogPeer = null,
    _selectedDialogInfo = null,
    _selectedDialogTyping = null,
    _currentPeer = null,
    _lastPeer = null;

var DialogStore = assign({}, EventEmitter.prototype, {
  emitChange: function() {
    this.emit(CHANGE_EVENT);
  },

  addChangeListener: function(callback) {
    this.on(CHANGE_EVENT, callback);
  },

  removeChangeListener: function(callback) {
    this.removeListener(CHANGE_EVENT, callback);
  },

  emitSelect: function() {
    this.emit(SELECT_EVENT);
  },

  addSelectListener: function(callback) {
    this.on(SELECT_EVENT, callback);
  },

  removeSelectListener: function(callback) {
    this.removeListener(SELECT_EVENT, callback);
  },

  emitSelectedChange: function() {
    this.emit(SELECTED_CHANGE_EVENT);
  },

  addSelectedChangeListener: function(callback) {
    this.on(SELECTED_CHANGE_EVENT, callback);
  },

  removeSelectedChangeListener: function(callback) {
    this.removeListener(SELECTED_CHANGE_EVENT, callback);
  },

  emitTyping: function() {
    this.emit(TYPING_EVENT);
  },

  addTypingListener: function(callback) {
    this.on(TYPING_EVENT, callback);
  },

  removeTypingListener: function(callback) {
    this.removeListener(TYPING_EVENT, callback);
  },

  getSelectedDialogInfo: function() {
    return _selectedDialogInfo;
  },

  getSelectedDialogPeer: function() {
    return _selectedDialogPeer;
  },

  getSelectedDialogTyping: function() {
    return _selectedDialogTyping;
  },

  getAll() {
    return _dialogs;
  },

  // Notifications settings
  isNotificationsEnabled: function(peer) {
    return ActorClient.isNotificationsEnabled(peer);
  },

  emitNotificationChange() {
    this.emit(NOTIFICATION_CHANGE_EVENT);
  },

  addNotificationsListener(callback) {
    this.on(NOTIFICATION_CHANGE_EVENT, callback);
  },

  removeNotificationsListener(callback) {
    this.removeListener(NOTIFICATION_CHANGE_EVENT, callback);
  },

  getLastPeer() {
    return _lastPeer;
  },

  isGroupMember(group) {
    return (group.members.length > 0);
  }
});

var setDialogs = function(dialogs) {
  // We need setTimeout here because bindDialogs dispatches event
  // but bindDialogs itself is called in the middle of dispatch (DialogStore)
  setTimeout(function() {
    DialogActionCreators.setDialogs(dialogs);
  }, 0);
};

var onCurrentDialogInfoChange = function(info) {
  _selectedDialogInfo = info;
  DialogActionCreators.createSelectedDialogInfoChanged(info);
};

var bindDialogInfo = function(peer) {
  switch(peer.type) {
    case PeerTypes.USER:
      ActorClient.bindUser(peer.id, onCurrentDialogInfoChange);
      break;
    case PeerTypes.GROUP:
      ActorClient.bindGroup(peer.id, onCurrentDialogInfoChange);
      break;
    default:
  }
};

var unbindCurrentDialogInfo = function() {
  if (_currentPeer != null) {
    switch (_currentPeer.type) {
      case PeerTypes.USER:
        ActorClient.unbindUser(_currentPeer.id, onCurrentDialogInfoChange);
        break;
      case PeerTypes.GROUP:
        ActorClient.unbindGroup(_currentPeer.id, onCurrentDialogInfoChange);
        break;
      default:

    }
  }
};

var onCurrentDialogTypingChange = function(typing) {
  _selectedDialogTyping = typing.typing;
  DialogStore.emitTyping();
};

var bindDialogTyping = function(peer) {
  ActorClient.bindTyping(peer, onCurrentDialogTypingChange);
};

var unbindCurrentDialogTyping = function() {
  if (_currentPeer != null) {
    ActorClient.unbindTyping(_currentPeer, onCurrentDialogTypingChange);
  }
};

DialogStore.dispatchToken = ActorAppDispatcher.register(function(action) {
  switch(action.type) {
    case ActionTypes.SET_LOGGED_IN:
      ActorAppDispatcher.waitFor([LoginStore.dispatchToken]);

      ActorClient.bindDialogs(setDialogs);

      break;
    case ActionTypes.SELECT_DIALOG_PEER:
      unbindCurrentDialogInfo();
      unbindCurrentDialogTyping();

      _lastPeer = _currentPeer;
      _selectedDialogPeer = action.peer;
      _currentPeer = action.peer;

      // crutch check for membership
      // TODO: need method for membership check
      if (action.peer.type === PeerTypes.GROUP) {
        const group = ActorClient.getGroup(action.peer.id);
        setTimeout(function() {
          if (DialogStore.isGroupMember(group)) {
            bindDialogTyping(action.peer);
          }
          bindDialogInfo(action.peer);
        }, 0);
      } else {
        setTimeout(function() {
          bindDialogTyping(action.peer);
          bindDialogInfo(action.peer);
        }, 0);
      }
      // end crutch check for membership

      DialogStore.emitSelect();
      break;
    case ActionTypes.SELECTED_DIALOG_INFO_CHANGED:
      _selectedDialogInfo = action.info;
      DialogStore.emitSelectedChange();
      break;
    case ActionTypes.DIALOGS_CHANGED:
      _dialogs = action.dialogs;
      DialogStore.emitChange();
      break;
    case ActionTypes.NOTIFICATION_CHANGE:
      ActorClient.changeNotificationsEnabled(action.peer, action.isEnabled);
      DialogStore.emitNotificationChange();
      break;
    default:

  }
});

export default DialogStore;
