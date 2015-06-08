'use strict';

var _ = require('lodash');
var React = require('react');

import requireAuth from '../utils/require-auth';

var VisibilityActionCreators = require('../actions/VisibilityActionCreators');

var ActivitySection = require('./ActivitySection.react');
var SidebarSection = require('./SidebarSection.react');
var ToolbarSection = require('./ToolbarSection.react');
var DialogSection = require('./DialogSection.react');
var ContactsModal = require('./modals/Contacts.react');

var visibilitychange = 'visibilitychange';
var onVisibilityChange = function () {
  if (!document.hidden) {
    VisibilityActionCreators.createAppVisible()
  } else {
    VisibilityActionCreators.createAppHidden()
  }
};

var Main = requireAuth(React.createClass({
  componentWillMount: function () {
    document.addEventListener(visibilitychange, onVisibilityChange);

    if (!document.hidden) {
      VisibilityActionCreators.createAppVisible()
    }
  },

  render: function () {
    return (
      <div className="app row">

        <SidebarSection/>

        <section className="main col-xs">
          <ToolbarSection/>
          <DialogSection/>
        </section>

        <ActivitySection/>
        <ContactsModal/>
      </div>
    )
  }
}));

module.exports = Main;
