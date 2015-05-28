var React = require('react');

var LoginActionCreators = require('../actions/LoginActionCreators');
var LoginStore = require('../stores/LoginStore');

var LoginSection = require('./LoginSection.react');
var SidebarSection = require('./SidebarSection.react');
var ToolbarSection = require('./ToolbarSection.react');
var DialogSection = require('./DialogSection.react');

var getStateFromStores = function() {
  return({
    isLoggedIn: LoginStore.isLoggedIn()
  })
};

var ActorWebApp = React.createClass({
  getInitialState: function() {
    return(getStateFromStores());
  },

  componentWillMount: function() {
    if (LoginStore.isLoggedIn()) {
      LoginActionCreators.setLoggedIn();
    }

    LoginStore.addChangeListener(this._onChange);
  },

  componentWillUnmount: function() {
    LoginStore.removeChangeListener(this._onChange);
  },

  render: function() {
    var body;

    if (this.state.isLoggedIn) {
      body =
        <div className="app row">

          <SidebarSection/>

          <section className="main col-xs">
            <ToolbarSection/>
            <DialogSection/>
          </section>

        </div>
    } else {
      body =
        <div className="login row center-xs middle-xs">

          <LoginSection/>

        </div>
    }

    return(body);
  },

  _onChange: function() {
    this.setState(getStateFromStores())
  }
});

module.exports = ActorWebApp;
