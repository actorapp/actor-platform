import _ from 'lodash';
import Immutable from 'immutable';
import keymirror from 'keymirror';

import React from 'react';
import { Styles, TextField, FlatButton } from 'material-ui';

import CreateGroupActionCreators from 'actions/CreateGroupActionCreators';

import ContactStore from 'stores/ContactStore';

import ContactItem from './ContactItem.react';

import ActorTheme from 'constants/ActorTheme';

const ThemeManager = new Styles.ThemeManager();

const STEPS = keymirror({
  NAME_INPUT: null,
  CONTACTS_SELECTION: null
});

class CreateGroupForm extends React.Component {
  static displayName = 'CreateGroupForm'

  static childContextTypes = {
    muiTheme: React.PropTypes.object
  };

  state = {
    step: STEPS.NAME_INPUT,
    selectedUserIds: new Immutable.Set()
  }

  getChildContext() {
    return {
      muiTheme: ThemeManager.getCurrentTheme()
    };
  }

  constructor(props) {
    super(props);

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

  render() {
    let stepForm;

    switch (this.state.step) {
      case STEPS.NAME_INPUT:
        stepForm = (
          <form className="group-name" onSubmit={this.onNameSubmit}>

            <div className="modal-new__body">
              <TextField className="login__form__input"
                         floatingLabelText="Group name"
                         fullWidth
                         onChange={this.onNameChange}
                         value={this.state.name}/>

            </div>

            <footer className="modal-new__footer text-right">
              <FlatButton hoverColor="rgba(74,144,226,.12)"
                          label="Add members"
                          secondary={true}
                          type="submit"/>
            </footer>

          </form>
        );
        break;
      case STEPS.CONTACTS_SELECTION:
        let contactList = _.map(ContactStore.getContacts(), (contact, i) => {
          return (
            <ContactItem contact={contact} key={i} onToggle={this.onContactToggle}/>
          );
        });

        stepForm = (
          <form className="group-members" onSubmit={this.onMembersSubmit}>

            <div className="count">{this.state.selectedUserIds.size} Members</div>

            <div className="modal-new__body">
              <ul className="contacts__list">
                {contactList}
              </ul>
            </div>

            <footer className="modal-new__footer text-right">
              <FlatButton hoverColor="rgba(74,144,226,.12)"
                          label="Create group"
                          secondary={true}
                          type="submit"/>

            </footer>
          </form>
        );
        break;
    }

    return stepForm;
  }

  onContactToggle = (contact, isSelected) => {
    if (isSelected) {
      this.setState({selectedUserIds: this.state.selectedUserIds.add(contact.uid)});
    } else {
      this.setState({selectedUserIds: this.state.selectedUserIds.remove(contact.uid)});
    }
  }

  onNameChange = event => {
    event.preventDefault();

    this.setState({name: event.target.value});
  }

  onNameSubmit = event => {
    event.preventDefault();
    if (this.state.name) {
      let name = this.state.name.trim();
      if (name.length > 0) {
        this.setState({step: STEPS.CONTACTS_SELECTION});
      }
    }
  }

  onMembersSubmit =event => {
    event.preventDefault();
    CreateGroupActionCreators.createGroup(this.state.name, null, this.state.selectedUserIds.toJS());
  }
}

export default CreateGroupForm;
