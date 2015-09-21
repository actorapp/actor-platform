/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import _ from 'lodash';
import { EventEmitter } from 'events';
import ActorClient from 'utils/ActorClient';

import { register, waitFor } from 'dispatcher/ActorAppDispatcher';

import { ActionTypes, PeerTypes } from 'constants/ActorAppConstants';

import GroupStore from './GroupStore';
import DraftStore from './DraftStore';
import UserStore from './UserStore';

const CHANGE_EVENT = 'change';

const getQuery = (text, position) => {
  const run = (runText, query) => {
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

  const runText = text.substring(0, position);
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

const ComposeStoreInstance = new ComposeStore();

const onTyping = (action) => {
  text = action.text;
  const query = getQuery(text, action.caretPosition);

  if (action.peer.type === PeerTypes.GROUP && query !== null) {
    mentions = ActorClient.findMentions(action.peer.id, query.text);
  } else {
    mentions = null;
  }

  ComposeStoreInstance.emitChange();
};

const onMentionInsert = (action) => {
  const query = getQuery(action.text, action.caretPosition);
  const mentionEnding = query.atStart ? ': ' : ' ';

  text = action.text.substring(0, action.caretPosition - query.text.length - 1) +
         action.mention.mentionText +
         mentionEnding +
         action.text.substring(action.caretPosition, action.text.length);

  mentions = null;

  ComposeStoreInstance.emitChange();
};

const onMentionClose = () => {
  mentions = null;
  ComposeStoreInstance.emitChange();
};

const onComposeClean = () => {
  text = '';
  mentions = null;
  ComposeStoreInstance.emitChange();
};

const onSelectDialogPeer = () => {
  waitFor([DraftStore.dispatchToken]);
  text = DraftStore.getDraft();
  ComposeStoreInstance.emitChange();
};

const onEmojiInsert = (action) => {
  const emojiText = `${action.emoji} `;

  text = action.text.substring(0, action.caretPosition) +
         emojiText +
         action.text.substring(action.caretPosition, action.text.length);

  ComposeStoreInstance.emitChange();
};

ComposeStoreInstance.dispatchToken = register(action => {
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
    case ActionTypes.COMPOSE_EMOJI_INSERT:
      onEmojiInsert(action);
      break;
    default:
  }
});

export default ComposeStoreInstance;
