/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { find, assign, forEach } from 'lodash';

import React from 'react';
import Modal from 'react-modal';
import ReactMixin from 'react-mixin';
import { IntlMixin } from 'react-intl';

import ActorClient from '../../utils/ActorClient';
import { KeyCodes } from '../../constants/ActorAppConstants';

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

class ContactsModal extends React.Component {
  constructor(props) {
    super(props);

    this.state = assign({
      search: ''
    }, getStateFromStores());

    ContactStore.addChangeListener(this.onChange);
    document.addEventListener('keydown', this.handleKeyDown, false);
  }

  componentWillUnmount() {
    ContactStore.removeChangeListener(this.onChange);
    document.removeEventListener('keydown', this.handleKeyDown, false);
  }

  onChange = () => this.setState(getStateFromStores());
  handleClose = () => ContactActionCreators.close();

  onContactSelect = (contact) => {
    DialogActionCreators.selectDialogPeerUser(contact.uid);
    this.handleClose()
  };
  onSearchChange = (event) => this.setState({search: event.target.value});

  handleKeyDown = (event) => {
    if (event.keyCode === KeyCodes.ESC) {
      event.preventDefault();
      this.handleClose();
    }
  };

  render() {
    const { contacts, search, isOpen } = this.state;

    let contactList = [];

    if (isOpen) {

      forEach(contacts, (contact, i) => {
        const name = contact.name.toLowerCase();
        if (name.includes(search.toLowerCase())) {
          contactList.push(
            <ContactItem contact={contact} key={i} onSelect={this.onContactSelect}/>
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
        <Modal className="modal-new modal-new--contacts contacts"
               closeTimeoutMS={150}
               isOpen={isOpen}
               style={{width: 340}}>

          <header className="modal-new__header">
            <a className="modal-new__header__icon material-icons">person</a>

            <h3 className="modal-new__header__title">{this.getIntlMessage('modal.contacts.title')}</h3>

            <div className="pull-right">
              <button className="button button--lightblue"
                      onClick={this.handleClose}>{this.getIntlMessage('button.done')}</button>
            </div>
          </header>

          <div className="modal-new__body">
            <div className="modal-new__search">
              <i className="material-icons">search</i>
              <input className="input input--search"
                     onChange={this.onSearchChange}
                     placeholder={this.getIntlMessage('modal.contacts.search')}
                     type="search"
                     value={search}/>
            </div>
          </div>

          <div className="contacts__body">
            <ul className="contacts__list">
              {contactList}
            </ul>
          </div>

        </Modal>
      );
    } else {
      return null;
    }
  }
}

ReactMixin.onClass(ContactsModal, IntlMixin);

export default ContactsModal;
