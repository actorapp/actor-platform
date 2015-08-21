import _ from 'lodash';
import { EventEmitter } from 'events';
import ActorClient from 'utils/ActorClient';

import ActorAppDispatcher from 'dispatcher/ActorAppDispatcher';

import { ActionTypes } from 'constants/ActorAppConstants';

import {PeerTypes} from 'constants/ActorAppConstants';

import GroupStore from './GroupStore';
import DraftStore from './DraftStore';
import UserStore from './UserStore';

const CHANGE_EVENT = 'change';

let getQuery = (text, position) => {
  let run = (runText, query) => {
    if (runText.length === 0) {
      return null;
    } else {
      const lastChar = runText.charAt(runText.length - 1);
      if (lastChar === '@') {
        const charBeforeAt = runText.charAt(runText.length - 2);
        if (charBeforeAt.trim() === '') {
          const text = (query || '');
          const atStart = text.length + 1 === position;

          return {
            text: text,
            atStart: atStart
          };
        } else {
          return null;
        }
      } else if (lastChar.trim() === '') {
        return null;
      } else {
        return run(runText.substring(0, runText.length - 1), lastChar + (query || ''));
      }
    }
  };

  let runText = text.substring(0, position);
  return run(runText, null);
};

let text = '';
let mentions = null;

class ComposeStore extends EventEmitter {
  getMentions() {
    return mentions;
  }

  getText() {
    return text;
  }

  emitChange() {
    this.emit(CHANGE_EVENT);
  }

  addChangeListener(callback) {
    this.on(CHANGE_EVENT, callback);
  }

  removeChangeListener(callback) {
    this.removeListener(CHANGE_EVENT, callback);
  }
}

const instance = new ComposeStore();

let onTyping = (action) => {
  text = action.text;
  const query = getQuery(text, action.caretPosition);

  if (action.peer.type === PeerTypes.GROUP && query !== null) {
    mentions = ActorClient.findMentions(action.peer.id, query.text);
  } else {
    mentions = null;
  }

  instance.emitChange();
};

let onMentionInsert = (action) => {
  const query = getQuery(action.text, action.caretPosition);
  const mentionEnding = query.atStart ? ': ' : ' ';

  text = action.text.substring(0, action.caretPosition - query.text.length - 1) +
         action.mention.mentionText +
         action.text.substring(action.caretPosition, action.text.length) +
         mentionEnding;

  mentions = null;

  instance.emitChange();
};

let onMentionClose = () => {
  mentions = null;
  instance.emitChange();
};

let onComposeClean = () => {
  text = '';
  mentions = null;
  instance.emitChange();
};

let onSelectDialogPeer = () => {
  ActorAppDispatcher.waitFor([DraftStore.dispatchToken]);
  text = DraftStore.getDraft();
  instance.emitChange();
};

instance.dispatchToken = ActorAppDispatcher.register(action => {
  switch (action.type) {
    case ActionTypes.COMPOSE_TYPING:
      onTyping(action);
      break;
    case ActionTypes.COMPOSE_MENTION_INSERT:
      onMentionInsert(action);
      break;
    case ActionTypes.COMPOSE_MENTION_CLOSE:
      onMentionClose();
      break;
    case ActionTypes.COMPOSE_CLEAN:
      onComposeClean();
      break;
    case ActionTypes.SELECT_DIALOG_PEER:
      onSelectDialogPeer();
      break;
    default:
  }
});

export default instance;
