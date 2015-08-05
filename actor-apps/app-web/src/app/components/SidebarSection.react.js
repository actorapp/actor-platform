import React from 'react';

//import { Styles, Tabs, Tab } from 'material-ui';
//import ActorTheme from 'constants/ActorTheme';

import HeaderSection from 'components/sidebar/HeaderSection.react';
import RecentSection from 'components/sidebar/RecentSection.react';
//import ContactsSection from 'components/sidebar/ContactsSection.react';

//const ThemeManager = new Styles.ThemeManager();

class SidebarSection extends React.Component {
  //static childContextTypes = {
  //  muiTheme: React.PropTypes.object
  //};
  //
  //getChildContext() {
  //  return {
  //    muiTheme: ThemeManager.getCurrentTheme()
  //  };
  //}

  constructor(props) {
    super(props);

    //ThemeManager.setTheme(ActorTheme);
  }

  render() {
    return (
      <aside className="sidebar">
        <HeaderSection/>
        <RecentSection/>
        {/*
        <Tabs className="sidebar__tabs"
              contentContainerClassName="sidebar__tabs__tab-content"
              tabItemContainerClassName="sidebar__tabs__tab-items">

          <Tab label="Recent">
            <RecentSection/>
          </Tab>

          <Tab label="Contacts">
            <ContactsSection/>
          </Tab>

        </Tabs>
        */}
      </aside>
    );
  }
}

export default SidebarSection;
