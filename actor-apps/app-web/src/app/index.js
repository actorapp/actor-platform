/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import babelPolyfill from 'babel/polyfill'; // eslint-disable-line
import RouterContainer from 'utils/RouterContainer';

import crosstab from 'crosstab';

import React, { Component } from 'react';
import Router from 'react-router';
import Raven from 'utils/Raven'; // eslint-disable-line
import isMobile from 'utils/IsMobile';
import ReactMixin from 'react-mixin';

import { endpoints } from 'constants/ActorAppConstants'

import Intl from 'intl'; // eslint-disable-line
import LocaleData from 'intl/locale-data/jsonp/en-US'; // eslint-disable-line
import { IntlMixin } from 'react-intl';

import injectTapEventPlugin from 'react-tap-event-plugin';

import LoginStore from 'stores/LoginStore';
import PreferencesStore from 'stores/PreferencesStore';

import LoginActionCreators from 'actions/LoginActionCreators';

import Deactivated from 'components/Deactivated.react';
import Login from 'components/Login.react';
import Main from 'components/Main.react';
import JoinGroup from 'components/JoinGroup.react';
import Install from 'components/Install.react';
//import AppCache from 'utils/AppCache'; // eslint-disable-line

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

// Internationalisation
const intlData = PreferencesStore.getLanguageData();

const initReact = () => {
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

  router.run((Root) => {
    injectTapEventPlugin();
    React.render(<Root {...intlData}/>, document.getElementById('actor-web-app'));
  });

  if (window.location.hash !== '#/deactivated') {
    if (LoginStore.isLoggedIn()) {
      LoginActionCreators.setLoggedIn(router, {redirect: false});
    }
  }
};

window.jsAppLoaded = () => setTimeout(initReact, 0);
