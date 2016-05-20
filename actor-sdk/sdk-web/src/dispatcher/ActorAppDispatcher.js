import { Dispatcher } from 'flux';
import { ActionTypes } from '../constants/ActorAppConstants';

const flux = new Dispatcher();

export function register(callback) {
  return flux.register(callback);
}

export function waitFor(ids) {
  return flux.waitFor(ids);
}

// Some Flux examples have methods like `handleViewAction`
// or `handleServerAction` here. They are only useful if you
// want to have extra pre-processing or logging for such actions,
// but I found no need for them.

/**
 * Dispatches a single action.
 */
export function dispatch(type, action = {}) {
  if (!type) {
    throw new Error('You forgot to specify type.');
  }

  // In production, thanks to DefinePlugin in webpack.config.production.js,
  // this comparison will turn `false`, and UglifyJS will cut logging out
  // as part of dead code elimination.
  if (process.env.NODE_ENV !== 'production') {
    // Logging all actions is useful for figuring out mistakes in code.
    // All data that flows into our application comes in form of actions.
    // Actions are just plain JavaScript objects describing “what happened”.
    // Think of them as newspapers.
    if (type !== ActionTypes.LOGGER_APPEND) {
      if (action.error) {
        console.error(type, action);
      } else {
        console.info(type, action);
      }
    }
  }

  flux.dispatch({ type, ...action });
  
  if (action.error) {
    return Promise.reject(action.error);
  }

  return Promise.resolve(action.response ? action.response : action);
}

const logError = console.error.bind(console);

/**
 * Dispatches three actions for an async operation represented by promise.
 */
export function dispatchAsync(promise, types, action = {}) {
  const { request, success, failure } = types;

  dispatch(request, action);
  return promise.then(
    response => dispatch(success, { ...action, response }),
    error => dispatch(failure, { ...action, error })
  ).catch(logError);
}

export default flux;
