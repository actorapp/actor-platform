/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { dispatch, dispatchAsync } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

import ActorClient from '../utils/ActorClient';
import history from '../utils/history';
import DelegateContainer from '../utils/DelegateContainer';
import LocationContainer from '../utils/LocationContainer';

import MyProfileActionCreators from './MyProfileActionCreators';
import DialogActionCreators from './DialogActionCreators';
import ContactActionCreators from './ContactActionCreators';
import QuickSearchActionCreators from './QuickSearchActionCreators';
import FaviconActionCreators from './FaviconActionCreators';
import EventBusActionCreators from './EventBusActionCreators';

const LoginActionCreators = {
  changeLogin(login) {
    dispatch(ActionTypes.AUTH_CHANGE_LOGIN, { login })
  },

  changeCode(code) {
    dispatch(ActionTypes.AUTH_CHANGE_CODE, { code })
  },

  changeName(name) {
    dispatch(ActionTypes.AUTH_CHANGE_NAME, { name })
  },

  requestCode(phone) {
    const isEmail = /@/.test(phone);
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
  },

  requestSms(phone) {
    dispatchAsync(ActorClient.requestSms(phone), {
      request: ActionTypes.AUTH_CODE_REQUEST,
      success: ActionTypes.AUTH_CODE_REQUEST_SUCCESS,
      failure: ActionTypes.AUTH_CODE_REQUEST_FAILURE
    }, { phone });
  },
  requestUserName(userName) {
          dispatchAsync(ActorClient.requestUserName(userName), {
                        request: ActionTypes.AUTH_CODE_REQUEST,
                        success: ActionTypes.AUTH_CODE_REQUEST_SUCCESS,
                        failure: ActionTypes.AUTH_CODE_REQUEST_FAILURE
                        }, { userName });
  },


  sendCode(code) {
    const sendCodePromise = () => dispatchAsync(ActorClient.sendCode(code), {
      request: ActionTypes.AUTH_CODE_SEND,
      success: ActionTypes.AUTH_CODE_SEND_SUCCESS,
      failure: ActionTypes.AUTH_CODE_SEND_FAILURE
    }, { code });
    const handleState = (state) => {
      switch (state) {
        case 'signup':
          this.startSignup();
          break;
        case 'logged_in':
          this.setLoggedIn({redirect: true});
          break;
        default:
          console.error('Unsupported state', state);
      }
    };

    sendCodePromise()
      .then(handleState);
  },
    
    sendPassword(password) {
        const sendPasswordPromise = () => dispatchAsync(ActorClient.sendPassword(password), {
                                                        request: ActionTypes.AUTH_CODE_SEND,
                                                        success: ActionTypes.AUTH_CODE_SEND_SUCCESS,
                                                        failure: ActionTypes.AUTH_CODE_SEND_FAILURE
                                                        }, { password });
        
        const handleState = (state) => {
            switch (state) {
                case 'signup':
                    this.startSignup();
                    break;
                case 'logged_in':
                    this.setLoggedIn({redirect: true});
                    break;
                default:
                    console.error('Unsupported state', state);
            }
        };
        
        sendPasswordPromise()
        .then(handleState);
    },
    
    sendSignup(name,password) {
        const signUpPromise = () => dispatchAsync(ActorClient.signUp(name,password), {
                                                  request: ActionTypes.AUTH_SIGNUP,
                                                  success: ActionTypes.AUTH_SIGNUP_SUCCESS,
                                                  failure: ActionTypes.AUTH_SIGNUP_FAILURE
                                                  }, { name,password });
        
        const setLoggedIn = () => this.setLoggedIn({redirect: true});
        
        signUpPromise()
        .then(setLoggedIn)
    },

  startSignup() {
    dispatch(ActionTypes.AUTH_SIGNUP_START);
  },

  sendSignup(name) {
    const signUpPromise = () => dispatchAsync(ActorClient.signUp(name), {
      request: ActionTypes.AUTH_SIGNUP,
      success: ActionTypes.AUTH_SIGNUP_SUCCESS,
      failure: ActionTypes.AUTH_SIGNUP_FAILURE
    }, { name });

    const setLoggedIn = () => this.setLoggedIn({redirect: true});

    signUpPromise()
      .then(setLoggedIn)
  },

  setLoggedIn(opts = {}) {
    const delegate = DelegateContainer.get();

    if (delegate.actions.setLoggedIn) {
      delegate.actions.setLoggedIn(opts);
    } else {
      if (opts.redirect) {
        const location = LocationContainer.get();
        const nextPathname = location.state ? location.state.nextPathname : null;

        if (nextPathname) {
          history.replace(nextPathname);
        } else {
          history.replace('/');
        }
      }

      ActorClient.bindUser(ActorClient.getUid(), MyProfileActionCreators.onProfileChanged);
      // ActorClient.bindDialogs(DialogActionCreators.setDialogs);
      ActorClient.bindGroupDialogs(DialogActionCreators.setDialogs);
      ActorClient.bindContacts(ContactActionCreators.setContacts);
      ActorClient.bindSearch(QuickSearchActionCreators.setQuickSearchList);
      ActorClient.bindTempGlobalCounter(FaviconActionCreators.setFavicon);
      ActorClient.bindEventBus(EventBusActionCreators.broadcastEvent);
      dispatch(ActionTypes.AUTH_SET_LOGGED_IN);
    }
  },

  setLoggedOut() {
    const delegate = DelegateContainer.get();

    if (delegate.actions.setLoggedOut) {
      delegate.actions.setLoggedOut();
    } else {
      ActorClient.unbindUser(ActorClient.getUid(), MyProfileActionCreators.onProfileChanged);
      ActorClient.unbindDialogs(DialogActionCreators.setDialogs);
      // ActorClient.unbindContacts(ContactActionCreators.setContacts);
      ActorClient.unbindGroupDialogs(DialogActionCreators.setDialogs);
      ActorClient.unbindSearch(QuickSearchActionCreators.setQuickSearchList);
      ActorClient.unbindTempGlobalCounter(FaviconActionCreators.setFavicon);
      ActorClient.unbindEventBus(EventBusActionCreators.broadcastEvent);
      dispatch(ActionTypes.AUTH_SET_LOGGED_OUT);
    }
  },

  restartAuth() {
    dispatch(ActionTypes.AUTH_RESTART)
  }
};

export default LoginActionCreators;
