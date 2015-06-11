import React from 'react';
import Router from 'react-router';

import Login from './components/Login.react.js';
import Main from './components/Main.react';
import JoinGroup from './components/JoinGroup.react';

import LoginStore from './stores/LoginStore';
import LoginActionCreators from './actions/LoginActionCreators';

const DefaultRoute = Router.DefaultRoute;
const Route = Router.Route;
const RouteHandler = Router.RouteHandler;

window.jsAppLoaded = () => {
  window.messenger = new window.actor.ActorApp();

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
      <DefaultRoute handler={Main}/>
    </Route>
  );

  const router = Router.run(routes, Router.HistoryLocation, function (Handler) {
    React.render(<Handler/>, document.getElementById('actor-web-app'));
  });

  if (LoginStore.isLoggedIn()) {
    LoginActionCreators.setLoggedIn(router, {redirect: false});
  }
};
