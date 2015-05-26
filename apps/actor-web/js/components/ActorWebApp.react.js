var React = require('react');

var DialogActionCreators = require('../actions/DialogActionCreators.react');

var SidebarSection = require('./SidebarSection.react');
var ToolbarSection = require('./ToolbarSection.react');
var DialogSection = require('./DialogSection.react');
var ComposeSection = require('./ComposeSection.react');

var _dialogsCallback = function(dialogs) {
  DialogActionCreators.setDialogs(dialogs);
};

var ActorWebApp = React.createClass({
  componentWillMount: function() {
    window.messenger.bindDialogs(_dialogsCallback);
  },

  componentWillUnmount: function() {
    window.messenger.unbindDialogs(_dialogsCallback);
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
