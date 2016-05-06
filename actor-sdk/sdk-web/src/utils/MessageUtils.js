import { emoji } from './EmojiUtils';
import { MessageStates } from '../constants/ActorAppConstants';

function isMessageSender(message, uid) {
  return uid === message.sender.peer.id;
}

export function getMessageState(message, uid, receiveDate, readDate) {
  if (message.sender.peer.id !== uid) {
    return MessageStates.UNKNOWN;
  }

  if (message.isOut && message.state === MessageStates.SENT) {
    if (message.sortDate <= readDate) {
      return MessageStates.READ;
    }

    if (message.sortDate <= receiveDate) {
      return MessageStates.RECEIVED;
    }
  }

  return message.state;
}

export function prepareTextMessage(text) {
  emoji.change_replace_mode('unified');
  return emoji.replace_colons(text);
}

export function quoteMessage(text) {
  return text
    .trim()
    .split('\n')
    .map((line) => `> ${line}`)
    .join('\n');
}

export function isLastMessageMine(uid, { messages }) {
  const lastMessage = messages[messages.length - 1];
  return lastMessage && isMessageSender(lastMessage, uid);
}

export function getFirstUnreadMessageIndex(messages, readDate, uid) {
  if (readDate === 0 || !messages.length) {
    return -1;
  }

  let index = -1;
  for (let i = messages.length - 1; i >= 0; i--) {
    const message = messages[i];
    if (message.sortDate <= readDate || isMessageSender(message, uid)) {
      return index;
    }

    index = i;
  }

  // maybe unreachable
  return index;
}
