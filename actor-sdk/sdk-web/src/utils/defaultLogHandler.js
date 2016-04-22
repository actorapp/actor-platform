import { LoggerTypes } from '../constants/ActorAppConstants';

function getMethod(type) {
  switch (type) {
    case LoggerTypes.INFO:
      return 'info';
    case LoggerTypes.ERROR:
      return 'error';
    case LoggerTypes.WARNING:
      return 'warn';
    case LoggerTypes.DEBUG:
    default:
      return 'log';
  }
}

function logHandler(tag, type, message) {
  console[getMethod(type)](tag + ': ' + message);
}

export default logHandler;
