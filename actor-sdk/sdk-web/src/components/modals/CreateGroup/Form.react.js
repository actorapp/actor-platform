/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import _ from 'lodash';

import React, { Component, PropTypes } from 'react';
import { Container } from 'flux/utils';
import ReactMixin from 'react-mixin';
import { IntlMixin, FormattedMessage } from 'react-intl';
import { CreateGroupSteps } from '../../../constants/ActorAppConstants';

import { Styles, TextField, FlatButton } from 'material-ui';

import CreateGroupActionCreators from '../../../actions/CreateGroupActionCreators';

import ContactStore from '../../../stores/ContactStore';
import CreateGroupStore from '../../../stores/CreateGroupStore';

import ContactItem from './ContactItem.react';

import ActorTheme from '../../../constants/ActorTheme';

const ThemeManager = new Styles.ThemeManager();

class CreateGroupForm extends Component {
  constructor(props) {
    super(props);
  }

  static childContextTypes = {
    muiTheme: PropTypes.object
  };

  static getStores() {
    return [CreateGroupStore];
  }

  static calculateState() {
    return {
      step: CreateGroupStore.getCurrentStep(),
      name: CreateGroupStore.getGroupName(),
      selectedUserIds: CreateGroupStore.getSelectedUserIds(),
      contacts: ContactStore.getContacts()
    }
  }

  getChildContext() {
    return {
      muiTheme: ThemeManager.getCurrentTheme()
    };
  }

  componentWillMount() {
    ThemeManager.setTheme(ActorTheme);
    ThemeManager.setComponentThemes({
      textField: {
        textColor: 'rgba(0,0,0,.87)',
        focusColor: '#68a3e7',
        backgroundColor: 'transparent',
        borderColor: '#68a3e7'
      }
    });
  }

  componentDidMount() {
    if (this.state.step === CreateGroupSteps.NAME_INPUT) {
      setTimeout(() => {
        this.refs.groupName.focus();
      }, 10);
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
    let stepForm;

    switch (step) {
      case CreateGroupSteps.NAME_INPUT:
        stepForm = (
          <form className="group-name">
            <div className="modal-new__body">
              <TextField className="login__form__input"
                         floatingLabelText={this.getIntlMessage('modal.createGroup.groupName')}
                         fullWidth
                         ref="groupName"
                         onChange={this.handleNameChange}
                         value={name}/>
            </div>

            <footer className="modal-new__footer text-right">
              <button className="button button--lightblue"
                      onClick={this.handleNameSubmit}>
                {this.getIntlMessage('button.addMembers')}
              </button>
            </footer>

          </form>
        );
        break;

      case CreateGroupSteps.CONTACTS_SELECTION:
      case CreateGroupSteps.CREATION_STARTED:
        let contactList = _.map(contacts, (contact, i) => {
          return (
            <ContactItem contact={contact} key={i} onToggle={this.onContactToggle}/>
          );
        });
        stepForm = (
          <form className="group-members">
            <div className="count">
              <FormattedMessage message={this.getIntlMessage('members')} numMembers={selectedUserIds.size}/>
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
                            disabled>{this.getIntlMessage('button.createGroup')}</button>
                  : <button className="button button--lightblue"
                            onClick={this.handleCreateGroup}>{this.getIntlMessage('button.createGroup')}</button>
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

ReactMixin.onClass(CreateGroupForm, IntlMixin);

export default Container.create(CreateGroupForm, {pure: false});
