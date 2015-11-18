/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import { assign, forEach } from 'lodash';
import ReactMixin from 'react-mixin';
import { IntlMixin } from 'react-intl';

import ContactActionCreators from '../../actions/ContactActionCreators';
import DialogActionCreators from '../../actions/DialogActionCreators';

import ContactStore from '../../stores/ContactStore';

import ContactItem from './contacts/ContactItem.react';

const getStateFromStores = () => {
  return ({
    isOpen: ContactStore.isContactsOpen(),
    contacts: ContactStore.getContacts()
  });
};

class Contacts extends Component {
  constructor(props) {
    super(props);

    this.state = assign({
      search: ''
    }, getStateFromStores());
  }

  componentWillMount() {
    ContactStore.addChangeListener(this.handleChange);
  }

  componentDidMount() {
    React.findDOMNode(this.refs.search).focus();
  }

  componentWillUnmount() {
    ContactStore.removeChangeListener(this.handleChange);
  }

  handleChange = () => this.setState(getStateFromStores());
  handleClose = () => ContactActionCreators.close();
  handleSearchChange = (event) => this.setState({search: event.target.value});

  handleContactSelect = (contact) => {
    DialogActionCreators.selectDialogPeerUser(contact.uid);
    this.handleClose()
  };


  render() {
    const { contacts, search } = this.state;

    let contactList = [];

    forEach(contacts, (contact, i) => {
      const name = contact.name.toLowerCase();
      if (name.includes(search.toLowerCase())) {
        contactList.push(
          <ContactItem contact={contact} key={i} onSelect={this.handleContactSelect}/>
        );
      }
    }, this);

    if (contactList.length === 0) {
      contactList.push(
        <li className="contacts__list__item contacts__list__item--empty text-center">
          {this.getIntlMessage('modal.contacts.notFound')}
        </li>
      );
    }

    return (
      <div className="newmodal newmodal__contacts">
        <header className="newmodal__header">
          <h2>{this.getIntlMessage('modal.contacts.title')}</h2>
        </header>

        <section className="newmodal__search">
          <input className="newmodal__search__input"
                 onChange={this.handleSearchChange}
                 placeholder={this.getIntlMessage('modal.contacts.search')}
                 type="search"
                 ref="search"
                 value={search}/>
        </section>

        <ul className="newmodal__result contacts__list">
          {contactList}
        </ul>
      </div>
    )
  }
}

ReactMixin.onClass(Contacts, IntlMixin);

export default Contacts;
