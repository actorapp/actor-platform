var React = require('react');

var LoginActionCreators = require('../actions/LoginActionCreators');
var LoginStore = require('../stores/LoginStore');

var ActorClient = require('../utils/ActorClient');

var SidebarSection = require('./SidebarSection.react');
var ToolbarSection = require('./ToolbarSection.react');
var DialogSection = require('./DialogSection.react');

var ActorWebApp = React.createClass({
  getInitialState: function() {
    return({isLoggedIn: ActorClient.isLoggedIn()});
  },

  componentWillMount: function() {
    if (ActorClient.isLoggedIn()) {
      LoginActionCreators.setLoggedIn();
    }

    LoginStore.addLoginListener(this._onLogin);
  },

  componentWillUnmount: function() {
    LoginStore.removeLoginListener(this._onLogin);
  },

  render: function() {
    var body;

    if (ActorClient.isLoggedIn()) {
      body =
        <div className="app row">
          <SidebarSection></SidebarSection>
          <section className="main col-xs">
            <ToolbarSection></ToolbarSection>
            <DialogSection></DialogSection>
          </section>
        </div>
    } else {
      body = <div className="app row">login form</div>
    }

    return(body);
  },

  _onLogin: function() {
    if (!this.state.isLoggedIn) {
      this.setState({isLoggedIn: true});
    }
  }
});

module.exports = ActorWebApp;
