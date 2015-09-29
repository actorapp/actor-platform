import _ from 'lodash';
import Immutable from 'immutable';
import keymirror from 'keymirror';
import ReactMixin from 'react-mixin';
import { IntlMixin, FormattedMessage } from 'react-intl';

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

@ReactMixin.decorate(IntlMixin)
class CreateGroupForm extends React.Component {
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
                         floatingLabelText={this.getIntlMessage('createGroupGroupName')}
                         fullWidth
                         onChange={this.onNameChange}
                         value={this.state.name}/>

            </div>

            <footer className="modal-new__footer text-right">
              <button className="button button--lightblue" type="submit">{this.getIntlMessage('createGroupAddMembers')}</button>
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

            <div className="count">
              <FormattedMessage message={this.getIntlMessage('members')} numMembers={this.state.selectedUserIds.size}/>
            </div>

            <div className="modal-new__body">
              <ul className="contacts__list">
                {contactList}
              </ul>
            </div>

            <footer className="modal-new__footer text-right">
              <button className="button button--lightblue" type="submit">{this.getIntlMessage('createGroupButton')}</button>
            </footer>
          </form>
        );
        break;
    }

    return stepForm;
  }

  onContactToggle = (contact, isSelected) => {
    const { selectedUserIds } = this.state;

    if (isSelected) {
      this.setState({selectedUserIds: selectedUserIds.add(contact.uid)});
    } else {
      this.setState({selectedUserIds: selectedUserIds.remove(contact.uid)});
    }
  }

  onNameChange = event => {
    event.preventDefault();

    this.setState({name: event.target.value});
  }

  onNameSubmit = event => {
    const { name } = this.state;

    event.preventDefault();
    if (name) {
      const trimmedName = name.trim();
      if (trimmedName.length > 0) {
        this.setState({step: STEPS.CONTACTS_SELECTION});
      }
    }
  }

  onMembersSubmit = event => {
    const { name, selectedUserIds } = this.state;

    event.preventDefault();
    CreateGroupActionCreators.createGroup(name, null, selectedUserIds.toJS());
  }
}

export default CreateGroupForm;
