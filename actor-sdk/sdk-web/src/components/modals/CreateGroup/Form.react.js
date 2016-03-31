/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { map } from 'lodash';

import React, { Component, PropTypes } from 'react';
import { Container } from 'flux/utils';
import { FormattedMessage } from 'react-intl';
import fuzzaldrin from 'fuzzaldrin';
import { CreateGroupSteps } from '../../../constants/ActorAppConstants';

import CreateGroupActionCreators from '../../../actions/CreateGroupActionCreators';

import ContactStore from '../../../stores/PeopleStore';
import CreateGroupStore from '../../../stores/CreateGroupStore';

import ContactItem from '../../common/ContactItem.react';
import TextField from '../../common/TextField.react';

class CreateGroupForm extends Component {
  static contextTypes = {
    intl: PropTypes.object
  };

  static getStores() {
    return [ContactStore, CreateGroupStore];
  }

  static calculateState() {
    return {
      step: CreateGroupStore.getCurrentStep(),
      name: CreateGroupStore.getGroupName(),
      selectedUserIds: CreateGroupStore.getSelectedUserIds(),
      contacts: ContactStore.getList()
    }
  }

  componentDidMount() {
    if (this.state.step === CreateGroupSteps.NAME_INPUT) {
      this.refs.groupName.focus();
    }
  }

  getContacts() {
    const { contacts, search } = this.state;

    return fuzzaldrin.filter(contacts, search, {
      key: 'name'
    });
  }

  renderContacts() {
    const { intl } = this.context;
    const { selectedUserIds } = this.state;
    const contacts = this.getContacts();

    if (!contacts.length) {
      return (
        <li className="contacts__list__item contacts__list__item--empty text-center">
          {intl.messages['inviteModalNotFound']}
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

  onContactToggle = (contact, isSelected) => {
    const { selectedUserIds } = this.state;

    if (isSelected) {
      CreateGroupActionCreators.setSelectedUserIds(selectedUserIds.add(contact.uid));
    } else {
      CreateGroupActionCreators.setSelectedUserIds(selectedUserIds.remove(contact.uid));
    }
  };

  handleNameChange = event => {
    event.preventDefault();

    this.setState({name: event.target.value});
  };

  handleNameSubmit = event => {
    event.preventDefault();

    const { name } = this.state;
    const trimmedName = name.trim();

    if (trimmedName.length > 0) {
      CreateGroupActionCreators.setGroupName(trimmedName);
    }
  };

  handleCreateGroup = event => {
    const { name, selectedUserIds } = this.state;

    event.preventDefault();
    CreateGroupActionCreators.createGroup(name, null, selectedUserIds.toJS());
  };

  onSearchChange = (e) => {
    this.setState({search: e.target.value});
  };

  render() {
    const { step, name, selectedUserIds, search } = this.state;
    const { intl } = this.context;
    let stepForm;

    switch (step) {
      case CreateGroupSteps.NAME_INPUT:
        stepForm = (
          <form className="group-name">
            <div className="modal-new__body">
              <TextField className="input__material--wide"
                         floatingLabel={intl.messages['modal.createGroup.groupName']}
                         ref="groupName"
                         onChange={this.handleNameChange}
                         value={name}/>
            </div>

            <footer className="modal-new__footer text-right">
              <button className="button button--lightblue"
                      onClick={this.handleNameSubmit}>
                {intl.messages['button.addMembers']}
              </button>
            </footer>

          </form>
        );
        break;

      case CreateGroupSteps.CONTACTS_SELECTION:
      case CreateGroupSteps.CREATION_STARTED:
        stepForm = (
          <form className="group-members">
            <div className="modal-new__body">
              {/*TODO: refactor this!!! */}
              <div className="modal-new__search">
                <i className="material-icons">search</i>
                <input className="input input--search"
                       onChange={this.onSearchChange}
                       placeholder={intl.messages['inviteModalSearch']}
                       type="search"
                       value={search}/>
              </div>

              <ul className="contacts__list">
                {this.renderContacts()}
              </ul>
            </div>

            <footer className="modal-new__footer ">
              <span className="pull-left">
                {
                  step === CreateGroupSteps.CONTACTS_SELECTION ||
                  step === CreateGroupSteps.CREATION_STARTED
                    ? <div className="count">
                        <FormattedMessage id="members" values={{numMembers: selectedUserIds.size}}/>
                      </div>
                    : null
                }
              </span>
              <span className="text-right">
                {
                  step === CreateGroupSteps.CREATION_STARTED
                    ? <button className="button button--lightblue"
                              disabled>{intl.messages['button.createGroup']}</button>
                    : <button className="button button--lightblue"
                              onClick={this.handleCreateGroup}>{intl.messages['button.createGroup']}</button>
                }
              </span>

            </footer>
          </form>
        );
        break;
      default:
    }

    return stepForm;
  }
}

export default Container.create(CreateGroupForm, {pure: false});
