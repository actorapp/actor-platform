var React = require('react');
var SidebarSection = require('./SidebarSection.react');
var ChatSection = require('./ChatSection.react');

var ActorWebApp = React.createClass({
  propTypes: {
    messenger : React.PropTypes.object.isRequired
  },

  render: function() {
    var body;

    if (this.props.messenger.getUid()) {
      body =
        <div className="app row">
          <SidebarSection></SidebarSection>
          <ChatSection></ChatSection>
        </div>
    } else {
      body = <div className="app row">login form</div>
    }

    return(body);
  }
});

module.exports = ActorWebApp;
