/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import 'babel-polyfill';
import '../utils/intl-polyfill';

import Actor from 'actor-js';
import DelegateContainer from '../utils/DelegateContainer';
import SharedContainer from '../utils/SharedContainer';
import SDKDelegate from './actor-sdk-delegate';
import { endpoints, rootElement, homePage, twitter, helpPhone, appName } from '../constants/ActorAppConstants'
import Pace from 'pace';

import React from 'react';
import { render } from 'react-dom';
import { Router, Route, IndexRoute } from 'react-router';
import history from '../utils/history';
import { IntlProvider } from 'react-intl';
import crosstab from 'crosstab';
import { lightbox } from '../utils/ImageUtils'

import LoginActionCreators from '../actions/LoginActionCreators';

import LoginStore from '../stores/LoginStore';

import App from '../components/App.react';
import Main from '../components/Main.react';
import DefaultLogin from '../components/Login.react';
import DefaultDeactivated from '../components/Deactivated.react';
import DefaultJoin from '../components/Join.react';
import DefaultInstall from '../components/Install.react';
import Modal from 'react-modal';

import { extendL18n, getIntlData } from '../l18n';

const ACTOR_INIT_EVENT = 'INIT';

// Init app loading progressbar
Pace.start({
  ajax: false,
  restartOnRequestAfter: false,
  restartOnPushState: false
});

// Init lightbox
lightbox.load({
  animation: false,
  controlClose: '<i class="material-icons">close</i>'
});

window.isJsAppLoaded = false;
window.jsAppLoaded = () => window.isJsAppLoaded = true;

/**
 * Class represents ActorSKD itself
 *
 * @param {object} options - Object contains custom components, actions and localisation strings.
 */
class ActorSDK {
  constructor(options = {}) {
    this.endpoints = (options.endpoints && options.endpoints.length > 0) ? options.endpoints : endpoints;
    this.isExperimental = options.isExperimental ? options.isExperimental : false;
    this.forceLocale = options.forceLocale ? options.forceLocale : null;
    this.rootElement = options.rootElement ? options.rootElement : rootElement;
    this.homePage = options.homePage ? options.homePage : homePage;
    this.twitter = options.twitter ? options.twitter : twitter;
    this.helpPhone = options.helpPhone ? options.helpPhone : helpPhone;
    this.appName = options.appName ? options.appName : appName;
    this.delegate = options.delegate ? options.delegate : new SDKDelegate();

    DelegateContainer.set(this.delegate);

    if (this.delegate.l18n) extendL18n();

    SharedContainer.set(this);
  }

  _starter = () => {
    if (crosstab.supported) {
      crosstab.on(ACTOR_INIT_EVENT, (msg) => {
        if (msg.origin !== crosstab.id && window.location.hash !== '#/deactivated') {
          history.push('deactivated');
        }
      });
    }

    const appRootElemet = document.getElementById(this.rootElement);

    if (window.location.hash !== '#/deactivated') {
      if (crosstab.supported) crosstab.broadcast(ACTOR_INIT_EVENT, {});
      window.messenger = Actor.create(this.endpoints);
    }

    const Login = this.delegate.components.login || DefaultLogin;
    const Deactivated = this.delegate.components.deactivated || DefaultDeactivated;
    const Install = this.delegate.components.install || DefaultInstall;
    const Join = this.delegate.components.join || DefaultJoin;
    const intlData = getIntlData(this.forceLocale);

    const requireAuth = (nextState, replaceState) => {
      if (!LoginStore.isLoggedIn()) {
        replaceState({
          pathname: '/auth',
          state: {nextPathname: nextState.location.pathname}
        })
      }
    };

    /**
     * Method for pulling props to router components
     *
     * @param RoutedComponent component for extending
     * @param props props to extend
     * @returns {object} extended component
     */
    const createElement = (Component, props) => {
      return <Component {...props} delegate={this.delegate} isExperimental={this.isExperimental}/>;
    };

    const root = (
      <IntlProvider {...intlData}>
        <Router history={history} createElement={createElement}>
          <Route path="/" component={App}>
            <Route path="auth" component={Login}/>
            <Route path="deactivated" component={Deactivated}/>
            <Route path="install" component={Install}/>
            <Route path="join/:token" component={Join}/>

            <Route path="im/:id" component={Main}/>

            <IndexRoute component={Main} onEnter={requireAuth}/>
          </Route>
        </Router>
      </IntlProvider>
    );

    render(root, appRootElemet);

    // initial setup fot react modal
    Modal.setAppElement(appRootElemet);

    if (window.location.hash !== '#/deactivated') {
      if (LoginStore.isLoggedIn()) LoginActionCreators.setLoggedIn({redirect: false});
    }
  };

  /**
   * Start application
   */
  startApp() {
    if (window.isJsAppLoaded) {
      this._starter();
    } else {
      window.jsAppLoaded = this._starter;
    }
  }
}

export default ActorSDK;
