import ActorClient from '../utils/ActorClient';

import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

const LoginActionCreators = {
  requestSms: (phone) => {
    ActorClient.requestSms(
      phone,
      () => {
        Dispatcher.dispatch({
          type: ActionTypes.AUTH_SMS_REQUEST_SUCCESS
        });
      },
      (error) => {
        Dispatcher.dispatch({
          type: ActionTypes.AUTH_SMS_REQUEST_FAILURE,
          error: error
        });
      });
  },

  sendCode: (router, code) => {
    ActorClient.sendCode(code,
      (state) => {
        switch (state) {
          case 'signup':
            Dispatcher.dispatch({
              type: ActionTypes.SEND_CODE_SUCCESS,
              needSignup: true
            });

            break;
          case 'logged_in':
            Dispatcher.dispatch({
              type: ActionTypes.SEND_CODE_SUCCESS,
              needSignup: false
            });

            LoginActionCreators.setLoggedIn(router, {redirect: true});

            break;
          default:
            console.error('Unsupported state', state);
        }
      },
      (error) => {
        Dispatcher.dispatch({
          type: ActionTypes.SEND_CODE_FAILURE,
          error: error
        });
      });
  },

  sendSignup: (router, name) => {
    ActorClient.signUp(
      name,
      () => {
        Dispatcher.dispatch({
          type: ActionTypes.SEND_SIGNUP_SUCCESS
        });

        LoginActionCreators.setLoggedIn(router, {redirect: true});
      },
      (error) => {
        Dispatcher.dispatch({
          type: ActionTypes.SEND_SIGNUP_FAILURE,
          error: error
        });
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

    Dispatcher.dispatch({
      type: ActionTypes.SET_LOGGED_IN
    });
  },

  setLoggedOut: () => {
    Dispatcher.dispatch({
      type: ActionTypes.SET_LOGGED_OUT
    });
  },

  wrongNumberClick: () => {
    Dispatcher.dispatch({
      type: ActionTypes.AUTH_WRONG_NUMBER_CLICK
    });
  }
};

export default LoginActionCreators;
