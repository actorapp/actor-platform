/*
 * Copyright (C) 2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { Container } from 'flux/utils';
import fuzzaldrin from 'fuzzaldrin';
import { FormattedMessage } from 'react-intl';

import BlockedUsersActionCreators from '../../../actions/BlockedUsersActionCreators';

import BlockedUsersStore from '../../../stores/BlockedUsersStore';

import ContactItem from '../../common/ContactItem.react';

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

    this.handleQueryChange = this.handleQueryChange.bind(this);
    this.handleUnblock = this.handleUnblock.bind(this);
  }

  componentWillMount() {
    BlockedUsersActionCreators.loadUsers();
  }

  handleQueryChange(event) {
    BlockedUsersActionCreators.setQuery(event.target.value);
  }

  handleUnblock(uid) {
    BlockedUsersActionCreators.unblockUser(uid, true);
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
          <FormattedMessage id="preferences.blocked.notExists"/>
        </div>
      );
    }

    const filtredUsers = this.getUsers();

    if (!filtredUsers.length) {
      return (
        <div className="contacts__list__item contacts__list__item--empty text-center">
          <FormattedMessage id="preferences.blocked.notFound"/>
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
          <button className="button button--lightblue" onClick={() => this.handleUnblock(user.id)}>
            <FormattedMessage id="preferences.blocked.unblock"/>
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
          placeholder={intl.messages['preferences.blocked.search']}
          onChange={this.handleQueryChange}
        />
      </div>
    );
  }

  render() {
    return (
      <div className="preferences__tabs__content blocked-users">
        <ul>
          <li>
            <i className="icon material-icons">block</i>
            <FormattedMessage id="preferences.blocked.title" tagName="h4"/>
            {this.renderSearch()}
            {this.renderUsers()}
          </li>
        </ul>
      </div>
    );
  }
}

export default Container.create(BlockedUsers);
