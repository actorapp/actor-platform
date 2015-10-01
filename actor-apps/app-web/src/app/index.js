/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import 'babel/polyfill';
import RouterContainer from 'utils/RouterContainer';

import crosstab from 'crosstab';

import React, { Component } from 'react';
import Router from 'react-router';
import ReactMixin from 'react-mixin';

import { intlData } from 'l18n';
import { IntlMixin } from 'react-intl';

import Raven from 'utils/Raven'; // eslint-disable-line
import isMobile from 'utils/IsMobile';

import { endpoints } from 'constants/ActorAppConstants'

import LoginActionCreators from 'actions/LoginActionCreators';

import LoginStore from 'stores/LoginStore';
import PreferencesStore from 'stores/PreferencesStore';

import Deactivated from 'components/Deactivated.react';
import Login from 'components/Login.react';
import Main from 'components/Main.react';
import JoinGroup from 'components/JoinGroup.react';
import Install from 'components/Install.react';
//import AppCache from 'utils/AppCache'; // eslint-disable-line

// Loading progress
import Pace from 'pace';
Pace.start({
  ajax: false,
  restartOnRequestAfter: false,
  restartOnPushState: false
});

// Preload emoji spritesheet
import { preloadEmojiSheet } from 'utils/EmojiUtils'
preloadEmojiSheet();

const { DefaultRoute, Route, RouteHandler } = Router;

const ActorInitEvent = 'concurrentActorInit';

if (crosstab.supported) {
  crosstab.on(ActorInitEvent, (msg) => {
    if (msg.origin !== crosstab.id && window.location.hash !== '#/deactivated') {
      window.location.assign('#/deactivated');
      window.location.reload();
    }
  });
}

// Check for mobile device, and force users to install native apps.
if (isMobile() && window.location.hash !== '#/install') {
  window.location.assign('#/install');
  document.body.classList.add('overflow');
} else if (window.location.hash === '#/install') {
  window.location.assign('/');
}

@ReactMixin.decorate(IntlMixin)
class App extends Component {
  render() {
    return <RouteHandler/>;
  }
}

const initReact = () => {
  const appRootElemet = document.getElementById('actor-web-app');

  if (window.location.hash !== '#/deactivated') {
    if (crosstab.supported) {
      crosstab.broadcast(ActorInitEvent, {});
    }

    if (location.pathname === '/app/index.html') {
      window.messenger = new window.actor.ActorApp(['ws://' + location.hostname + ':9080/']);
    } else {
      window.messenger = new window.actor.ActorApp(endpoints);
    }
  }

  const routes = (
    <Route handler={App} name="app" path="/">
      <Route handler={Main} name="main" path="/im/:id"/>
      <Route handler={JoinGroup} name="join" path="/join/:token"/>
      <Route handler={Login} name="login" path="/auth"/>
      <Route handler={Deactivated} name="deactivated" path="/deactivated"/>
      <Route handler={Install} name="install" path="/install"/>
      <DefaultRoute handler={Main}/>
    </Route>
  );

  const router = Router.create(routes, Router.HashLocation);

  RouterContainer.set(router);

  router.run((Root) => React.render(<Root {...intlData}/>, appRootElemet));

  if (window.location.hash !== '#/deactivated') {
    if (LoginStore.isLoggedIn()) {
      LoginActionCreators.setLoggedIn(router, {redirect: false});
    }
  }
};

window.jsAppLoaded = () => setTimeout(initReact, 0);
