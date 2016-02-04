/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import 'babel-polyfill';
import '../utils/intl-polyfill';
//import '../workers'

import RouterContainer from '../utils/RouterContainer';
import DelegateContainer from '../utils/DelegateContainer';
import SharedContainer from '../utils/SharedContainer';
import SDKDelegate from './actor-sdk-delegate';
import { endpoints, rootElement, homePage, twitter, helpPhone } from '../constants/ActorAppConstants'
import Pace from 'pace';

import React, { Component, PropTypes } from 'react';
import Router from 'react-router';
import ReactMixin from 'react-mixin';
import Actor from 'actor-js';

import { IntlMixin } from 'react-intl';
import crosstab from 'crosstab';
import { lightbox } from '../utils/ImageUtils'

import LoginActionCreators from '../actions/LoginActionCreators';

import LoginStore from '../stores/LoginStore';

import DefaultDeactivated from '../components/Deactivated.react';
import DefaultLogin from '../components/Login.react';
import Main from '../components/Main.react';
import DefaultJoinGroup from '../components/JoinGroup.react';
import DefaultInstall from '../components/Install.react';
import Modal from 'react-modal';

import { extendL18n, getIntlData } from '../l18n';

const { DefaultRoute, Route, RouteHandler } = Router;

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

class App extends Component {
  static childContextTypes =  {
    delegate: PropTypes.object,
    isExperimental: PropTypes.bool
  };

  static propTypes =  {
    delegate: PropTypes.object,
    isExperimental: PropTypes.bool
  };

  getChildContext() {
    return {
      delegate: this.props.delegate,
      isExperimental: this.props.isExperimental
    };
  }

  constructor(props) {
    super(props);
  }

  render() {
    return <RouteHandler/>;
  }
}

ReactMixin.onClass(App, IntlMixin);

/**
 * Class represents ActorSKD itself
 * @param {object} options - Object contains custom components, actions, localisation strings and etc.
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
    this.delegate = options.delegate ? options.delegate : new SDKDelegate();

    DelegateContainer.set(this.delegate);

    if (this.delegate.l18n) extendL18n();

    SharedContainer.set(this);
  }

  _starter = () => {
    const ActorInitEvent = 'concurrentActorInit';

    if (crosstab.supported) {
      crosstab.on(ActorInitEvent, (msg) => {
        if (msg.origin !== crosstab.id && window.location.hash !== '#/deactivated') {
          window.location.assign('#/deactivated');
          window.location.reload();
        }
      });
    }

    const appRootElemet = document.getElementById(this.rootElement);

    // initial setup fot react modal
    Modal.setAppElement(appRootElemet);

    if (window.location.hash !== '#/deactivated') {
      if (crosstab.supported) crosstab.broadcast(ActorInitEvent, {});
      window.messenger = Actor.create(this.endpoints);
    }

    const Login = this.delegate.components.login || DefaultLogin;
    const Deactivated = this.delegate.components.deactivated || DefaultDeactivated;
    const Install = this.delegate.components.install || DefaultInstall;
    const JoinGroup = this.delegate.components.joinGroup || DefaultJoinGroup;
    const intlData = getIntlData(this.forceLocale);

    const routes = (
      <Route handler={App} name="app" path="/">
        <Route handler={Login} name="login" path="/auth"/>

        <Route handler={Main} name="main" path="/im/:id"/>
        <Route handler={JoinGroup} name="join" path="/join/:token"/>
        <Route handler={Deactivated} name="deactivated" path="/deactivated"/>
        <Route handler={Install} name="install" path="/install"/>

        <DefaultRoute handler={Main}/>
      </Route>
    );

    const router = Router.create(routes, Router.HashLocation);

    RouterContainer.set(router);

    router.run((Root) => React.render(<Root {...intlData} delegate={this.delegate} isExperimental={this.isExperimental}/>, appRootElemet));

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
