/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { map, remove } from 'lodash';

import { Store } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

let _isOpen = false,
    _attachments = [],
    _selectedIndex = 0;

const SEND_AS_PICTURE = true;

class AttachmentStore extends Store {
  constructor(Dispatcher) {
    super(Dispatcher);
  }

  isOpen() {
    return _isOpen;
  }

  getAllAttachments() {
    return _attachments;
  }

  getAttachment(index = _selectedIndex) {
    return _attachments[index];
  }

  getSelectedIndex() {
    return _selectedIndex;
  }

  deleteAttachment(index = _selectedIndex) {
    _attachments.splice(index, 1);

    if (_attachments.length === 0) {
      this.resetStore();
    }

    _selectedIndex = 0; // TODO: select relevant index
  }

  resetStore() {
    _isOpen = false;
    _attachments = [];
    _selectedIndex = 0;
  }

  __onDispatch = (action) => {
    switch (action.type) {
      case ActionTypes.ATTACHMENT_MODAL_SHOW:
        _isOpen = true;
        _attachments = map(action.attachments, (file) => {
          const isImage = file.type.includes('image');
          return {
            isImage,
            sendAsPicture: SEND_AS_PICTURE,
            file
          }
        });
        this.__emitChange();
        break;
      case ActionTypes.ATTACHMENT_MODAL_HIDE:
        this.resetStore();
        this.__emitChange();
        break;
      case ActionTypes.ATTACHMENT_SELECT:
        _selectedIndex = action.index;
        this.__emitChange();
        break;
      case ActionTypes.ATTACHMENT_CHANGE:
        _attachments[_selectedIndex].sendAsPicture = action.sendAsPicture;
        this.__emitChange();
        break;
      case ActionTypes.ATTACHMENT_DELETE:
      case ActionTypes.ATTACHMENT_SEND:
        this.deleteAttachment();
        this.__emitChange();
        break;
      case ActionTypes.ATTACHMENT_SEND_ALL:
        this.resetStore();
        this.__emitChange();
        break;
      default:
    }
  };
}

export default new AttachmentStore(Dispatcher);
