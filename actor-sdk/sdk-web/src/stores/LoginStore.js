/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { Store } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes, AuthSteps } from '../constants/ActorAppConstants';

import ActorClient from '../utils/ActorClient';

import { getIntlData } from '../l18n';

let step = AuthSteps.LOGIN_WAIT,
    errors = {
      login: null,
      code: null,
      signup: null
    },
    login = '',
    code = '',
    name = '',
    isCodeRequested = false,
    isCodeSended = false,
    isSignupStarted = false,
    myUid = null;

class LoginStore extends Store {
  constructor(dispatcher) {
    super(dispatcher);

    // TODO: do not use intlData here. save error codes and send them to ui.
    this.intl = getIntlData();
  }

  getStep = () => step;
  getErrors = () => errors;
  getLogin = () => login;
  getCode = () => code;
  getName = () => name;
  isCodeRequested = () => isCodeRequested;
  isCodeSended = () => isCodeSended;
  isSignupStarted = () => isSignupStarted;
  getMyId = () => myUid;
  isLoggedIn = () => ActorClient.isLoggedIn();

  resetStore = () => {
    step = AuthSteps.LOGIN_WAIT;
    errors = {
      login: null,
      code: null,
      signup: null
    };
    login = code = name = '';
    isCodeRequested = isCodeSended = isSignupStarted = false;
    myUid = null;
  };

  __onDispatch(action) {
    switch (action.type) {

      case ActionTypes.AUTH_CHANGE_LOGIN:
        login = action.login;
        this.__emitChange();
        break;
      case ActionTypes.AUTH_CHANGE_CODE:
        code = action.code;
        this.__emitChange();
        break;
      case ActionTypes.AUTH_CHANGE_NAME:
        name = action.name;
        this.__emitChange();
        break;

      case ActionTypes.AUTH_CODE_REQUEST:
        isCodeRequested = true;
        this.__emitChange();
        break;
      case ActionTypes.AUTH_CODE_REQUEST_SUCCESS:
        step = AuthSteps.CODE_WAIT;
        errors.login = null;
        this.__emitChange();
        break;
      case ActionTypes.AUTH_CODE_REQUEST_FAILURE:
        switch (action.error) {
          case 'PHONE_NUMBER_INVALID':
            errors.login = this.intl.messages['login.errors.numberInvalid'];
            break;
          case 'CODE_WAIT':
            errors.login = this.intl.messages['login.errors.codeWait'];
            break;
          default:
            errors.login = action.error;
        }
        isCodeRequested = false;
        this.__emitChange();
        break;

      case ActionTypes.AUTH_CODE_SEND:
        isCodeSended = true;
        this.__emitChange();
        break;
      case ActionTypes.AUTH_CODE_SEND_SUCCESS:
        errors.code = null;
        this.__emitChange();
        break;
      case ActionTypes.AUTH_CODE_SEND_FAILURE:
        switch (action.error) {
          case 'PHONE_CODE_INVALID':
          case 'EMAIL_CODE_INVALID':
            errors.code = this.intl.messages['login.errors.codeInvalid'];
            break;
          case 'PHONE_CODE_EXPIRED':
            errors.code = this.intl.messages['login.errors.codeExpired'];
            break;
          default:
            errors.code = action.error;
        }
        isCodeSended = false;
        this.__emitChange();
        break;

      case ActionTypes.AUTH_SIGNUP_START:
        step = AuthSteps.NAME_WAIT;
        this.__emitChange();
        break;

      case ActionTypes.AUTH_SIGNUP:
        isSignupStarted = true;
        this.__emitChange();
        break;
      case ActionTypes.AUTH_SIGNUP_SUCCESS:
        errors.signup = null;
        this.__emitChange();
        break;
      case ActionTypes.AUTH_SIGNUP_FAILURE:
        switch (action.error) {
          case 'NAME_INVALID':
            errors.signup = this.intl.messages['login.errors.nameInvalid'];
            break;
          default:
            errors.signup = action.error;
        }
        isSignupStarted = false;
        this.__emitChange();
        break;

      case ActionTypes.AUTH_RESTART:
        this.resetStore();
        this.__emitChange();
        break;

      case ActionTypes.AUTH_SET_LOGGED_IN:
        myUid = ActorClient.getUid();
        this.__emitChange();
        break;
      case ActionTypes.AUTH_SET_LOGGED_OUT:
        localStorage.clear();
        location.reload();
        break;
      default:
    }
  }
}

export default new LoginStore(Dispatcher);
