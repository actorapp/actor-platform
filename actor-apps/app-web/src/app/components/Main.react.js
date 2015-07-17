import React from 'react';

import requireAuth from 'utils/require-auth';

import VisibilityActionCreators from 'actions/VisibilityActionCreators';

import ActivitySection from 'components/ActivitySection.react';
import SidebarSection from 'components/SidebarSection.react';
import ToolbarSection from 'components/ToolbarSection.react';
import DialogSection from 'components/DialogSection.react';

import AppCacheStore from 'stores/AppCacheStore';
import AppCacheUpdateModal from 'components/modals/AppCacheUpdate.react';

const visibilitychange = 'visibilitychange';

const onVisibilityChange = () => {
  if (!document.hidden) {
    VisibilityActionCreators.createAppVisible();
  } else {
    VisibilityActionCreators.createAppHidden();
  }
};

const getStateFromStores = () => {
  return {
    isAppUpdateModalOpen: AppCacheStore.isModalOpen()
  };
};

class Main extends React.Component {
  constructor(props) {
    super(props);

    this.state = getStateFromStores();

    document.addEventListener(visibilitychange, onVisibilityChange);
    AppCacheStore.addChangeListener(this.onChange);

    if (!document.hidden) {
      VisibilityActionCreators.createAppVisible();
    }
  }

  onChange = () => {
    this.setState(getStateFromStores());
  }

  render() {
    let appCacheUpdateModal;
    if (this.state.isAppUpdateModalOpen) {
      appCacheUpdateModal = <AppCacheUpdateModal/>;
    }

    return (
      <div className="app row">

        <SidebarSection/>

        <section className="main col-xs">
          <ToolbarSection/>
          <DialogSection/>
        </section>

        <ActivitySection/>

        {appCacheUpdateModal}
      </div>
    );
  }
}

export default requireAuth(Main);
