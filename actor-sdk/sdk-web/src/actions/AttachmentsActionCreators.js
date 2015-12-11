/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { forEach } from 'lodash';

import { dispatch } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

import MessageActionCreators from './MessageActionCreators';

import DialogStore from '../stores/DialogStore';
import AttachmentStore from '../stores/AttachmentStore';

export default {
  show: (attachments) => dispatch(ActionTypes.ATTACHMENT_MODAL_SHOW, { attachments }),
  hide: () => dispatch(ActionTypes.ATTACHMENT_MODAL_HIDE),

  selectAttachment: (index) => dispatch(ActionTypes.ATTACHMENT_SELECT, { index }),
  changeAttachment: (sendAsPicture) => dispatch(ActionTypes.ATTACHMENT_CHANGE, { sendAsPicture }),
  deleteAttachment: () => dispatch(ActionTypes.ATTACHMENT_DELETE),

  sendAttachment: () => {
    const currentPeer = DialogStore.getCurrentPeer();
    const attachment = AttachmentStore.getAttachment();

    if (attachment.isImage && attachment.sendAsPicture) {
      MessageActionCreators.sendPhotoMessage(currentPeer, attachment.file);
    } else {
      MessageActionCreators.sendFileMessage(currentPeer, attachment.file);
    }

    dispatch(ActionTypes.ATTACHMENT_SEND);
  },

  sendAll: (attachments) => {
    const currentPeer = DialogStore.getCurrentPeer();

    forEach(attachments, (attachment) => {
      if (attachment.isImage && attachment.sendAsPicture) {
        MessageActionCreators.sendPhotoMessage(currentPeer, attachment.file);
      } else {
        MessageActionCreators.sendFileMessage(currentPeer, attachment.file);
      }
    });
    dispatch(ActionTypes.ATTACHMENT_SEND_ALL, { attachments });
  }
}
