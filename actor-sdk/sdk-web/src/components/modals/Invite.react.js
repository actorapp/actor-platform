/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import Modal from 'react-modal';
import { Container } from 'flux/utils';
import fuzzaldrin from 'fuzzaldrin';
import classNames from 'classnames';
import { FormattedMessage } from 'react-intl';

import { AsyncActionStates, ModalTypes } from '../../constants/ActorAppConstants';
import { hasMember } from '../../utils/GroupUtils';

import InviteUserActions from '../../actions/InviteUserActions';
import InviteUserByLinkActions from '../../actions/InviteUserByLinkActions';

import PeopleStore from '../../stores/PeopleStore';
import InviteUserStore from '../../stores/InviteUserStore';

import ContactItem from '../common/ContactItem.react';
import Stateful from '../common/Stateful.react';

class InviteUser extends Component {
  static getStores() {
    return [InviteUserStore, PeopleStore];
  }

  static calculateState() {
    return {
      contacts: PeopleStore.getState(),
      group: InviteUserStore.getState().group,
      users: InviteUserStore.getState().users
    };
  }

  static contextTypes = {
    intl: PropTypes.object
  }

  constructor(props, context) {
    super(props, context);

    this.handleClose = this.handleClose.bind(this);
    this.onSearchChange = this.onSearchChange.bind(this);
    this.onContactSelect = this.onContactSelect.bind(this);
    this.onInviteUrlByClick = this.onInviteUrlByClick.bind(this);
  }

  handleClose() {
    InviteUserActions.hide();
  }

  onContactSelect(uid) {
    InviteUserActions.inviteUser(this.state.group.id, uid);
  }

  onInviteUrlByClick() {
    const { group } = this.state;

    InviteUserByLinkActions.show(group, ModalTypes.INVITE);
  }

  onSearchChange(event) {
    this.setState({ search: event.target.value });
  }

  getContacts() {
    const { contacts, search } = this.state;
    if (!search) return contacts;

    return contacts.filter((contact) => {
      return fuzzaldrin.score(contact.name, search) > 0;
    });
  }

  renderContacts() {
    const { group, users } = this.state;
    const contacts = this.getContacts();

    if (!contacts.length) {
      return (
        <li className="contacts__list__item contacts__list__item--empty text-center">
          <FormattedMessage id="invite.notFound"/>
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

  renderSearch() {
    const { search } = this.state;
    const { intl } = this.context;

    return (
      <div className="small-search">
        <i className="material-icons">search</i>
        <input className="input"
               onChange={this.onSearchChange}
               ref="search"
               placeholder={intl.messages['invite.search']}
               type="search"
               value={search}/>
      </div>
    );
  }

  inviteByLinkButton() {
    return (
      <a className="link link--blue" onClick={this.onInviteUrlByClick}>
        <i className="material-icons">link</i>
        <FormattedMessage id="invite.inviteByLink"/>
      </a>
    );
  }

  render() {
    return (
      <Modal
        overlayClassName="modal-overlay"
        className="modal"
        onRequestClose={this.handleClose}
        isOpen>

        <div className="invite">
          <div className="modal__content">

            <header className="modal__header">
              <i className="modal__header__icon material-icons">person_add</i>
              <FormattedMessage id="invite.title" tagName="h1"/>
              <button className="button button--lightblue" onClick={this.handleClose}>
                <FormattedMessage id="button.done"/>
              </button>
            </header>

            <div className="modal__body">
              {this.renderSearch()}

              {this.inviteByLinkButton()}

              <ul className="contacts__list">
                {this.renderContacts()}
              </ul>
            </div>

          </div>
        </div>

      </Modal>
    );
  }
}

export default Container.create(InviteUser);
