/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { Container } from 'flux/utils';
import { FormattedMessage } from 'react-intl';
import fuzzaldrin from 'fuzzaldrin';
import { CreateGroupSteps } from '../../../constants/ActorAppConstants';

import CreateGroupActionCreators from '../../../actions/CreateGroupActionCreators';

import PeopleStore from '../../../stores/PeopleStore';
import CreateGroupStore from '../../../stores/CreateGroupStore';

import ContactItem from '../../common/ContactItem.react';
import TextField from '../../common/TextField.react';

class CreateGroupForm extends Component {
  static contextTypes = {
    intl: PropTypes.object
  };

  static getStores() {
    return [CreateGroupStore, PeopleStore];
  }

  static calculateState() {
    return {
      step: CreateGroupStore.getCurrentStep(),
      name: CreateGroupStore.getGroupName(),
      selectedUserIds: CreateGroupStore.getSelectedUserIds(),
      contacts: PeopleStore.getState()
    };
  }

  constructor(props, context) {
    super(props, context);

    this.onContactToggle = this.onContactToggle.bind(this);
    this.handleNameChange = this.handleNameChange.bind(this);
    this.handleNameSubmit = this.handleNameSubmit.bind(this);
    this.handleCreateGroup = this.handleCreateGroup.bind(this);
    this.onSearchChange = this.onSearchChange.bind(this);
  }

  componentDidMount() {
    if (this.state.step === CreateGroupSteps.NAME_INPUT) {
      this.refs.name.focus();
    }
  }

  getContacts() {
    const { contacts, search } = this.state;

    return fuzzaldrin.filter(contacts, search, {
      key: 'name'
    });
  }

  renderContacts() {
    const { selectedUserIds } = this.state;
    const contacts = this.getContacts();

    if (!contacts.length) {
      return (
        <li className="contacts__list__item contacts__list__item--empty text-center">
          <FormattedMessage id="invite.notFound"/>
        </li>
      );
    }

    return contacts.map((contact, i) => {
      const isSelected = selectedUserIds.has(contact.uid);
      const icon = isSelected ? 'check_box' : 'check_box_outline_blank';

      return (
        <ContactItem {...contact} key={i}>
          <a className="material-icons" onClick={() => this.onContactToggle(contact, !isSelected)}>
            {icon}
          </a>
        </ContactItem>
      );
    });
  }

  onContactToggle(contact, isSelected) {
    const { selectedUserIds } = this.state;

    if (isSelected) {
      CreateGroupActionCreators.setSelectedUserIds(selectedUserIds.add(contact.uid));
    } else {
      CreateGroupActionCreators.setSelectedUserIds(selectedUserIds.remove(contact.uid));
    }
  }

  handleNameChange(event) {
    event.preventDefault();

    this.setState({ name: event.target.value });
  }

  handleNameSubmit(event) {
    event.preventDefault();

    const { name } = this.state;
    const trimmedName = name.trim();

    if (trimmedName.length > 0) {
      CreateGroupActionCreators.setGroupName(trimmedName);
    }
  }

  handleCreateGroup(event) {
    event.preventDefault();
    const { name, selectedUserIds } = this.state;

    CreateGroupActionCreators.createGroup(name, null, selectedUserIds.toJS());
  }

  onSearchChange(event) {
    this.setState({ search: event.target.value });
  }

  renderGroupNameInput() {
    const { name } = this.state;
    return (
      <TextField
        className="input__material--wide"
        floatingLabel={<FormattedMessage id="modal.createGroup.groupName"/>}
        ref="name"
        onChange={this.handleNameChange}
        value={name}/>
    );
  }

  renderAddUsersButton() {
    return (
      <button className="button button--lightblue" onClick={this.handleNameSubmit}>
        <FormattedMessage id="button.addMembers"/>
      </button>
    );
  }

  renderUserSearchInput() {
    const { search } = this.state;
    const { intl } = this.context;

    return (
      <div className="small-search">
        <i className="material-icons">search</i>
        <input
          className="input"
          onChange={this.onSearchChange}
          placeholder={intl.messages['invite.search']}
          type="search"
          value={search}/>
      </div>
    );
  }

  renderSelectedUsersCount() {
    const { selectedUserIds } = this.state;
    return (
      <div className="count">
        <FormattedMessage id="members" values={{ numMembers: selectedUserIds.size }}/>
      </div>
    );
  }

  renderCreateGroupButton() {
    const { step } = this.state;

    if (step !== CreateGroupSteps.CREATION_STARTED) {
      return (
        <button className="button button--lightblue" onClick={this.handleCreateGroup}>
          <FormattedMessage id="button.createGroup"/>
        </button>
      )
    }

    return (
      <button className="button button--lightblue" disabled>
        <FormattedMessage id="button.createGroup"/>
      </button>
    );
  }

  render() {
    const { step } = this.state;

    switch (step) {
      case CreateGroupSteps.NAME_INPUT:
        return (
          <form className="group-name">
            <div className="modal__body">
              {this.renderGroupNameInput()}
            </div>

            <footer className="modal__footer text-right">
              {this.renderAddUsersButton()}
            </footer>
          </form>
        );

      case CreateGroupSteps.CONTACTS_SELECTION:
      case CreateGroupSteps.CREATION_STARTED:
        return (
          <form className="group-members">
            <div className="modal__body">
              {this.renderUserSearchInput()}

              <ul className="contacts__list">
                {this.renderContacts()}
              </ul>
            </div>

            <footer className="modal__footer">
              <div className="row">
                <div className="col-xs text-left">
                  {this.renderSelectedUsersCount()}
                </div>
                <div className="col-xs text-right">
                  {this.renderCreateGroupButton()}
                </div>
              </div>
            </footer>
          </form>
        );
      default:
        return null;
    }
  }
}

export default Container.create(CreateGroupForm, { pure: false });
