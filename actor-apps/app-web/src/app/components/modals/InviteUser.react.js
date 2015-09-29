/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { find, assign, forEach } from 'lodash';

import React from 'react';
import Modal from 'react-modal';
import ReactMixin from 'react-mixin';
import { IntlMixin, FormattedMessage } from 'react-intl';

import ActorClient from 'utils/ActorClient';
import { KeyCodes } from 'constants/ActorAppConstants';

import InviteUserActions from 'actions/InviteUserActions';
import InviteUserByLinkActions from 'actions/InviteUserByLinkActions';

import ContactStore from 'stores/ContactStore';
import InviteUserStore from 'stores/InviteUserStore';

import ContactItem from './invite-user/ContactItem.react';

const getStateFromStores = () => {
  return ({
    isOpen: InviteUserStore.isModalOpen(),
    contacts: ContactStore.getContacts(),
    group: InviteUserStore.getGroup()
  });
};

const hasMember = (group, userId) =>
  undefined !== find(group.members, (c) => c.peerInfo.peer.id === userId);

@ReactMixin.decorate(IntlMixin)
class InviteUser extends React.Component {
  constructor(props) {
    super(props);

    this.state = assign({
      search: ''
    }, getStateFromStores());

    InviteUserStore.addChangeListener(this.onChange);
    ContactStore.addChangeListener(this.onChange);
  }

  componentWillUnmount() {
    InviteUserStore.removeChangeListener(this.onChange);
    ContactStore.removeChangeListener(this.onChange);
  }

  componentWillUpdate(nextProps, nextState) {
    if (nextState.isOpen && !this.state.isOpen) {
      document.addEventListener('keydown', this.onKeyDown, false);
    } else if (!nextState.isOpen && this.state.isOpen) {
      document.removeEventListener('keydown', this.onKeyDown, false);
    }
  }

  onChange = () => this.setState(getStateFromStores());
  onClose = () => InviteUserActions.hide();
  onContactSelect = (contact) => InviteUserActions.inviteUser(this.state.group.id, contact.uid);
  onSearchChange = (event) => this.setState({search: event.target.value});

  onInviteUrlByClick = () => {
    const { group } = this.state;

    InviteUserByLinkActions.show(group);
    InviteUserActions.hide();
  };

  onKeyDown = (event) => {
    if (event.keyCode === KeyCodes.ESC) {
      event.preventDefault();
      this.onClose();
    }
  };

  render() {
    const { contacts, group, search, isOpen } = this.state;

    let contactList = [];

    if (isOpen) {

      forEach(contacts, (contact, i) => {
        const name = contact.name.toLowerCase();
        if (name.includes(search.toLowerCase())) {
          if (!hasMember(group, contact.uid)) {
            contactList.push(
              <ContactItem contact={contact} key={i} onSelect={this.onContactSelect}/>
            );
          } else {
            contactList.push(
              <ContactItem contact={contact} key={i} isMember/>
            );
          }
        }
      }, this);

      if (contactList.length === 0) {
        contactList.push(
          <li className="contacts__list__item contacts__list__item--empty text-center">
            <FormattedMessage message={this.getIntlMessage('inviteModalNotFound')}/>
          </li>
        );
      }

      return (
        <Modal className="modal-new modal-new--invite contacts"
               closeTimeoutMS={150}
               isOpen={isOpen}
               style={{width: 400}}>

          <header className="modal-new__header">
            <a className="modal-new__header__icon material-icons">person_add</a>
            <h4 className="modal-new__header__title">
              <FormattedMessage message={this.getIntlMessage('inviteModalTitle')}/>
            </h4>

            <div className="pull-right">
              <button className="button button--lightblue" onClick={this.onClose}>Done</button>
            </div>
          </header>

          <div className="modal-new__body">
            <div className="modal-new__search">
              <i className="material-icons">search</i>
              <input className="input input--search"
                     onChange={this.onSearchChange}
                     placeholder={this.getIntlMessage('inviteModalSearch')}
                     type="search"
                     value={search}/>
            </div>

            <a className="link link--blue" onClick={this.onInviteUrlByClick}>
              <i className="material-icons">link</i>
              {this.getIntlMessage('inviteByLink')}
            </a>
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

export default InviteUser;
