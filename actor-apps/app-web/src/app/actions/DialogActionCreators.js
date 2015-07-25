import ActorClient from 'utils/ActorClient';

import ActorAppDispatcher from 'dispatcher/ActorAppDispatcher';
import ActorAppConstants from 'constants/ActorAppConstants';

const ActionTypes = ActorAppConstants.ActionTypes;

const DialogActionCreators = {
  setDialogs(dialogs) {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.DIALOGS_CHANGED,
      dialogs: dialogs
    });
  },

  selectDialogPeer(peer) {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.SELECT_DIALOG_PEER,
      peer: peer
    });
  },

  selectDialogPeerUser(userId) {
    if (userId === ActorClient.getUid()) {
      console.warn('You can\'t chat with yourself');
    } else {
      this.selectDialogPeer({
        type: ActorAppConstants.PeerTypes.USER,
        id: userId
      });
    }
  },

  createSelectedDialogInfoChanged(info) {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.SELECTED_DIALOG_INFO_CHANGED,
      info: info
    });
  },

  onConversationOpen(peer) {
    ActorClient.onConversationOpen(peer);
  },

  onConversationClosed(peer) {
    ActorClient.onConversationClosed(peer);
  },

  onDialogsEnd() {
    ActorClient.onDialogsEnd();
  },

  onChatEnd(peer) {
    ActorClient.onChatEnd(peer);
  },

  leaveGroup(groupId) {
    ActorClient.leaveGroup(groupId);
  },

  kickMember(userId, groupId) {
    ActorClient.kickMember(userId, groupId);
  },

  changeNotificationsEnabled(peer, isEnabled) {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.NOTIFICATION_CHANGE,
      peer: peer,
      isEnabled: isEnabled
    });

  }

};

export default DialogActionCreators;
