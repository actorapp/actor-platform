import ActorClient from '../utils/ActorClient';

import ActorAppDispatcher from '../dispatcher/ActorAppDispatcher';
import ActorAppConstants from '../constants/ActorAppConstants';

var ActionTypes = ActorAppConstants.ActionTypes;

export default {
  setDialogs: function(dialogs) {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.DIALOGS_CHANGED,
      dialogs: dialogs
    });
  },

  selectDialogPeer: function(peer) {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.SELECT_DIALOG_PEER,
      peer: peer
    });
  },

  selectDialogPeerUser: function(userId) {
    if (userId === ActorClient.getUid()) {
      console.warn("You can't chat with yourself");
    } else {
      this.selectDialogPeer({
        type: ActorAppConstants.PeerTypes.USER,
        id: userId
      });
    }
  },

  createSelectedDialogInfoChanged: function(info) {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.SELECTED_DIALOG_INFO_CHANGED,
      info: info
    });
  },

  onConversationOpen: function(peer) {
    ActorClient.onConversationOpen(peer);
  },

  onConversationClosed: function(peer) {
    ActorClient.onConversationClosed(peer);
  },

  leaveGroup: function(groupId) {
    ActorClient.leaveGroup(groupId);
  },

  kickMember: function(userId, groupId) {
    ActorClient.kickMember(userId, groupId);
  }
};

