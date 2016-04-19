/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import Modal from 'react-modal';
import { Container } from 'flux/utils';
import fuzzaldrin from 'fuzzaldrin';

import { KeyCodes } from '../../constants/ActorAppConstants';
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

    this.onClose = this.onClose.bind(this);
    this.onQueryChange = this.onQueryChange.bind(this);
    this.onUnblock = this.onUnblock.bind(this);
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

  onKeyDown(event) {
    if (event.keyCode === KeyCodes.ESC) {
      event.preventDefault();
      this.onClose();
    }
  }

  getUsers() {
    const { users, query } = this.state;

    if (!query) {
      return users;
    }

    return users.filter((user) => {
      const score = fuzzaldrin.score(user.name, query);
      return score > 0;
    });
  }

  renderUsers() {
    const { intl } = this.context;
    const { users } = this.state;

    if (!users.length) {
      return (
        <li className="contacts__list__item contacts__list__item--empty text-center">
          {intl.messages['blockedUsersNotExists']}
        </li>
      );
    }

    const filtredUsers = this.getUsers();

    if (!filtredUsers.length) {
      return (
        <li className="contacts__list__item contacts__list__item--empty text-center">
          {intl.messages['blockedUsersNotFound']}
        </li>
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
            {intl.messages['blockedUsersUnblock']}
          </button>
        </ContactItem>
      );
    });
  }

  getStyles() {
    return {
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
  }

  render() {
    const { isOpen, query } = this.state;
    const { intl } = this.context;

    if (!isOpen) {
      return null;
    }


    return (
      <Modal
        className="modal-new modal-new--invite contacts"
        closeTimeoutMS={150}
        isOpen={isOpen}
        style={this.getStyles()}
      >
        <header className="modal-new__header">
          <a className="modal-new__header__icon material-icons">block</a>
          <h3 className="modal-new__header__title">{intl.messages['blockedUsersTitle']}</h3>

          <div className="pull-right">
            <button className="button button--lightblue" onClick={this.onClose}>
              {intl.messages['button.done']}
            </button>
          </div>
        </header>

        <div className="modal-new__body">
          <div className="modal-new__search">
            <i className="material-icons">search</i>
            <input
              className="input input--search"
              type="search"
              value={query}
              placeholder={intl.messages['blockedUsersSearch']}
              onChange={this.onQueryChange}
            />
          </div>
        </div>

        <div className="contacts__body">
          <ul className="contacts__list">
            {this.renderUsers()}
          </ul>
        </div>

      </Modal>
    );
  }
}

export default Container.create(BlockedUsers);
