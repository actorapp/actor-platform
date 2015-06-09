import React from 'react';

import requireAuth from '../utils/require-auth';

import VisibilityActionCreators from '../actions/VisibilityActionCreators';

import ActivitySection from './ActivitySection.react';
import SidebarSection from './SidebarSection.react';
import ToolbarSection from './ToolbarSection.react';
import DialogSection from './DialogSection.react';
import ContactsModal from './modals/Contacts.react';

var visibilitychange = 'visibilitychange';
var onVisibilityChange = function () {
  if (!document.hidden) {
    VisibilityActionCreators.createAppVisible();
  } else {
    VisibilityActionCreators.createAppHidden();
  }
};

export default requireAuth(React.createClass({
  componentWillMount: function () {
    document.addEventListener(visibilitychange, onVisibilityChange);

    if (!document.hidden) {
      VisibilityActionCreators.createAppVisible();
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
    );
  }
}));
