var React = require('react');
var SidebarSection = require('./SidebarSection.react');
var ToolbarSection = require('./ToolbarSection.react');
var MessageSection = require('./MessageSection.react');
var ComposeSection = require('./ComposeSection.react');

var ActorWebApp = React.createClass({
  propTypes: {
    messenger : React.PropTypes.object.isRequired
  },

  render: function() {
    var body;
    var messenger = this.props.messenger;

    if (messenger.isLoggedIn()) {
      body =
        <div className="app row">
          <SidebarSection messenger={messenger}></SidebarSection>
          <section className="main col-xs">
            <ToolbarSection messenger={messenger}></ToolbarSection>
            <MessageSection messenger={messenger}></MessageSection>
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
