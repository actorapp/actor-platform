import crosstab from 'crosstab';

import React from 'react';
import Router from 'react-router';
import Raven from './utils/Raven'; // eslint-disable-line

import injectTapEventPlugin from 'react-tap-event-plugin';

import Deactivated from './components/Deactivated.react';
import Login from './components/Login.react';
import Main from './components/Main.react';
import JoinGroup from './components/JoinGroup.react';

import LoginStore from './stores/LoginStore';
import LoginActionCreators from './actions/LoginActionCreators';

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

  const App = React.createClass({
    render() {
      return <RouteHandler/>;
    }
  });

  const routes = (
    <Route handler={App} name="app" path="/">
      <Route handler={Main} name="main" path="/"/>
      <Route handler={JoinGroup} name="join" path="/join/:token"/>
      <Route handler={Login} name="login" path="/auth"/>
      <Route handler={Deactivated} name="deactivated" path="/deactivated"/>
      <DefaultRoute handler={Main}/>
    </Route>
  );

  const router = Router.run(routes, Router.HashLocation, function (Handler) {
    injectTapEventPlugin();
    React.render(<Handler/>, document.getElementById('actor-web-app'));
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
