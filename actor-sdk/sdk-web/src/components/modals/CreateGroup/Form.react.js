/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { map } from 'lodash';

import React, { Component, PropTypes } from 'react';
import { Container } from 'flux/utils';
import { FormattedMessage } from 'react-intl';
import { CreateGroupSteps } from '../../../constants/ActorAppConstants';

import CreateGroupActionCreators from '../../../actions/CreateGroupActionCreators';

import ContactStore from '../../../stores/PeopleStore';
import CreateGroupStore from '../../../stores/CreateGroupStore';

import ContactItem from './ContactItem.react';

import TextField from '../../common/TextField.react';

class CreateGroupForm extends Component {
  constructor(props) {
    super(props);
  }

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

  static contextTypes = {
    intl: PropTypes.object
  };

  componentDidMount() {
    if (this.state.step === CreateGroupSteps.NAME_INPUT) {
      this.refs.groupName.focus();
    }
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

  render() {
    const { step, name, selectedUserIds, contacts } = this.state;
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
        let contactList = map(contacts, (contact, i) => {
          return (
            <ContactItem contact={contact} key={i} onToggle={this.onContactToggle}/>
          );
        });
        stepForm = (
          <form className="group-members">
            <div className="count">
              <FormattedMessage id="members" values={{numMembers: selectedUserIds.size}}/>
            </div>

            <div className="modal-new__body">
              <ul className="contacts__list">
                {contactList}
              </ul>
            </div>

            <footer className="modal-new__footer text-right">
              {
                step === CreateGroupSteps.CREATION_STARTED
                  ? <button className="button button--lightblue"
                            disabled>{intl.messages['button.createGroup']}</button>
                  : <button className="button button--lightblue"
                            onClick={this.handleCreateGroup}>{intl.messages['button.createGroup']}</button>
              }

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
