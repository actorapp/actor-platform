import ActorClient from 'utils/ActorClient';
import Raven from 'utils/Raven';
import mixpanel from 'utils/Mixpanel';

import ActorAppDispatcher from 'dispatcher/ActorAppDispatcher';
import { ActionTypes, AuthSteps } from 'constants/ActorAppConstants';

import { EventEmitter } from 'events';
import assign from 'object-assign';

const CHANGE_EVENT = 'change';

let errors = {
  phone: null,
  code: null,
  signup: null
};

let step = AuthSteps.PHONE_WAIT;

let isSmsRequested = false,
    isSignupStarted = false,
    myUid = null;

var LoginStore = assign({}, EventEmitter.prototype, {
  isLoggedIn: function () {
    return (ActorClient.isLoggedIn());
  },

  getStep: () => step,

  getErrors: () => errors,

  isSmsRequested: function () {
    return (isSmsRequested);
  },

  isSignupStarted: function () {
    return (isSignupStarted);
  },

  emitChange: function () {
    this.emit(CHANGE_EVENT);
  },

  addChangeListener: function (callback) {
    this.on(CHANGE_EVENT, callback);
  },

  removeChangeListener: function (callback) {
    this.removeListener(CHANGE_EVENT, callback);
  },

  getMyId: function () {
    return (myUid);
  }
});

const processPhoneExpired = () => {
  errors.name = null;
  errors.phone = 'Phone code is expired';
  step = AuthSteps.PHONE_WAIT;
  LoginStore.emitChange();
};

LoginStore.dispatchToken = ActorAppDispatcher.register(function (action) {
  switch (action.type) {
    case ActionTypes.AUTH_SMS_REQUEST_SUCCESS:
      errors.phone = null;
      step = AuthSteps.CODE_WAIT;
      mixpanel.track('Request SMS');
      LoginStore.emitChange();
      break;
    case ActionTypes.AUTH_SMS_REQUEST_FAILURE:
      switch (action.error) {
        case 'PHONE_NUMBER_INVALID':
          errors.phone = 'Invalid number';
          break;
        default:
          errors.phone = action.error;
      }

      LoginStore.emitChange();
      break;
    case ActionTypes.SEND_CODE_SUCCESS:
      errors.code = null;

      if (action.needSignup) {
        step = AuthSteps.SIGNUP_NAME_WAIT;
      } else {
        mixpanel.track('Successful login');
      }

      LoginStore.emitChange();
      break;
    case ActionTypes.SEND_CODE_FAILURE:
      switch (action.error) {
        case 'PHONE_CODE_INVALID':
          errors.code = 'Invalid code';
          mixpanel.track('Invalid code');
          break;
        case 'PHONE_CODE_EXPIRED':
          processPhoneExpired();
          break;
        default:
          errors.code = action.error;
      }

      LoginStore.emitChange();
      break;
    case ActionTypes.SEND_SIGNUP_SUCCESS:
      errors.name = null;
      step = AuthSteps.COMPLETED;
      mixpanel.track('Sign up');
      mixpanel.alias(ActorClient.getUid());
      mixpanel.people.set_once({$created: new Date()});
      LoginStore.emitChange();
      break;
    case ActionTypes.SEND_SIGNUP_FAILURE:
      switch (action.error) {
        case 'NAME_INVALID':
          errors.signup = 'Invalid name';
          break;
        case 'PHONE_CODE_EXPIRED':
          errors.signup = null;
          processPhoneExpired();
          break;
        default:
          errors.name = action.error;
      }
      LoginStore.emitChange();
      break;
    case ActionTypes.AUTH_WRONG_NUMBER_CLICK:
      errors.phone = null;
      step = AuthSteps.PHONE_WAIT;
      LoginStore.emitChange();
      break;
    case ActionTypes.SET_LOGGED_IN:
      myUid = ActorClient.getUid();
      const user = ActorClient.getUser(myUid);
      Raven.setUserContext({id: myUid});
      mixpanel.identify(myUid);
      mixpanel.people.set({
        $phone: user.phones[0],
        $name: user.name
      });
      LoginStore.emitChange();
      break;
    case ActionTypes.SET_LOGGED_OUT:
      Raven.setUserContext();
      mixpanel.track('Log out');
      mixpanel.cookie.clear();
      localStorage.clear();
      location.reload();
      break;
    default:
  }
});

export default LoginStore;
