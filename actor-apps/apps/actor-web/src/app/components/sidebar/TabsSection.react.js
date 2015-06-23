import React from 'react';

class TabsSection extends React.Component {
  constructor() {
    super();
  }

  render() {
    return (
      <ul className="sidebar__tabs">
        <li className="sidebar__tabs__item sidebar__tabs__item--active">Recents</li>
        <li className="sidebar__tabs__item">Contacts</li>
      </ul>
    );
  }
}

export default TabsSection;
