var React = require('react');

var ActorClient = require('../utils/ActorClient');

var SidebarSection = require('./SidebarSection.react');
var ToolbarSection = require('./ToolbarSection.react');
var DialogSection = require('./DialogSection.react');
var ComposeSection = require('./ComposeSection.react');

var ActorWebApp = React.createClass({
  componentWillMount: function() {
    ActorClient.bindDialogs();
  },

  componentWillUnmount: function() {
    ActorClient.unbindDialogs();
  },

  render: function() {
    var body;

    if (window.messenger.isLoggedIn()) {
      body =
        <div className="app row">
          <SidebarSection></SidebarSection>
          <section className="main col-xs">
            <ToolbarSection></ToolbarSection>
            <DialogSection></DialogSection>
            <ComposeSection></ComposeSection>
          </section>
        </div>
    } else {
      body = <div className="app row">login form</div>
    }

    return(body);
  }
});

module.exports = ActorWebApp;
