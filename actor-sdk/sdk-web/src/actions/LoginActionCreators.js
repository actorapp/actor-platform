/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { dispatch, dispatchAsync } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

import ActorClient from '../utils/ActorClient';
import history from '../utils/history';
import DelegateContainer from '../utils/DelegateContainer';
import LocationContainer from '../utils/LocationContainer';

import ActionCreators from './ActionCreators';
import JoinGroupActions from './JoinGroupActions';
import ProfileActionCreators from './ProfileActionCreators';
import DialogActionCreators from './DialogActionCreators';
import ContactActionCreators from './ContactActionCreators';
import QuickSearchActionCreators from './QuickSearchActionCreators';
import FaviconActionCreators from './FaviconActionCreators';
import EventBusActionCreators from './EventBusActionCreators';
import StickersActionCreators from './StickersActionCreators';

class LoginActionCreators extends ActionCreators {
  start() {
    dispatch(ActionTypes.AUTH_START);
  }

  changeLogin(login) {
    dispatch(ActionTypes.AUTH_CHANGE_LOGIN, { login });
  }

  changeCode(code) {
    dispatch(ActionTypes.AUTH_CHANGE_CODE, { code });
  }

  changeName(name) {
    dispatch(ActionTypes.AUTH_CHANGE_NAME, { name });
  }

  startSignup() {
    dispatch(ActionTypes.AUTH_SIGNUP_START);
  }

  restartAuth() {
    dispatch(ActionTypes.AUTH_RESTART);
  }

  requestCode(phone) {
    let promise;
    if (/@/.test(phone)) {
      promise = ActorClient.requestCodeEmail(phone);
    } else {
      promise = ActorClient.requestSms(phone);
    }

    dispatchAsync(promise, {
      request: ActionTypes.AUTH_CODE_REQUEST,
      success: ActionTypes.AUTH_CODE_REQUEST_SUCCESS,
      failure: ActionTypes.AUTH_CODE_REQUEST_FAILURE
    }, { phone });
  }

  requestSms(phone) {
    dispatchAsync(ActorClient.requestSms(phone), {
      request: ActionTypes.AUTH_CODE_REQUEST,
      success: ActionTypes.AUTH_CODE_REQUEST_SUCCESS,
      failure: ActionTypes.AUTH_CODE_REQUEST_FAILURE
    }, { phone });
  }

  sendCode(code) {
    dispatchAsync(ActorClient.sendCode(code), {
      request: ActionTypes.AUTH_CODE_SEND,
      success: ActionTypes.AUTH_CODE_SEND_SUCCESS,
      failure: ActionTypes.AUTH_CODE_SEND_FAILURE
    }, {
      code
    }).then((state) => {
      switch (state) {
        case 'signup':
          this.startSignup();
          break;
        case 'logged_in':
          this.setLoggedIn({ redirect: true });
          break;
        default:
          console.error('Unsupported state', state);
      }
    });
  }

  sendSignup(name) {
    const signUpPromise = () => dispatchAsync(ActorClient.signUp(name), {
      request: ActionTypes.AUTH_SIGNUP,
      success: ActionTypes.AUTH_SIGNUP_SUCCESS,
      failure: ActionTypes.AUTH_SIGNUP_FAILURE
    }, { name });

    const setLoggedIn = () => this.setLoggedIn({ redirect: true });

    signUpPromise()
      .then(setLoggedIn)
  }

  setLoggedIn(opts = {}) {
    const delegate = DelegateContainer.get();

    if (delegate.actions.setLoggedIn) {
      return delegate.actions.setLoggedIn(opts);
    }

    if (opts.redirect) {
      const location = LocationContainer.get();
      const nextPathname = location.state ? location.state.nextPathname : '/';

      history.replace(nextPathname);
    }

    this.setBindings('main', [
      ActorClient.bindUser(ActorClient.getUid(), ProfileActionCreators.setProfile),
      ActorClient.bindGroupDialogs(DialogActionCreators.setDialogs),
      ActorClient.bindContacts(ContactActionCreators.setContacts),
      ActorClient.bindSearch(QuickSearchActionCreators.setQuickSearchList),
      ActorClient.bindTempGlobalCounter(FaviconActionCreators.setFavicon),
      ActorClient.bindEventBus(EventBusActionCreators.broadcastEvent),
      ActorClient.bindStickers(StickersActionCreators.setStickers)
    ]);

    dispatch(ActionTypes.AUTH_SET_LOGGED_IN);

    JoinGroupActions.joinAfterLogin();
  }

  setLoggedOut() {
    const delegate = DelegateContainer.get();

    if (delegate.actions.setLoggedOut) {
      return delegate.actions.setLoggedOut();
    }

    this.removeBindings('main');

    dispatch(ActionTypes.AUTH_SET_LOGGED_OUT);
  }
}

export default new LoginActionCreators();
