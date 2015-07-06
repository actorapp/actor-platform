import React from 'react';

import Tabs from 'react-simpletabs';

import HeaderSection from './sidebar/HeaderSection.react';
import RecentSection from './sidebar/RecentSection.react';
import ContactsSection from './sidebar/ContactsSection.react';

class SidebarSection extends React.Component {
  constructor() {
    super();
  }

  render() {
    return (
      <aside className="sidebar">
        <HeaderSection/>

        <Tabs className="sidebar__tabs">
          <Tabs.Panel title="Recent">
            <RecentSection/>
          </Tabs.Panel>
          <Tabs.Panel title="Contacts">
            <ContactsSection/>
          </Tabs.Panel>
        </Tabs>
      </aside>
    );
  }
}

export default SidebarSection;
