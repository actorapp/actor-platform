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

window.jsAppLoaded = function () {
  window.messenger = new window.actor.ActorApp();

  const App = React.createClass({
    render: function () {
      return <RouteHandler/>;
    }
  });

  const routes = (
    <Route name="app" path="/" handler={App}>
      <Route name="main" path="/" handler={Main}/>
      <Route name="join-group" path="/join/:token" handler={JoinGroup}/>
      <Route name="login" path="/auth" handler={Login}/>
      <DefaultRoute handler={Main}/>
    </Route>
  );

  const router = Router.run(routes, Router.HistoryLocation, function (Handler) {
    React.render(<Handler/>, document.getElementById('actor-web-app'));
  });


  if (LoginStore.isLoggedIn()) {
    LoginActionCreators.setLoggedIn(router);
  }
};
