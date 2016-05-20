/*
 * Copyright (C) 2016 Actor LLC. <https://actor.im>
 */

import { ReduceStore } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';
import Immutable from 'immutable';

class AttachmentsStore extends ReduceStore {
  getInitialState() {
    return {
      attachments: new Immutable.List(),
      selectedIndex: 0
    }
  }

  reduce(state,action) {
    switch (action.type) {
      case ActionTypes.ATTACHMENT_MODAL_SHOW:
        return {
          ...state,
          attachments: new Immutable.List(action.attachments)
        }
      case ActionTypes.ATTACHMENT_MODAL_HIDE:
        return this.getInitialState();

      case ActionTypes.ATTACHMENT_SELECT:
        return {
          ...state,
          selectedIndex: action.index
        }

      case ActionTypes.ATTACHMENT_CHANGE:
        const changedAttachment = state.attachments.get(state.selectedIndex);

        return {
          ...state,
          attachments: state.attachments.set(state.selectedIndex, { ...changedAttachment, sendAsPicture: action.sendAsPicture })
        };

      case ActionTypes.ATTACHMENT_DELETE:
      case ActionTypes.ATTACHMENT_SEND:
        return {
          attachments: state.attachments.delete(state.selectedIndex),
          selectedIndex: 0
        }
      case ActionTypes.ATTACHMENT_SEND_ALL:
        return this.getInitialState();

      default:
        return state;
    }
  }

  getAllAttachments() {
    return this.getState().attachments.toArray();
  }

  getAttachment(index = this.getSelectedIndex()) {
    return this.getState().attachments.get(index);
  }

  getSelectedIndex() {
    return this.getState().selectedIndex;
  }
}

export default new AttachmentsStore(Dispatcher);
