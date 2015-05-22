var ActorWebApp = require('./components/ActorWebApp.react');

var React = require('react');
window.React = React; // export for react-devtools

window.jsAppLoaded = function() {
  var messenger = new actor.ActorApp;

  React.render(
    <ActorWebApp messenger={messenger}/>,
    document.getElementById('actor-web-app')
  )
};
