/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatch, dispatchAsync } from 'dispatcher/ActorAppDispatcher';
import { ActionTypes, PeerTypes } from 'constants/ActorAppConstants';
import ActorClient from 'utils/ActorClient';
import PeerUtils from 'utils/PeerUtils';
import RouterContainer from 'utils/RouterContainer';

const DialogActionCreators = {
  setDialogs(dialogs) {
    dispatch(ActionTypes.DIALOGS_CHANGED, {
      dialogs
    });
  },

  selectDialogPeer(peer) {
    RouterContainer.get().transitionTo('main', {id: PeerUtils.peerToString(peer)});
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
    dispatchAsync(ActorClient.leaveGroup(gid), {
      request: ActionTypes.CHAT_LEAVE,
      success: ActionTypes.CHAT_LEAVE_SUCCESS,
      failure: ActionTypes.CHAT_LEAVE_ERROR
    }, { gid });
  },

  changeNotificationsEnabled(peer, isEnabled) {
    dispatch(ActionTypes.NOTIFICATION_CHANGE, {
      peer, isEnabled
    });
  },

  deleteChat(peer) {
    const gid = peer.id;
    const leaveGroup = () => dispatchAsync(ActorClient.leaveGroup(gid), {
      request: ActionTypes.CHAT_LEAVE,
      success: ActionTypes.CHAT_LEAVE_SUCCESS,
      failure: ActionTypes.CHAT_LEAVE_ERROR
    }, { gid });
    const deleteChat = () => dispatchAsync(ActorClient.deleteChat(peer), {
      request: ActionTypes.CHAT_DELETE,
      success: ActionTypes.CHAT_DELETE_SUCCESS,
      failure: ActionTypes.CHAT_DELETE_ERROR
    }, { peer });

    switch (peer.type) {
      case PeerTypes.USER:
        deleteChat();
        break;
      case PeerTypes.GROUP:
        leaveGroup().then(() => deleteChat());
        break;
    }
  },

  clearChat(peer) {
    dispatchAsync(ActorClient.clearChat(peer), {
      request: ActionTypes.CHAT_CLEAR,
      success: ActionTypes.CHAT_CLEAR_SUCCESS,
      failure: ActionTypes.CHAT_CLEAR_ERROR
    }, { peer });
  }
};

export default DialogActionCreators;
