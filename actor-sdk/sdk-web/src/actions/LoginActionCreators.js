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


  ////////////////////////////////////////////////////////////

  requestNickName(userName) {
    const requestNickName = () => dispatchAsync(ActorClient.requestNickName(userName), {
      request: ActionTypes.AUTH_CODE_REQUEST,
      success: ActionTypes.AUTH_NICKNAME_REQUEST_SUCCESS,
      failure: ActionTypes.AUTH_CODE_REQUEST_FAILURE
    }, { userName });
    const handleState = (state) =>
    {
      switch (state) {
        case 'start':
          this.requestWebSignUp('http://220.189.207.21:8405', userName);
          break;
        default:
          console.error(ActionTypes.AUTH_CODE_REQUEST_FAILURE, state);
          break;
      }
    };

    requestNickName()
      .then(handleState);
  }

  sendPassword(ip,json,password) {
    const sendPasswordPromise = () =>
    dispatchAsync(ActorClient.sendPassword(password), {
      request: ActionTypes.AUTH_CODE_SEND,
      success: ActionTypes.AUTH_CODE_SEND_SUCCESS,
      failure: ActionTypes.AUTH_CODE_SEND_FAILURE
    }, { password });
    const handleState = (state) =>
    {
      switch (state) {
        case 'signup':
          // this.startSignup();
          this.requestWebValidatePassword(ip,json,password);
          break;
        case 'logged_in':
          this.setLoggedIn({ redirect: true });
          break;
        default:
          console.error('Unsupported state', state);
      }
    };
    sendPasswordPromise()
      .then(handleState);
  }
  requestSignUp(nickName, name, ip){
    const requestSignUp = () =>
    dispatchAsync(ActorClient.requestSignUp(nickName, name, ip), {
      request: ActionTypes.AUTH_CODE_REQUEST,
      success: ActionTypes.AUTH_CODE_REQUEST_SUCCESS,
      failure: ActionTypes.AUTH_CODE_REQUEST_FAILURE
    }, { nickName, name, ip });

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
    };

    requestSignUp()
      .then(handleState);
  }


  requestWebSignUp(ip,nickName){
    const methodName = 'isUserNeedSignUp';
    let json = 'username='+nickName;//
    const requestWebSignUp = () =>
    dispatchAsync(ActorClient.requestWebSignUp(ip, methodName, json), {
      request: ActionTypes.AUTH_CODE_REQUEST,
      success: ActionTypes.AUTH_CODE_REQUEST_SUCCESS,
      failure: ActionTypes.AUTH_CODE_REQUEST_FAILURE
    }, { ip, methodName, json });
    const handleState = (response) =>
    {
      if(response != null) {
        var rename  = response.name;
        zhName = rename;
      }
    };

    requestWebSignUp()
      .then(handleState);
  }

  requestWebSyncUser(ip,nickName){
    let json = 'oaUserName=' + nickName;
    const methodName='syncUser';
    const requestWebSyncUser = () =>
    dispatchAsync(ActorClient.requestWebSyncUser(ip, methodName, json), {
      request: ActionTypes.AUTH_CODE_SEND,
      success: ActionTypes.AUTH_CODE_SEND_SUCCESS,
      failure: ActionTypes.AUTH_CODE_SEND_FAILURE
    }, { ip, methodName, json });
    const handleState = (response) =>
    {
      var result = response.result;
      if(result){
        this.sendPassword('','',this.password);
       }else{
        console.error('Unsupported state', response);
       }
    };

    requestWebSyncUser()
      .then(handleState);
  }

  requestWebValidatePassword(ip,json,password){
    const methodName='validatePassword';
    const requestWebValidatePassword = () =>
    dispatchAsync(ActorClient.requestWebValidatePassword(ip, methodName, json), {
      request: ActionTypes.AUTH_CODE_SEND,
      success: ActionTypes.AUTH_CODE_SEND_SUCCESS,
      failure: ActionTypes.AUTH_CODE_SEND_FAILURE
    }, { ip, methodName, json });
    const handleState = (response) =>
    {
      var result = response.result;
      if(result){
        this.password = password;
        this.sendSignupForPassword(zhName,'11111111');
        // LoginActionCreators.sendPassword(password);
      }else{
        console.error('Unsupported state', response);
      }
    };

    requestWebValidatePassword()
      .then(handleState);
  }

  sendSignupForPassword(name, password) {
    const signUpPromise = () =>
    dispatchAsync(ActorClient.signUpForPassword(name, password), {
      request: ActionTypes.AUTH_CODE_SEND,
      success: ActionTypes.AUTH_CODE_SEND_SUCCESS,
      failure: ActionTypes.AUTH_CODE_SEND_FAILURE
    }, { name, password });
    const setLoggedIn = () =>
    {
      this.requestWebSyncUser(this.ip,this.nickName);
    };
    signUpPromise()
      .then(setLoggedIn)
  }
}
var zhName='';
export default new LoginActionCreators();
