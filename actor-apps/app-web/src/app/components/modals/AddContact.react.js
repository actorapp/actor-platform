/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import _ from 'lodash';

import React, { Component, PropTypes } from 'react';
import { Container } from 'flux/utils';
import Modal from 'react-modal';
import ReactMixin from 'react-mixin';
import { IntlMixin } from 'react-intl';

// TODO: get rid of the material-ui
import { Styles, TextField } from 'material-ui';

import AddContactStore from 'stores/AddContactStore';
import AddContactActionCreators from 'actions/AddContactActionCreators';

import classNames from 'classnames';

import { KeyCodes, AddContactMessages } from 'constants/ActorAppConstants';
import ActorTheme from 'constants/ActorTheme';

const ThemeManager = new Styles.ThemeManager();

@ReactMixin.decorate(IntlMixin)
class AddContact extends Component {
  static getStores = () => [AddContactStore];

  static calculateState() {
    return {
      isOpen: AddContactStore.isModalOpen(),
      message: AddContactStore.getMessage(),
      query: AddContactStore.getQuery()
    };
  }

  static childContextTypes = {
    muiTheme: PropTypes.object
  };

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

    document.addEventListener('keydown', this.handleKeyDown, false);
  }

  componentDidMount() {
    setTimeout(() => {
      this.refs.query.focus();
    }, 10)
  }

  componentWillUnmount() {
    document.removeEventListener('keydown', this.handleKeyDown, false);
  }

  handleClose = () => AddContactActionCreators.close();

  handleQueryChange = event => this.setState({query: event.target.value});

  addContact = () => AddContactActionCreators.findUsers(this.state.query);

  handleKeyDown = (event) => {
    if (event.keyCode === KeyCodes.ESC) {
      event.preventDefault();
      this.handleClose();
    } else if (event.keyCode === KeyCodes.ENTER) {
      event.preventDefault();
      this.addContact()
    }
  };

  render() {
    const { isOpen, message, query } = this.state;

    const messageClassName = classNames({
      'error-message': true,
      'error-message--shown': message
    });

    let messageText;
    switch (message) {
      case AddContactMessages.PHONE_NOT_REGISTERED:
        messageText = this.getIntlMessage('modal.addContact.error.notRegistered');
        break;
      case AddContactMessages.ALREADY_HAVE:
        messageText = this.getIntlMessage('modal.addContact.error.inContacts');
        break;
      default:
    }

    return (
      <Modal className="modal-new modal-new--add-contact"
             closeTimeoutMS={150}
             isOpen={isOpen}
             style={{width: 320}}>

        <header className="modal-new__header">
          <h3 className="modal-new__header__title">{this.getIntlMessage('modal.addContact.title')}</h3>
          <a className="modal-new__header__close modal-new__header__icon material-icons pull-right"
             onClick={this.handleClose}>clear</a>
        </header>

        <div className="modal-new__body">
          <TextField className="login__form__input"
                     floatingLabelText={this.getIntlMessage('modal.addContact.query')}
                     fullWidth
                     onChange={this.handleQueryChange}
                     ref="query"
                     value={query}/>
        </div>

        <span className={messageClassName}>{messageText}</span>

        <footer className="modal-new__footer text-right">
          <button className="button button--lightblue" onClick={this.addContact} type="submit">
            {this.getIntlMessage('button.add')}
          </button>
        </footer>

      </Modal>
    );
  }
}

export default Container.create(AddContact);
