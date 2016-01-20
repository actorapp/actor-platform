/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { Store } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import ActorClient from '../utils/ActorClient';
import { ActionTypes, PeerTypes } from '../constants/ActorAppConstants';

import DraftStore from './DraftStore';

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
let _isFocusDisabled = false;

class ComposeStore extends Store {
  constructor(dispatcher) {
    super(dispatcher);
  }

  getMentions() {
    return mentions;
  }

  getText() {
    return text;
  }

  isFocusDisabled() {
    return _isFocusDisabled;
  }

  onTyping = (action) => {
    text = action.text;
    const query = getQuery(text, action.caretPosition);

    if (action.peer.type === PeerTypes.GROUP && query !== null) {
      mentions = ActorClient.findMentions(action.peer.id, query.text);
    } else {
      mentions = null;
    }

    this.__emitChange();
  };

  onMentionInsert = (action) => {
    const query = getQuery(action.text, action.caretPosition);
    const mentionEnding = query.atStart ? ': ' : ' ';

    text = action.text.substring(0, action.caretPosition - query.text.length - 1) +
      action.mention.mentionText +
      mentionEnding +
      action.text.substring(action.caretPosition, action.text.length);

    mentions = null;

    this.__emitChange();
  };

  onMentionClose = () => {
    mentions = null;
    this.__emitChange();
  };

  onComposeClean = () => {
    text = '';
    mentions = null;
    this.__emitChange();
  };

  onSelectDialogPeer = () => {
    //waitFor([DraftStore.dispatchToken]);
    text = DraftStore.getDraft();
    this.__emitChange();
  };

  onEmojiInsert = (action) => {
    const emojiText = `${action.emoji} `;

    text = action.text.substring(0, action.caretPosition) +
      emojiText +
      action.text.substring(action.caretPosition, action.text.length);

    this.__emitChange();
  };

  onComposePaste = (newText) => {
    text = newText;
    this.__emitChange();
  };

  __onDispatch(action) {
    switch (action.type) {
      case ActionTypes.COMPOSE_TYPING:
        this.onTyping(action);
        break;
      case ActionTypes.COMPOSE_MENTION_INSERT:
        this.onMentionInsert(action);
        break;
      case ActionTypes.COMPOSE_MENTION_CLOSE:
        this.onMentionClose();
        break;
      case ActionTypes.COMPOSE_CLEAN:
        this.onComposeClean();
        break;
      case ActionTypes.SELECT_DIALOG_PEER:
        this.onSelectDialogPeer();
        break;
      case ActionTypes.EMOJI_INSERT:
        this.onEmojiInsert(action);
        break;
      case ActionTypes.COMPOSE_PASTE:
        this.onComposePaste(action.text);
        break;
      default:
    }
  };
}

export default new ComposeStore(Dispatcher);
