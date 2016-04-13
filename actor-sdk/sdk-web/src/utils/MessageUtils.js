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
