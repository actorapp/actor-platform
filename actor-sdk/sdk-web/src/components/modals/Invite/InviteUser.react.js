/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { find, assign, forEach } from 'lodash';
import React, { Component, PropTypes } from 'react';
import { Container } from 'flux/utils';
import Modal from 'react-modal';

import { KeyCodes } from '../../../constants/ActorAppConstants';

import InviteUserActions from '../../../actions/InviteUserActions';
import InviteUserByLinkActions from '../../../actions/InviteUserByLinkActions';

import ContactStore from '../../../stores/PeopleStore';
import InviteUserStore from '../../../stores/InviteUserStore';

import ContactItem from './ContactItem.react.js';

const hasMember = (group, userId) =>
  undefined !== find(group.members, (c) => c.peerInfo.peer.id === userId);

class InviteUser extends Component {
  static getStores() {
    return [InviteUserStore, ContactStore];
  }

  static calculateState(prevState) {
    return {
      search: prevState ? prevState.search : '',
      isOpen: InviteUserStore.isModalOpen(),
      contacts: ContactStore.getList(),
      group: InviteUserStore.getGroup(),
      inviteState: InviteUserStore.getInviteUserState()
    };
  }

  static contextTypes = {
    intl: PropTypes.object
  };

  componentWillUpdate(nextProps, nextState) {
    if (nextState.isOpen && !this.state.isOpen) {
      document.addEventListener('keydown', this.onKeyDown, false);
    } else if (!nextState.isOpen && this.state.isOpen) {
      document.removeEventListener('keydown', this.onKeyDown, false);
    }
  }

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

  renderContactList() {
    const { contacts, group, search, inviteState } = this.state;

    let contactList = [];

    forEach(contacts, (contact, index) => {
      const name = contact.name.toLowerCase();
      if (name.includes(search.toLowerCase())) {
        if (!hasMember(group, contact.uid)) {
          contactList.push(
            <ContactItem contact={contact} key={index} onSelect={this.onContactSelect} inviteState={inviteState[contact.uid]}/>
          );
        } else {
          contactList.push(
            <ContactItem contact={contact} key={index} isMember/>
          );
        }
      }
    }, this);

    if (contactList.length === 0) {
      return (
        <li className="contacts__list__item contacts__list__item--empty text-center">
          {intl.messages['inviteModalNotFound']}
        </li>
      );
    }

    return contactList;
  }

  render() {
    const { search, isOpen } = this.state;
    const { intl } = this.context;

    if (isOpen) {

      const modalStyle = {
        content : {
          position: null,
          top: null,
          left: null,
          right: null,
          bottom: null,
          border: null,
          background: null,
          overflow: null,
          outline: null,
          padding: null,
          borderRadius: null,
          width: 440
        }
      };

      return (
        <Modal className="modal-new modal-new--invite contacts"
               closeTimeoutMS={150}
               isOpen={isOpen}
               style={modalStyle}>

          <header className="modal-new__header">
            <a className="modal-new__header__icon material-icons">person_add</a>
            <h3 className="modal-new__header__title">
              {intl.messages['inviteModalTitle']}
            </h3>

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
              {this.renderContactList()}
            </ul>
          </div>

        </Modal>
      );
    } else {
      return null;
    }
  }
}

export default Container.create(InviteUser, {pure: false});
