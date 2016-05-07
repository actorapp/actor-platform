/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { ReduceStore } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import ActorClient from '../utils/ActorClient';
import { ActionTypes, PeerTypes } from '../constants/ActorAppConstants';

import DraftStore from './DraftStore';

function parseCommand(text) {
  const matches = /^\/(.)?(?: (.+))?/.exec(text);
  if (!matches) {
    return null;
  }

  return {
    name: matches[1],
    args: matches[2]
  };
}

function parseMentionQuery(text, position) {
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
}

class ComposeStore extends ReduceStore {
  getInitialState() {
    return {
      text: '',
      mentions: null,
      commands: null,
      autoFocus: true
    };
  }

  reduce(state, action) {
    switch (action.type) {
      case ActionTypes.COMPOSE_TYPING:
        const nextState = {
          ...state,
          text: action.text,
          commands: null,
          mentions: null
        };

        if (action.peer.type === PeerTypes.GROUP) {
          const query = parseMentionQuery(action.text, action.caretPosition);
          if (query) {
            nextState.mentions = ActorClient.findMentions(action.peer.id, query.text);
          }
        } else {
          const command = parseCommand(action.text);
          if (command) {
            nextState.commands = ActorClient.findBotCommands(action.peer.id, command.name || '');
          }
        }

        return nextState;

      case ActionTypes.COMPOSE_MENTION_INSERT:
        const query = parseMentionQuery(action.text, action.caretPosition);
        if (!query) {
          console.error('Mention not found', { state, action });
          return state;
        }

        const mentionEnding = query.atStart ? ': ' : ' ';
        const textBeforeMention = action.text.substring(0, action.caretPosition - query.text.length - 1);
        const textAfterMention = action.text.substring(action.caretPosition, action.text.length);

        return {
          ...state,
          text: textBeforeMention + action.mention.mentionText + mentionEnding + textAfterMention,
          mentions: null
        };

      case ActionTypes.COMPOSE_MENTION_CLOSE:
        return {
          ...state,
          mentions: null
        };

      case ActionTypes.COMPOSE_CLEAN:
        return {
          ...state,
          text: '',
          commands: null,
          mentions: null
        };

      case ActionTypes.DRAFT_LOAD:
        return {
          ...state,
          text: DraftStore.getDraft()
        };

      case ActionTypes.EMOJI_INSERT:
        const textBeforeEmoji = action.text.substring(0, action.caretPosition);
        const textAfterEmoji = action.text.substring(action.caretPosition, action.text.length);

        return {
          ...state,
          text: textBeforeEmoji + action.emoji + ' ' + textAfterEmoji
        };

      case ActionTypes.COMPOSE_PASTE:
        return {
          ...state,
          text: action.text
        };

      case ActionTypes.COMPOSE_TOGGLE_AUTO_FOCUS:
        return {
          ...state,
          autoFocus: action.isEnable
        };

      case ActionTypes.SEARCH_TOGGLE_FOCUS:
        return {
          ...state,
          autoFocus: !action.isEnable
        };

      default:
        return state;
    }
  }
}

export default new ComposeStore(Dispatcher);
