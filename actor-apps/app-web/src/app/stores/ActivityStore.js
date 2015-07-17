import ActorAppDispatcher from 'dispatcher/ActorAppDispatcher';
import { ActionTypes, ActivityTypes, PeerTypes } from 'constants/ActorAppConstants';
import ActorClient from 'utils/ActorClient';

import DialogStore from 'stores/DialogStore';

import { EventEmitter } from 'events';
import assign from 'object-assign';

var CHANGE_EVENT = 'change';

var _activity = null;

var ActivityStore = assign({}, EventEmitter.prototype, {
  getActivity: function () {
    return _activity;
  },

  emitChange: function () {
    this.emit(CHANGE_EVENT);
  },

  addChangeListener: function (callback) {
    this.on(CHANGE_EVENT, callback);
  },

  removeChangeListener: function (callback) {
    this.removeListener(CHANGE_EVENT, callback);
  }
});


var _cleanup = function () {
};

var _setActivityFromPeer = function () {
  _cleanup();

  var peer = DialogStore.getSelectedDialogPeer();
  switch (peer.type) {
    case PeerTypes.USER:
    {
      let change = function (user) {
        _activity = {
          type: ActivityTypes.USER_PROFILE,
          user: user
        };

        ActivityStore.emitChange();
      };

      _cleanup = function () {
        ActorClient.unbindUser(peer.id, change);
      };

      ActorClient.bindUser(peer.id, change);
    }
      break;
    case PeerTypes.GROUP:
      _cleanup();
    {
      let change = function (group) {
        _activity = {
          type: ActivityTypes.GROUP_PROFILE,
          group: group
        };

        ActivityStore.emitChange();
      };

      _cleanup = function () {
        ActorClient.unbindGroup(peer.id, change);
      };

      ActorClient.bindGroup(peer.id, change);
    }
      break;
    default:
  }
};

ActivityStore.dispatchToken = ActorAppDispatcher.register(function (action) {
  switch (action.type) {
    case ActionTypes.HIDE_ACTIVITY:
      _activity = null;
      ActivityStore.emitChange();
      break;

    case ActionTypes.SHOW_ACTIVITY:
      ActorAppDispatcher.waitFor([DialogStore.dispatchToken]);
      _setActivityFromPeer();
      break;

    case ActionTypes.SELECT_DIALOG_PEER:
      if (_activity != null) { // check if it is not hidden
        ActorAppDispatcher.waitFor([DialogStore.dispatchToken]);
        _setActivityFromPeer();
      }
      break;

    default:
  }
});

export default ActivityStore;
