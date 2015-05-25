var React = require('react');
var SidebarSection = require('./SidebarSection.react');
var ToolbarSection = require('./ToolbarSection.react');
var MessageSection = require('./MessageSection.react');
var ComposeSection = require('./ComposeSection.react');

var ActorWebApp = React.createClass({
  render: function() {
    var body;

    if (window.messenger.isLoggedIn()) {
      body =
        <div className="app row">
          <SidebarSection></SidebarSection>
          <section className="main col-xs">
            <ToolbarSection></ToolbarSection>
            <MessageSection></MessageSection>
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
