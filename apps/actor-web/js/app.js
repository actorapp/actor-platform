var ActorWebApp = require('./components/ActorWebApp.react');

var React = require('react');
window.React = React; // export for react-devtools

window.jsAppLoaded = function() {
  window.messenger = new actor.ActorApp;

  React.render(
    <ActorWebApp/>,
    document.getElementById('actor-web-app')
  )
};
