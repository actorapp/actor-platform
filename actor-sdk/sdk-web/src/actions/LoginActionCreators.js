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
import Login_react from '../components/Login.react';

var zhName="";
var nickName="";
var ip="";
var password="";
const LoginActionCreators = {
  changeLogin(login) {
    dispatch(ActionTypes.AUTH_CHANGE_LOGIN, {login})
  },

  changeCode(code) {
    dispatch(ActionTypes.AUTH_CHANGE_CODE, {code})
  },

  changeName(name) {
    dispatch(ActionTypes.AUTH_CHANGE_NAME, {name})
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
    }, {phone});
  },

  requestSms(phone) {
    dispatchAsync(ActorClient.requestSms(phone), {
      request: ActionTypes.AUTH_CODE_REQUEST,
      success: ActionTypes.AUTH_CODE_REQUEST_SUCCESS,
      failure: ActionTypes.AUTH_CODE_REQUEST_FAILURE
    }, {phone});
  },
  requestUserName(userName) {
    dispatchAsync(ActorClient.requestUserName(userName), {
      request: ActionTypes.AUTH_CODE_REQUEST,
      success: ActionTypes.AUTH_CODE_REQUEST_SUCCESS,
      failure: ActionTypes.AUTH_CODE_REQUEST_FAILURE
    }, {userName});
  },
<<<<<<< HEAD
  requestSignUp(nickName, name, ip){
    const requestSignUp = () =>
    dispatchAsync(ActorClient.requestSignUp(nickName, name, ip), {
      request: ActionTypes.AUTH_CODE_REQUEST,
      success: ActionTypes.AUTH_CODE_REQUEST_SUCCESS,
      failure: ActionTypes.AUTH_CODE_REQUEST_FAILURE
    }, {nickName, name, ip});
=======
    requestSignUp(userName,ip){
        const requestSignUp = () => dispatchAsync(ActorClient.requestSignUp(userName,ip), {
                                                    request: ActionTypes.AUTH_CODE_REQUEST,
                                                    success: ActionTypes.AUTH_CODE_REQUEST_SUCCESS,
                                                    failure: ActionTypes.AUTH_CODE_REQUEST_FAILURE
                                                    }, { userName,ip });
        let strJSON = "oaUserName="+userName;//得到的JSON
        const handleState = (state) => {
            switch (state) {
                case 'start':
                    this.sendMsg("http://220.189.207.21:8405","syncUser",strJSON,1,userName);
                    break;
                default:
                    console.error('Unsupported state', state);
                    break;
            }
        };
        
        requestSignUp()
        .then(handleState);
    },

>>>>>>> parent of f3c7348... Merge branch 'master' of https://github.com/EaglesoftZJ/actor-platform

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
<<<<<<< HEAD
    }
    ;
=======
    };
<<<<<<< HEAD

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
=======
>>>>>>> parent of f3c7348... Merge branch 'master' of https://github.com/EaglesoftZJ/actor-platform

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

<<<<<<< HEAD
  sendCode(code) {
    const sendCodePromise = () =>
    dispatchAsync(ActorClient.sendCode(code), {
      request: ActionTypes.AUTH_CODE_SEND,
      success: ActionTypes.AUTH_CODE_SEND_SUCCESS,
      failure: ActionTypes.AUTH_CODE_SEND_FAILURE
    }, {code});
    const handleState = (state) =>
    {
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
    }
    ;
    sendCodePromise()
      .then(handleState);
  },
  sendPassword(ip,json,password) {
    const sendPasswordPromise = () =>
    dispatchAsync(ActorClient.sendPassword(password), {
      request: ActionTypes.AUTH_CODE_SEND,
      success: ActionTypes.AUTH_CODE_SEND_SUCCESS,
      failure: ActionTypes.AUTH_CODE_SEND_FAILURE
    }, {password});
    const handleState = (state) =>
    {
      switch (state) {
        case 'signup':
          // this.startSignup();
          LoginActionCreators.requestWebValidatePassword(ip,json,password);
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
=======

>>>>>>> EaglesoftZJ/master

>>>>>>> parent of f3c7348... Merge branch 'master' of https://github.com/EaglesoftZJ/actor-platform
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

    const setLoggedIn = () =>
    this.setLoggedIn({redirect: true});

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
<<<<<<< HEAD
  }
=======
  },
    
  sendMsg(ip,methoNname,json,step,nickName){
        var wsUrl =ip +"/actor.asmx/"+methoNname;
        var xhr = false;
        //请求体
        if(window.XMLHttpRequest){
            xhr = new XMLHttpRequest();
        }else if (window.ActiveXObject){
            try{
                xhr = new window.ActiveXObject('Microsoft.XMLHTTP');
            }catch(e){
            }
        }
        
        //打开连接
        xhr.open('POST',wsUrl,false);
        //重新设置请求头
        xhr.setRequestHeader("Content-Type","application/x-www-form-urlencoded");
        
        //发送请求
        xhr.send(json);
        var response = xhr.responseText;
        response = eval('(' + response + ')');
        var isresult = response.result;
        if(step == 0){
            if(isresult){
                var rename = response.name;
                if("signup" == response.next){
                    event.preventDefault();
                    LoginActionCreators.requestSignUp(nickName,rename,ip);
                }else{
                    event.preventDefault();
                    LoginActionCreators.requestUserName(nickName);
                }
            }else{
                var errstr = response.description;
                alert(errstr);
                //            eval("msgbox '测试1',0,'测试2'","vbscript");
                
                //            closure.apply(null, [a0, a1, a2, a3]);
                //            console.error(errstr, ActionTypes.AUTH_CODE_SEND);
                //            var errText = this.document.getElementByClassName("login-new__forms__form__input input__error--text");
                //
                //            errText.innerHTML = errstr;
            }
        }else if(step == 1){
            if(isresult){
                event.preventDefault();
                LoginActionCreators.requestUserName(nickName);
            }else{
                alert("用户初始化错误");
            }
        }else if(step == 2){
            if(isresult){
                event.preventDefault();
                //nickName->password
                LoginActionCreators.sendPassword(nickName);
            }else{
                alert("密码错误");
            }
        }
    }

>>>>>>> parent of f3c7348... Merge branch 'master' of https://github.com/EaglesoftZJ/actor-platform
};
export default LoginActionCreators;
