/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatch } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';
import DraftActionCreators from './DraftActionCreators';
import ActorClient from '../utils/ActorClient';

export default {
  cleanText() {
    DraftActionCreators.saveDraft('', true);
    dispatch(ActionTypes.COMPOSE_CLEAN);
  },

  insertMention(peer, text, caretPosition, mention) {
    dispatch(ActionTypes.COMPOSE_MENTION_INSERT, { peer, text, caretPosition, mention });
  },

  closeMention() {
    dispatch(ActionTypes.COMPOSE_MENTION_CLOSE);
  },

  pasteText(text) {
    if (text !== '') {
      dispatch(ActionTypes.COMPOSE_PASTE, { text });
    }
  },

  onTyping(peer, text, caretPosition) {
    if (text !== '') {
      ActorClient.onTyping(peer);
    }

    DraftActionCreators.saveDraft(text);
    dispatch(ActionTypes.COMPOSE_TYPING, { peer, text, caretPosition });
  }
};
