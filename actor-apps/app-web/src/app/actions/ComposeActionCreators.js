import ActorClient from 'utils/ActorClient';

import ActorAppDispatcher from 'dispatcher/ActorAppDispatcher';

import { ActionTypes } from 'constants/ActorAppConstants';

import DraftActionCreators from 'actions/DraftActionCreators';

export default {
  cleanText: () => {
    DraftActionCreators.saveDraft('', true);
    ActorAppDispatcher.dispatch({
      type: ActionTypes.COMPOSE_CLEAN
    });
  },

  insertMention: (peer, text, caretPosition, mention) => {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.COMPOSE_MENTION_INSERT,
      peer: peer,
      text: text,
      caretPosition: caretPosition,
      mention: mention
    });
  },

  closeMention: () => {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.COMPOSE_MENTION_CLOSE
    });
  },

  onTyping: function(peer, text, caretPosition) {
    if (text !== '') {
      ActorClient.onTyping(peer);
    }

    DraftActionCreators.saveDraft(text);
    ActorAppDispatcher.dispatch({
      type: ActionTypes.COMPOSE_TYPING,
      peer: peer,
      text: text,
      caretPosition: caretPosition
    });
  }
};
