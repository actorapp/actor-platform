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
      return;
  }
};

ActivityStore.dispatchToken = ActorAppDispatcher.register(action => {
  console.info(action);
  switch (action.type) {
    case ActionTypes.HIDE_ACTIVITY:
      _isOpen = false;
      break;
    case ActionTypes.SHOW_ACTIVITY:
      _isOpen = true;
      break;
    case ActionTypes.SELECT_DIALOG_PEER:
      ActorAppDispatcher.waitFor([DialogStore.dispatchToken]);
      _setActivityFromPeer();
      break;

    default:
      return;
  }
  ActivityStore.emitChange();
});

export default ActivityStore;
