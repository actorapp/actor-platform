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
import Login_react from '../components/Login.react';

var zhName="";
var nickName="";
var ip="";
var password="";
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
    }, {phone});
  }

  requestSms(phone) {
    dispatchAsync(ActorClient.requestSms(phone), {
      request: ActionTypes.AUTH_CODE_REQUEST,
      success: ActionTypes.AUTH_CODE_REQUEST_SUCCESS,
      failure: ActionTypes.AUTH_CODE_REQUEST_FAILURE
    }, {phone});
  }
  requestUserName(userName) {
    dispatchAsync(ActorClient.requestUserName(userName), {
      request: ActionTypes.AUTH_CODE_REQUEST,
      success: ActionTypes.AUTH_CODE_REQUEST_SUCCESS,
      failure: ActionTypes.AUTH_CODE_REQUEST_FAILURE
    }, {userName});
  },
  requestSignUp(nickName, name, ip){
    const requestSignUp = () =>
      dispatchAsync(ActorClient.requestSignUp(nickName, name, ip), {
        request: ActionTypes.AUTH_CODE_REQUEST,
        success: ActionTypes.AUTH_CODE_REQUEST_SUCCESS,
        failure: ActionTypes.AUTH_CODE_REQUEST_FAILURE
      }, {nickName, name, ip});

    const handleState = (state) =>
      {
        switch (state) {
          case 'start':
            LoginActionCreators.requestWebSyncUser(ip,nickName);
            break;
          default:
            console.error('Unsupported state', state);
            break;
        }
      }
      ;

    requestSignUp()
      .then(handleState);
  },


  requestWebSignUp(ip,nickName){
    const methodName = "isUserNeedSignUp";
    this.nickName = nickName;
    this.ip = ip;
    let json = "username="+nickName;//得到的JSON
    const requestWebSignUp = () =>
      dispatchAsync(ActorClient.requestWebSignUp(ip, methodName, json, nickName), {
        request: ActionTypes.AUTH_CODE_REQUEST,
        success: ActionTypes.AUTH_CODE_REQUEST_SUCCESS,
        failure: ActionTypes.AUTH_CODE_REQUEST_FAILURE
      }, {ip, methodName, json, nickName});
    const handleState = (response) =>
    {
      if(response != null){
        var state  = response.next;
        var rename  = response.name;
        zhName = rename;
        switch (state) {
          case 'signup':
            // LoginActionCreators.requestSignUp(nickName, rename, ip);
            LoginActionCreators.requestUserName(nickName);
            break;
          case 'login':
            LoginActionCreators.requestUserName(nickName);
            break;
          default:
            console.error('Unsupported state', state);
            break;
        }
      }
    };

    requestWebSignUp()
      .then(handleState);
  },

  requestWebSyncUser(ip,nickName){
    let json = "oaUserName=" + nickName;//得到的JSON
    const methodName="syncUser";
    const requestWebSyncUser = () =>
      dispatchAsync(ActorClient.requestWebSyncUser(ip, methodName, json), {
        request: ActionTypes.AUTH_CODE_SEND,
        success: ActionTypes.AUTH_CODE_SEND_SUCCESS,
        failure: ActionTypes.AUTH_CODE_SEND_FAILURE
      }, {ip, methodName, json});
    const handleState = (response) =>
    {
      var result = response.result;
      if(result){
        LoginActionCreators.sendPassword("","",this.password);
      }else{
        console.error('Unsupported state', response);
      }
    };

    requestWebSyncUser()
      .then(handleState);
  },

  requestWebValidatePassword(ip,json,password){
    const methodName="validatePassword";
    const requestWebValidatePassword = () =>
      dispatchAsync(ActorClient.requestWebValidatePassword(ip, methodName, json), {
        request: ActionTypes.AUTH_CODE_SEND,
        success: ActionTypes.AUTH_CODE_SEND_SUCCESS,
        failure: ActionTypes.AUTH_CODE_SEND_FAILURE
      }, {ip, methodName, json});
    const handleState = (response) =>
    {
      var result = response.result;
      if(result){
        this.password = password;
        LoginActionCreators.sendSignupForPassword(zhName,"11111111");
        // LoginActionCreators.sendPassword(password);
      }else{
        console.error('Unsupported state', response);
      }
    };

    requestWebValidatePassword()
      .then(handleState);
  },

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
          // this.startSignup();
          LoginActionCreators.requestWebValidatePassword(ip,json,password);
          break;
        case 'logged_in':
          this.setLoggedIn({ redirect: true });
          break;
        default:
          console.error('Unsupported state', state);
      }
    });
  }
    sendPasswordPromise()
      .then(handleState);
  },

  sendSignupForPassword(name, password) {
    const signUpPromise = () =>
      dispatchAsync(ActorClient.signUp(name, password), {
        request: ActionTypes.AUTH_CODE_SEND,
        success: ActionTypes.AUTH_CODE_SEND_SUCCESS,
        failure: ActionTypes.AUTH_CODE_SEND_FAILURE
      }, {name, password});
    const setLoggedIn = () =>
    {
      LoginActionCreators.requestWebSyncUser(this.ip,this.nickName);
    };
    signUpPromise()
      .then(setLoggedIn)
  },
  startSignup() {
    dispatch(ActionTypes.AUTH_SIGNUP_START);
  },

  sendSignup(name) {
    const signUpPromise = () =>
      dispatchAsync(ActorClient.signUp(name), {
        request: ActionTypes.AUTH_SIGNUP,
        success: ActionTypes.AUTH_SIGNUP_SUCCESS,
        failure: ActionTypes.AUTH_SIGNUP_FAILURE
      }, {name,password});

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
