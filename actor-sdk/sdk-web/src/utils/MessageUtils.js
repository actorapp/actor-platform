import { MessageStates } from '../constants/ActorAppConstants';

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

export function quoteMessage(text) {
  return text
    .trim()
    .split('\n')
    .map(((line) => `> ${line}`))
    .join('\n');
}

export function isLastMessageMine(uid, { messages }) {
  const lastMessage = messages[messages.length - 1];
  return lastMessage && uid === lastMessage.sender.peer.id;
}

export function getFirstUnreadMessageIndex(messages, readDate, uid) {
  if (readDate === 0 || !messages.length) {
    return -1;
  }

  let index = -1;
  for (let i = messages.length - 1; i--; i >= 0) {
    const message = messages[i];
    if (message.sortDate <= readDate || message.sender.peer.id === uid) {
      return index;
    }

    index = i;
  }

  // maybe unreachable
  return index;
}
