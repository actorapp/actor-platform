var React = require('react');
var SidebarSection = require('./SidebarSection.react');
var MessageSection = require('./MessageSection.react');

var ActorWebApp = React.createClass({
  propTypes: {
    messenger : React.PropTypes.object.isRequired
  },

  render: function() {
    var body;

    if (this.props.messenger.getUid()) {
      body =
        <div className="app row">
          <SidebarSection messenger={this.props.messenger}></SidebarSection>
          <MessageSection></MessageSection>
        </div>
    } else {
      body = <div className="app row">login form</div>
    }

    return(body);
  }
});

module.exports = ActorWebApp;
