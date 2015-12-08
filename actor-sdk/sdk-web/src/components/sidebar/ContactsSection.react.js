
import React, { Component } from 'react';
import { Styles, RaisedButton } from 'material-ui';
import ActorTheme from '../../constants/ActorTheme';

import ContactStore from '../../stores/ContactStore';
import AddContactStore from '../../stores/AddContactStore';

import ContactActionCreators from '../../actions/ContactActionCreators';
import AddContactActionCreators from '../../actions/AddContactActionCreators';

import ContactsSectionItem from './ContactsSectionItem.react';
import AddContactModal from '../modals/AddContact.react';

const ThemeManager = new Styles.ThemeManager();

const getStateFromStores = () => {
  return {
    isAddContactModalOpen: AddContactStore.isOpen(),
    contacts: ContactStore.getContacts()
  };
};

class ContactsSection extends Component {
  static childContextTypes = {
    muiTheme: React.PropTypes.object
  };

  getChildContext() {
    return {
      muiTheme: ThemeManager.getCurrentTheme()
    };
  }

  componentWillUnmount() {
    ContactActionCreators.close();
    ContactStore.removeChangeListener(this.onChange);
    AddContactStore.removeChangeListener(this.onChange);
  }

  constructor(props) {
    super(props);

    this.state = getStateFromStores();

    ContactActionCreators.open();
    ContactStore.addChangeListener(this.onChange);
    AddContactStore.addChangeListener(this.onChange);

    ThemeManager.setTheme(ActorTheme);
  }

  onChange = () => {
    this.setState(getStateFromStores());
  };

  openAddContactModal = () => {
    AddContactActionCreators.openModal();
  };

  render() {
    let contacts = this.state.contacts;

    let contactList = _.map(contacts, (contact, i) => {
      return (
        <ContactsSectionItem contact={contact} key={i}/>
      );
    });

    let addContactModal;
    if (this.state.isAddContactModalOpen) {
      addContactModal = <AddContactModal/>;
    }

    return (
      <section className="sidebar__contacts">

        <ul className="sidebar__list sidebar__list--contacts">
          {contactList}
        </ul>

      <footer>
        <RaisedButton label="Add contact" onClick={this.openAddContactModal} style={{width: '100%'}}/>
        {addContactModal}
      </footer>
    </section>
  );
  }
}

export default ContactsSection;
