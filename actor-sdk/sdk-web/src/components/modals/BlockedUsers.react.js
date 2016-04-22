/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import Modal from 'react-modal';
import { Container } from 'flux/utils';
import fuzzaldrin from 'fuzzaldrin';
import { FormattedMessage } from 'react-intl';

import BlockedUsersActionCreators from '../../actions/BlockedUsersActionCreators';

import BlockedUsersStore from '../../stores/BlockedUsersStore';

import ContactItem from '../common/ContactItem.react';

class BlockedUsers extends Component {
  static contextTypes = {
    intl: PropTypes.object
  };

  static getStores() {
    return [BlockedUsersStore];
  }

  static calculateState() {
    return BlockedUsersStore.getState();
  }

  constructor(props, context) {
    super(props, context);

    this.handleClose = this.handleClose.bind(this);
    this.onQueryChange = this.onQueryChange.bind(this);
    this.onUnblock = this.onUnblock.bind(this);
  }

  handleClose() {
    BlockedUsersActionCreators.hide();
  }

  onQueryChange(event) {
    BlockedUsersActionCreators.setQuery(event.target.value);
  }

  onUnblock(uid) {
    BlockedUsersActionCreators.unblockUser(uid, true);
  }

  onReload() {
    BlockedUsersActionCreators.loadUsers();
  }

  getUsers() {
    const { users, query } = this.state;

    if (!query || query === '') return users;

    return users.filter((user) => {
      const score = fuzzaldrin.score(user.name, query);
      return score > 0;
    });
  }

  renderUsers() {
    const { users } = this.state;

    if (!users.length) {
      return (
        <div className="contacts__list__item contacts__list__item--empty text-center">
          <FormattedMessage id="modal.blockedUsers.notExists"/>
        </div>
      );
    }

    const filtredUsers = this.getUsers();

    if (!filtredUsers.length) {
      return (
        <div className="contacts__list__item contacts__list__item--empty text-center">
          <FormattedMessage id="modal.blockedUsers.notFound"/>
        </div>
      );
    }

    return filtredUsers.map((user) => {
      return (
        <ContactItem
          uid={user.id}
          name={user.name}
          placeholder={user.placeholder}
          avatar={user.avatar}
          key={user.id}
        >
          <button className="button button--lightblue" onClick={() => this.onUnblock(user.id)}>
            <FormattedMessage id="modal.blockedUsers.unblock"/>
          </button>
        </ContactItem>
      );
    });
  }

  renderSearch() {
    const { intl } = this.context;
    const { query } = this.state;

    return (
      <div className="small-search">
        <i className="material-icons">search</i>
        <input
          className="input"
          type="search"
          value={query}
          placeholder={intl.messages['modal.blockedUsers.search']}
          onChange={this.onQueryChange}
        />
      </div>
    );
  }

  render() {
    return (
      <Modal
        overlayClassName="modal-overlay"
        className="modal"
        onRequestClose={this.handleClose}
        isOpen>

        <div className="blocked-users">
          <div className="modal__content">

            <header className="modal__header">
              <a className="modal__header__icon material-icons">block</a>
              <FormattedMessage id="modal.blockedUsers.title" tagName="h1"/>
              <button className="button button--lightblue" onClick={this.handleClose}>
                <FormattedMessage id="button.done"/>
              </button>
            </header>

            <div className="modal__body">
              {this.renderSearch()}
            </div>

            <div className="contacts__list">
              {this.renderUsers()}
            </div>

          </div>
        </div>

      </Modal>
    );
  }
}

export default Container.create(BlockedUsers);
