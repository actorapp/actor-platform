/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import Modal from 'react-modal';
import { Container } from 'flux/utils';
import fuzzaldrin from 'fuzzaldrin';
import classNames from 'classnames';

import { KeyCodes, AsyncActionStates } from '../../constants/ActorAppConstants';
import { hasMember } from '../../utils/GroupUtils';

import InviteUserActions from '../../actions/InviteUserActions';
import InviteUserByLinkActions from '../../actions/InviteUserByLinkActions';

import ContactsStore from '../../stores/ContactsStore';
import InviteUserStore from '../../stores/InviteUserStore';

import ContactItem from '../common/ContactItem.react';
import Stateful from '../common/Stateful.react';

class InviteUser extends Component {
  static contextTypes = {
    intl: PropTypes.object
  };

  static getStores() {
    return [InviteUserStore, ContactsStore];
  }

  static calculateState() {
    const contacts = ContactsStore.getState();
    const { isOpen, group, users, query } = InviteUserStore.getState();

    return {
      contacts,
      isOpen,
      group,
      users,
      query
    };
  }

  constructor(props, context) {
    super(props, context);

    this.onClose = this.onClose.bind(this);
    this.onSearchChange = this.onSearchChange.bind(this);
    this.onContactSelect = this.onContactSelect.bind(this);
    this.onInviteUrlByClick = this.onInviteUrlByClick.bind(this);
    this.onKeyDown = this.onKeyDown.bind(this);
  }

  componentWillUpdate(nextProps, nextState) {
    if (nextState.isOpen && !this.state.isOpen) {
      document.addEventListener('keydown', this.onKeyDown, false);
    } else if (!nextState.isOpen && this.state.isOpen) {
      document.removeEventListener('keydown', this.onKeyDown, false);
    }
  }

  onClose() {
    InviteUserActions.hide();
  }

  onSearchChange(event) {
    InviteUserActions.setQuery(event.target.value);
  }

  onContactSelect(uid) {
    InviteUserActions.inviteUser(this.state.group.id, uid);
  }

  onInviteUrlByClick() {
    const { group } = this.state;

    InviteUserByLinkActions.show(group);
    InviteUserActions.hide();
  }

  onKeyDown(event) {
    if (event.keyCode === KeyCodes.ESC) {
      event.preventDefault();
      this.onClose();
    }
  }

  getContacts() {
    const { contacts, query } = this.state;

    if (!query) {
      return contacts;
    }

    return contacts.filter((contact) => {
      const score = fuzzaldrin.score(contact.name, query);
      return score > 0;
    });
  }

  renderContacts() {
    const { intl } = this.context;
    const { group, users } = this.state;
    const contacts = this.getContacts();

    if (!contacts.length) {
      return (
        <li className="contacts__list__item contacts__list__item--empty text-center">
          {intl.messages['inviteModalNotFound']}
        </li>
      );
    }

    return contacts.map((contact) => {
      const isMember = hasMember(group.id, contact.uid);
      const currentState = isMember ? AsyncActionStates.SUCCESS : (users[contact.uid] || AsyncActionStates.PENDING);

      const onClick = () => {
        console.log(`%c Trying to invite "${contact.name}"(uid=${contact.uid}) to group ${group.id}`, 'color: #fd5c52');
        this.onContactSelect(contact.uid)
      };

      const contactClassName = classNames({
        'contact--disabled': currentState === AsyncActionStates.SUCCESS
      });

      return (
        <ContactItem {...contact} className={contactClassName} key={contact.uid}>
          <Stateful
            currentState={currentState}
            pending={<a className="material-icons" onClick={onClick}>person_add</a>}
            processing={<i className="material-icons spin">autorenew</i>}
            success={<i className="material-icons">check</i>}
            failure={<i className="material-icons">warning</i>}
          />
        </ContactItem>
      );
    });
  }

  render() {
    const { isOpen, search } = this.state;
    const { intl } = this.context;

    if (!isOpen) {
      return null;
    }

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
            {this.renderContacts()}
          </ul>
        </div>

      </Modal>
    );
  }
}

export default Container.create(InviteUser);
