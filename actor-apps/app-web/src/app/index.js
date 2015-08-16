import crosstab from 'crosstab';

import React from 'react';
import Router from 'react-router';
import Raven from 'utils/Raven'; // eslint-disable-line
import isMobile from 'utils/isMobile';
import ReactMixin from 'react-mixin';

import Intl from 'intl'; // eslint-disable-line
import LocaleData from 'intl/locale-data/jsonp/en-US'; // eslint-disable-line
import { IntlMixin } from 'react-intl';
import { english, russian } from 'l18n';

import injectTapEventPlugin from 'react-tap-event-plugin';

import Deactivated from 'components/Deactivated.react';
import Login from 'components/Login.react';
import Main from 'components/Main.react';
import JoinGroup from 'components/JoinGroup.react';
import Install from 'components/Install.react';

import LoginStore from 'stores/LoginStore';
import LoginActionCreators from 'actions/LoginActionCreators';

//import AppCache from 'utils/AppCache'; // eslint-disable-line

import Pace from 'pace';
Pace.start({
  ajax: false,
  restartOnRequestAfter: false,
  restartOnPushState: false
});

const DefaultRoute = Router.DefaultRoute;
const Route = Router.Route;
const RouteHandler = Router.RouteHandler;

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
class App extends React.Component {
  render() {
    return <RouteHandler/>;
  }
}

// Internationalisation
// TODO: Move to preferences
const language = 'en-US';
//const language = 'ru-RU';
let intlData;
switch (language) {
  case 'ru-RU':
    intlData = russian;
    break;
  case 'en-US':
    intlData = english;
    break;
}

const initReact = () => {
  if (window.location.hash !== '#/deactivated') {
    if (crosstab.supported) {
      crosstab.broadcast(ActorInitEvent, {});
    }

    if (location.pathname === '/app/index.html') {
      window.messenger = new window.actor.ActorApp(['ws://' + location.hostname + ':9080/']);
    } else {
      window.messenger = new window.actor.ActorApp();
    }
  }

  const routes = (
    <Route handler={App} name="app" path="/">
      <Route handler={Main} name="main" path="/"/>
      <Route handler={JoinGroup} name="join" path="/join/:token"/>
      <Route handler={Login} name="login" path="/auth"/>
      <Route handler={Deactivated} name="deactivated" path="/deactivated"/>
      <Route handler={Install} name="install" path="/install"/>
      <DefaultRoute handler={Main}/>
    </Route>
  );

  const router = Router.run(routes, Router.HashLocation, function (Handler) {
    injectTapEventPlugin();
    React.render(<Handler {...intlData}/>, document.getElementById('actor-web-app'));
  });

  if (window.location.hash !== '#/deactivated') {
    if (LoginStore.isLoggedIn()) {
      LoginActionCreators.setLoggedIn(router, {redirect: false});
    }
  }
};

window.jsAppLoaded = () => {
  setTimeout(initReact, 0);
};
