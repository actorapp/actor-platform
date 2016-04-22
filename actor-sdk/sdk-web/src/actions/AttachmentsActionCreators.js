/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatch } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';
import blobToFile from '../utils/blobToFile';

import MessageActionCreators from './MessageActionCreators';
import ComposeActionCreators from '../actions/ComposeActionCreators';

import DialogStore from '../stores/DialogStore';
import AttachmentsStore from '../stores/AttachmentsStore';

export default {
  show(attachments) {
    const normalizedAttachments = attachments.map((file) => {
      if (file instanceof File == false) {
        file = blobToFile(file);
      }

      return {
        isImage: file.type.includes('image') && file.type !== 'image/gif',
        sendAsPicture: true,
        file
      }
    });

    dispatch(ActionTypes.ATTACHMENT_MODAL_SHOW, { attachments: normalizedAttachments });
    ComposeActionCreators.toggleAutoFocus(false);
  },

  hide() {
    dispatch(ActionTypes.ATTACHMENT_MODAL_HIDE);
    ComposeActionCreators.toggleAutoFocus(true);
  },

  selectAttachment(index) {
    dispatch(ActionTypes.ATTACHMENT_SELECT, { index })
  },

  changeAttachment(sendAsPicture) {
    dispatch(ActionTypes.ATTACHMENT_CHANGE, { sendAsPicture });
  },

  deleteAttachment() {
    dispatch(ActionTypes.ATTACHMENT_DELETE);
    if (AttachmentsStore.getAllAttachments().length === 0) {
      this.hide();
    }
  },

  sendAttachment() {
    const currentPeer = DialogStore.getCurrentPeer();
    const attachment = AttachmentsStore.getAttachment();

    if (attachment.isImage && attachment.sendAsPicture) {
      MessageActionCreators.sendPhotoMessage(currentPeer, attachment.file);
    } else {
      MessageActionCreators.sendFileMessage(currentPeer, attachment.file);
    }

    dispatch(ActionTypes.ATTACHMENT_SEND);

    if (AttachmentsStore.getAllAttachments().length === 0) {
      this.hide();
    }

    ComposeActionCreators.toggleAutoFocus(true);
  },

  sendAll(attachments) {
    const currentPeer = DialogStore.getCurrentPeer();

    attachments.forEach((attachment) => {
      if (attachment.isImage && attachment.sendAsPicture) {
        MessageActionCreators.sendPhotoMessage(currentPeer, attachment.file);
      } else {
        MessageActionCreators.sendFileMessage(currentPeer, attachment.file);
      }
    });

    dispatch(ActionTypes.ATTACHMENT_SEND_ALL, { attachments });
    this.hide();
    ComposeActionCreators.toggleAutoFocus(true);
  }
}
