'use strict';

var _ = require('lodash');
var React = require('react');

var LoginActionCreators = require('../actions/LoginActionCreators');
var LoginStore = require('../stores/LoginStore');

var VisibilityActionCreators = require('../actions/VisibilityActionCreators');

var ActivitySection = require('./ActivitySection.react');
var LoginSection = require('./LoginSection.react');
var ContactSection = require('./ContactSection.react');
var SidebarSection = require('./SidebarSection.react');
var ToolbarSection = require('./ToolbarSection.react');
var DialogSection = require('./DialogSection.react');

var getStateFromStores = function() {
  return({
    isLoggedIn: LoginStore.isLoggedIn()
  })
};

var visibilitychange = 'visibilitychange';
var onVisibilityChange = function() {
  if (!document.hidden) {
    VisibilityActionCreators.createAppVisible()
  } else {
    VisibilityActionCreators.createAppHidden()
  }
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

    document.addEventListener(visibilitychange, onVisibilityChange);

    if (!document.hidden) {
      VisibilityActionCreators.createAppVisible()
    }
  },

  componentWillUnmount: function() {
    LoginStore.removeChangeListener(this._onChange);

    document.removeEventListener(visibilitychange, onVisibilityChange);
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

          <ActivitySection/>

          <ContactSection/>

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
