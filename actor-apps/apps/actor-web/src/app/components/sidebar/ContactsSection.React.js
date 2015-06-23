import _ from 'lodash';

import React from 'react';

import ContactStore from '../../stores/ContactStore';

import ContactsSectionItem from './ContactsSectionItem.react';

const getStateFromStores = () => {
  return {
    contacts: ContactStore.getContacts()
  };
};

class ContactsSection extends React.Component {
  componentWillMount() {
    ContactStore.addChangeListener(this.onChange);
  }

  componentWillUnmount() {
    ContactStore.removeChangeListener(this.onChange);
  }

  constructor() {
    super();

    this.onChange = this.onChange.bind(this);

    this.state = getStateFromStores();
  }

  onChange() {
    this.setState(getStateFromStores());
  }

  render() {
    let contacts = this.state.contacts;

    let contactList = _.map(contacts, (contact, i) => {
      return (
        <ContactsSectionItem contact={contact} key={i}/>
      );
    });

    return (
      <ul className="sidebar__list sidebar__list--contacts">
        {contactList}
      </ul>
    );
  }
}

export default ContactsSection;

