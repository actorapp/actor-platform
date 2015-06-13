import React from 'react';

import requireAuth from '../utils/require-auth';

import VisibilityActionCreators from '../actions/VisibilityActionCreators';

import ActivitySection from './ActivitySection.react';
import SidebarSection from './SidebarSection.react';
import ToolbarSection from './ToolbarSection.react';
import DialogSection from './DialogSection.react';
import ContactsModal from './modals/Contacts.react';

const visibilitychange = 'visibilitychange';
var onVisibilityChange = () => {
  if (!document.hidden) {
    VisibilityActionCreators.createAppVisible();
  } else {
    VisibilityActionCreators.createAppHidden();
  }
};

class Main extends React.Component {
  componentWillMount() {
    document.addEventListener(visibilitychange, onVisibilityChange);

    if (!document.hidden) {
      VisibilityActionCreators.createAppVisible();
    }
  }

  constructor() {
    super();
  }


  render() {
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
}

export default requireAuth(Main);
