import ActorAppDispatcher from 'dispatcher/ActorAppDispatcher';
import { ActionTypes, ActivityTypes, PeerTypes } from 'constants/ActorAppConstants';
import ActorClient from 'utils/ActorClient';

import DialogStore from 'stores/DialogStore';

import { EventEmitter } from 'events';
import assign from 'object-assign';

const CHANGE_EVENT = 'change';

let _isOpen = false,
    _activity = null;

var ActivityStore = assign({}, EventEmitter.prototype, {
  getActivity() {
    return _activity;
  },

  isOpen() {
    return _isOpen;
  },

  emitChange() {
    this.emit(CHANGE_EVENT);
  },

  addChangeListener(callback) {
    this.on(CHANGE_EVENT, callback);
  },

  removeChangeListener(callback) {
    this.removeListener(CHANGE_EVENT, callback);
  }
});


let _cleanup = () => {};

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
      return;
  }
};

ActivityStore.dispatchToken = ActorAppDispatcher.register(action => {
  switch (action.type) {
    case ActionTypes.ACTIVITY_HIDE:
      _isOpen = false;
      ActivityStore.emitChange();
      break;
    case ActionTypes.ACTIVITY_SHOW:
      _isOpen = true;
      ActivityStore.emitChange();
      break;
    case ActionTypes.SELECT_DIALOG_PEER:
      ActorAppDispatcher.waitFor([DialogStore.dispatchToken]);
      _setActivityFromPeer();
      ActivityStore.emitChange();
      break;
    default:
      return;
  }
});

export default ActivityStore;
