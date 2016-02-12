/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { find, assign, forEach } from 'lodash';

import React, { Component, PropTypes } from 'react';
import Modal from 'react-modal';

import { KeyCodes } from '../../constants/ActorAppConstants';

import InviteUserActions from '../../actions/InviteUserActions';
import InviteUserByLinkActions from '../../actions/InviteUserByLinkActions';

import ContactStore from '../../stores/PeopleStore';
import InviteUserStore from '../../stores/InviteUserStore';

import ContactItem from './invite-user/ContactItem.react';

const getStateFromStores = () => {
  return ({
    isOpen: InviteUserStore.isModalOpen(),
    contacts: ContactStore.getList(),
    group: InviteUserStore.getGroup()
  });
};

const hasMember = (group, userId) =>
  undefined !== find(group.members, (c) => c.peerInfo.peer.id === userId);

class InviteUser extends Component {
  constructor(props) {
    super(props);

    this.state = assign({
      search: ''
    }, getStateFromStores());

    InviteUserStore.addChangeListener(this.onChange);
    ContactStore.addListener(this.onChange);
  }

  static contextTypes = {
    intl: PropTypes.object
  };

  componentWillUnmount() {
    InviteUserStore.removeChangeListener(this.onChange);
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
    const { intl } = this.context;

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
            {intl.messages['inviteModalNotFound']}
          </li>
        );
      }

      return (
        <Modal className="modal-new modal-new--invite contacts"
               closeTimeoutMS={150}
               isOpen={isOpen}
               style={{width: 440}}>

          <header className="modal-new__header">
            <a className="modal-new__header__icon material-icons">person_add</a>
            <h3 className="modal-new__header__title">{intl.messages['inviteModalTitle']}</h3>

            <div className="pull-right">
              <button className="button button--lightblue" onClick={this.onClose}>{intl.messages['button.done']}</button>
            </div>
          </header>

          <div className="modal-new__body">
            <div className="modal-new__search">
              <i className="material-icons">search</i>
              <input className="input input--search"
                     onChange={this.onSearchChange}
                     placeholder={intl.messages['inviteModalSearch']}
                     type="search"
                     value={search}/>
            </div>

            <a className="link link--blue" onClick={this.onInviteUrlByClick}>
              <i className="material-icons">link</i>
              {intl.messages['inviteByLink']}
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
