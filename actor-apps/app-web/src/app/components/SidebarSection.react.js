import React from 'react';

import { Styles, Tabs, Tab } from 'material-ui';
import ActorTheme from '../constants/ActorTheme';

import HeaderSection from './sidebar/HeaderSection.react';
import RecentSection from './sidebar/RecentSection.react';
import ContactsSection from './sidebar/ContactsSection.react';

const ThemeManager = new Styles.ThemeManager();


class SidebarSection extends React.Component {
  static childContextTypes = {
    muiTheme: React.PropTypes.object
  };

  getChildContext() {
    return {
      muiTheme: ThemeManager.getCurrentTheme()
    };
  }

  componentWillMount() {
    ThemeManager.setTheme(ActorTheme);
  }

  constructor() {
    super();
  }

  render() {
    return (
      <aside className="sidebar">
        <HeaderSection/>

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


        {/*
        <Tabs className="sidebar__tabs">
          <Tabs.Panel title="Recent">
            <RecentSection/>
          </Tabs.Panel>
          <Tabs.Panel title="Contacts">
            <ContactsSection/>
          </Tabs.Panel>
        </Tabs>
         */}
      </aside>
    );
  }
}

export default SidebarSection;
