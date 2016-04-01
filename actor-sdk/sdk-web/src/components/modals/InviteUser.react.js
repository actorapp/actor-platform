/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import Modal from 'react-modal';
import { Container } from 'flux/utils';
import fuzzaldrin from 'fuzzaldrin';

import { KeyCodes, AsyncActionStates } from '../../constants/ActorAppConstants';
import { hasMember } from '../../utils/GroupUtils';

import InviteUserActions from '../../actions/InviteUserActions';
import InviteUserByLinkActions from '../../actions/InviteUserByLinkActions';

import PeopleStore from '../../stores/PeopleStore';
import InviteUserStore from '../../stores/InviteUserStore';

import ContactItem from '../common/ContactItem.react';
import Stateful from '../common/Stateful.react';

class InviteUser extends Component {
  static contextTypes = {
    intl: PropTypes.object
  };

  static getStores() {
    return [InviteUserStore, PeopleStore];
  }

  static calculateState() {
    return {
      isOpen: InviteUserStore.isModalOpen(),
      contacts: PeopleStore.getList(),
      group: InviteUserStore.getGroup(),
      inviteUserState: InviteUserStore.getInviteUserState()
    };
  }

  componentWillUpdate(nextProps, nextState) {
    if (nextState.isOpen && !this.state.isOpen) {
      document.addEventListener('keydown', this.onKeyDown, false);
    } else if (!nextState.isOpen && this.state.isOpen) {
      document.removeEventListener('keydown', this.onKeyDown, false);
    }
  }

  onClose = () => InviteUserActions.hide();
  onContactSelect = (uid) => InviteUserActions.inviteUser(this.state.group.id, uid);
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

  getContacts() {
    const { contacts, search } = this.state;

    return fuzzaldrin.filter(contacts, search, {
      key: 'name'
    });
  }

  renderContacts() {
    const { intl } = this.context;
    const { group, inviteUserState } = this.state;
    const contacts = this.getContacts();

    if (!contacts.length) {
      return (
        <li className="contacts__list__item contacts__list__item--empty text-center">
          {intl.messages['inviteModalNotFound']}
        </li>
      );
    }

    return contacts.map((contact, i) => {
      let controls, contactClassName;
      if (hasMember(group.id, contact.uid)) {
        controls = <i className="material-icons">check</i>;
        contactClassName = 'contact--disabled'
      } else {
        const currentState = inviteUserState[contact.uid] || AsyncActionStates.PENDING;
        controls = (
          <Stateful
            currentState={currentState}
            pending={<a className="material-icons" onClick={() => this.onContactSelect(contact.uid)}>person_add</a>}
            processing={<i className="material-icons spin">autorenew</i>}
            success={<i className="material-icons">check</i>}
            failure={<i className="material-icons">warning</i>}
          />
        );
        contactClassName = currentState === AsyncActionStates.SUCCESS ? 'contact--disabled' : '';
      }

      return (
        <ContactItem {...contact} className={contactClassName} key={i}>
          {controls}
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

export default Container.create(InviteUser, {pure: false});
