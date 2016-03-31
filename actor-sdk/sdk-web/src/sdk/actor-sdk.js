/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import 'babel-polyfill';
import 'setimmediate';
import 'intl';

import Actor from 'actor-js';
import DelegateContainer from '../utils/DelegateContainer';
import SharedContainer from '../utils/SharedContainer';
import SDKDelegate from './actor-sdk-delegate';
import { endpoints, rootElement, homePage, twitter, helpPhone, appName } from '../constants/ActorAppConstants'
import Pace from 'pace';
import { isFunction } from 'lodash';

import React from 'react';
import { render } from 'react-dom';
import { Router, Route, IndexRoute, IndexRedirect } from 'react-router';
import history from '../utils/history';
import RouterHooks from '../utils/RouterHooks';
import { IntlProvider } from 'react-intl';
import crosstab from 'crosstab';
import { lightbox } from '../utils/ImageUtils'

import LoginActionCreators from '../actions/LoginActionCreators';
import {loggerAppend} from '../actions/LoggerActionCreators';
import defaultLogHandler from '../utils/defaultLogHandler';

import LoginStore from '../stores/LoginStore';

import App from '../components/App.react';
import Main from '../components/Main.react';
import DefaultLogin from '../components/Login.react';
import DefaultDeactivated from '../components/Deactivated.react';
import DefaultJoin from '../components/Join.react';
import DefaultInstall from '../components/Install.react';
import DefaultArchive from '../components/Archive.react';
import DefaultDialog from '../components/Dialog.react';
import DefaultEmpty from '../components/Empty.react';
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
    this.logHandler = isFunction(options.logHandler) ? options.logHandler : this.createLogHandler();
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

  createLogHandler() {
    if (localStorage.debug) {
      return loggerAppend;
    }

    return defaultLogHandler;
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
      window.messenger = Actor.create({
        endpoints: this.endpoints,
        logHandler: this.logHandler
      });
    }

    const Login = (typeof this.delegate.components.login == 'function') ? this.delegate.components.login : DefaultLogin;
    const Deactivated = (typeof this.delegate.components.deactivated == 'function') ? this.delegate.components.deactivated : DefaultDeactivated;
    const Install = (typeof this.delegate.components.install == 'function') ? this.delegate.components.install : DefaultInstall;
    const Archive = (typeof this.delegate.components.archive == 'function') ? this.delegate.components.archive : DefaultArchive;
    const Join = (typeof this.delegate.components.join == 'function') ? this.delegate.components.join : DefaultJoin;
    const Empty = (typeof this.delegate.components.empty == 'function') ? this.delegate.components.empty : DefaultEmpty;
    const Dialog = (typeof this.delegate.components.dialog == 'function') ? this.delegate.components.dialog : DefaultDialog;
    const intlData = getIntlData(this.forceLocale);

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
            <Route path="join/:token" component={Join} onEnter={RouterHooks.requireAuth}/>

            <Route path="im" component={Main} onEnter={RouterHooks.requireAuth}>
              <Route path="archive" component={Archive}/>
              <Route path=":id" component={Dialog}/>
              <IndexRoute component={Empty}/>
            </Route>

            <IndexRedirect to="im"/>
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
