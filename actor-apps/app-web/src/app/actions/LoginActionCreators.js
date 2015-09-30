/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import ActorClient from 'utils/ActorClient';
import { dispatch } from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';
import MyProfileActionCreators from 'actions/MyProfileActionCreators';

const LoginActionCreators = {
  requestSms: (phone) => {
    ActorClient.requestSms(
      phone,
      () => {
        dispatch(ActionTypes.AUTH_SMS_REQUEST_SUCCESS);
      },
      (error) => {
        dispatch(ActionTypes.AUTH_SMS_REQUEST_FAILURE, { error });
      }
    )
  },

  sendCode: (router, code) => {
    ActorClient.sendCode(code,
      (state) => {
        switch (state) {
          case 'signup':
            dispatch(ActionTypes.SEND_CODE_SUCCESS, { needSignup: true });
            break;
          case 'logged_in':
            dispatch(ActionTypes.SEND_CODE_SUCCESS, { needSignup: false });
            LoginActionCreators.setLoggedIn(router, {redirect: true});
            break;
          default:
            console.error('Unsupported state', state);
        }
      },
      (error) => {
        dispatch(ActionTypes.SEND_CODE_FAILURE, { error });
      });
  },

  sendSignup: (router, name) => {
    ActorClient.signUp(name,
      () => {
        dispatch(ActionTypes.SEND_SIGNUP_SUCCESS);
        LoginActionCreators.setLoggedIn(router, {redirect: true});
      },
      (error) => {
        dispatch(ActionTypes.SEND_SIGNUP_FAILURE, { error });
      });
  },

  setLoggedIn: (router, opts) => {
    opts = opts || {};

    if (opts.redirect) {
      var nextPath = router.getCurrentQuery().nextPath;

      if (nextPath) {
        router.replaceWith(nextPath);
      } else {
        router.replaceWith('/');
      }
    }

    dispatch(ActionTypes.SET_LOGGED_IN);
    ActorClient.bindUser(ActorClient.getUid(), MyProfileActionCreators.onProfileChanged);
  },

  setLoggedOut: () => {
    dispatch(ActionTypes.SET_LOGGED_OUT);
    ActorClient.unbindUser(ActorClient.getUid(), MyProfileActionCreators.onProfileChanged);
  },

  wrongNumberClick: () => dispatch(ActionTypes.AUTH_WRONG_NUMBER_CLICK)
};

export default LoginActionCreators;
