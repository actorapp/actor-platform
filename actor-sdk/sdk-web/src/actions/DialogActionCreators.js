/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatch, dispatchAsync } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes, PeerTypes } from '../constants/ActorAppConstants';
import history from '../utils/history';
import ActorClient from '../utils/ActorClient';
import PeerUtils from '../utils/PeerUtils';

import MessageActionCreators from './MessageActionCreators';
import TypingActionCreators from './TypingActionCreators';
import DialogInfoActionCreators from './DialogInfoActionCreators';
import OnlineActionCreators from './OnlineActionCreators';
import GroupProfileActionCreators from './GroupProfileActionCreators';
import DraftActionCreators from './DraftActionCreators';

import DialogStore from '../stores/DialogStore';
import MessageStore from '../stores/MessageStore';

let messagesBinding = null;

const DialogActionCreators = {
  setDialogs(dialogs) {
    dispatch(ActionTypes.DIALOGS_CHANGED, { dialogs });
  },

  selectDialogPeer(peer) {
    const currentPeer = DialogStore.getCurrentPeer();

    // Unbind from previous peer
    if (currentPeer !== null) {
      DraftActionCreators.saveDraft(currentPeer);
      dispatch(ActionTypes.UNBIND_DIALOG_PEER, { peer: currentPeer });

      this.onConversationClosed(currentPeer);
      messagesBinding && messagesBinding.unbind();
      ActorClient.unbindTyping(currentPeer, TypingActionCreators.setTyping);

      switch (currentPeer.type) {
        case PeerTypes.USER:
          ActorClient.unbindUser(currentPeer.id, DialogInfoActionCreators.setDialogInfo);
          ActorClient.unbindUserOnline(currentPeer.id, OnlineActionCreators.setUserOnline);
          break;
        case PeerTypes.GROUP:
          ActorClient.unbindGroup(currentPeer.id, DialogInfoActionCreators.setDialogInfo);
          ActorClient.unbindGroupOnline(currentPeer.id, OnlineActionCreators.setGroupOnline);
          break;
        default:
      }
    }

    if (peer !== null) {
      dispatch(ActionTypes.BIND_DIALOG_PEER, { peer });
      DraftActionCreators.loadDraft(peer);

      this.onConversationOpen(peer);
      messagesBinding = ActorClient.bindMessages(peer, MessageActionCreators.setMessages);
      ActorClient.bindTyping(peer, TypingActionCreators.setTyping);

      switch(peer.type) {
        case PeerTypes.USER:
          ActorClient.bindUser(peer.id, DialogInfoActionCreators.setDialogInfo);
          ActorClient.bindUserOnline(peer.id, OnlineActionCreators.setUserOnline);
          break;
        case PeerTypes.GROUP:
          ActorClient.bindGroup(peer.id, DialogInfoActionCreators.setDialogInfo);
          ActorClient.bindGroupOnline(peer.id, OnlineActionCreators.setGroupOnline);
          GroupProfileActionCreators.getIntegrationToken(peer.id);
          break;
        default:
      }
    }
  },

  selectDialogPeerUser(uid) {
    if (uid === ActorClient.getUid()) {
      console.warn('You can\'t chat with yourself');
    } else {
      history.push(`/im/${PeerUtils.peerToString(ActorClient.getUserPeer(uid))}`);
    }
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

  leaveGroup(gid) {
    dispatchAsync(ActorClient.leaveGroup(gid), {
      request: ActionTypes.GROUP_LEAVE,
      success: ActionTypes.GROUP_LEAVE_SUCCESS,
      failure: ActionTypes.GROUP_LEAVE_ERROR
    }, { gid });
  },

  deleteChat(peer) {
    const gid = peer.id;
    const leaveGroup = () => dispatchAsync(ActorClient.leaveGroup(gid), {
      request: ActionTypes.GROUP_LEAVE,
      success: ActionTypes.GROUP_LEAVE_SUCCESS,
      failure: ActionTypes.GROUP_LEAVE_ERROR
    }, { gid });
    const deleteChat = () => dispatchAsync(ActorClient.deleteChat(peer), {
      request: ActionTypes.GROUP_DELETE,
      success: ActionTypes.GROUP_DELETE_SUCCESS,
      failure: ActionTypes.GROUP_DELETE_ERROR
    }, { peer });

    switch (peer.type) {
      case PeerTypes.USER:
        deleteChat();
        break;
      case PeerTypes.GROUP:
        leaveGroup()
          .then(deleteChat);
        break;
      default:
    }
  },

  clearChat(peer) {
    dispatchAsync(ActorClient.clearChat(peer), {
      request: ActionTypes.GROUP_CLEAR,
      success: ActionTypes.GROUP_CLEAR_SUCCESS,
      failure: ActionTypes.GROUP_CLEAR_ERROR
    }, { peer });
  },

  hideChat(peer) {
    dispatchAsync(ActorClient.hideChat(peer), {
      request: ActionTypes.GROUP_HIDE,
      success: ActionTypes.GROUP_HIDE_SUCCESS,
      failure: ActionTypes.GROUP_HIDE_ERROR
    }, { peer });
  },

  loadMoreMessages(peer) {
    if (MessageStore.isAllRendered()) {
      this.onChatEnd(peer);
    } else {
      dispatch(ActionTypes.MESSAGES_LOAD_MORE);
    }
  }
};

export default DialogActionCreators;
