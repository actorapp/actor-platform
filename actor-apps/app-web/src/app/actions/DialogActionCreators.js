/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatch, dispatchAsync } from 'dispatcher/ActorAppDispatcher';
import { ActionTypes, PeerTypes } from 'constants/ActorAppConstants';
import ActorClient from 'utils/ActorClient';

const DialogActionCreators = {
  setDialogs(dialogs) {
    dispatch(ActionTypes.DIALOGS_CHANGED, {
      dialogs
    });
  },

  selectDialogPeer(peer) {
    dispatch(ActionTypes.SELECT_DIALOG_PEER, {
      peer
    });
  },

  selectDialogPeerUser(userId) {
    if (userId === ActorClient.getUid()) {
      console.warn('You can\'t chat with yourself');
    } else {
      this.selectDialogPeer({
        type: PeerTypes.USER,
        id: userId
      });
    }
  },

  createSelectedDialogInfoChanged(info) {
    dispatch(ActionTypes.SELECTED_DIALOG_INFO_CHANGED, {
      info
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

  deleteMessages(peer, rids) {
    console.warn('delete', peer, rids);
    //ActorClient.deleteMessages(peer, rids);
  },

  leaveGroup(gid) {
    ActorClient
      .leaveGroup(gid)
      .then(() => {
        dispatch(ActionTypes.LEFT_GROUP, {
          gid
        });
      });
  },

  changeNotificationsEnabled(peer, isEnabled) {
    dispatch(ActionTypes.NOTIFICATION_CHANGE, {
      peer, isEnabled
    });
  }

};

export default DialogActionCreators;
