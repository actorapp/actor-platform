import _ from 'lodash';

import React from 'react';
import Modal from 'react-modal';
import addons from 'react/addons';
import ReactMixin from 'react-mixin';

import { Styles, TextField, FlatButton } from 'material-ui';

import AddContactStore from 'stores/AddContactStore';
import AddContactActionCreators from 'actions/AddContactActionCreators';

import classNames from 'classnames';

import { KeyCodes } from 'constants/ActorAppConstants';
import ActorTheme from 'constants/ActorTheme';

const ThemeManager = new Styles.ThemeManager();

const appElement = document.getElementById('actor-web-app');
Modal.setAppElement(appElement);

const getStateFromStores = () => {
  return {
    isOpen: AddContactStore.isModalOpen(),
    message: AddContactStore.getMessage()
  };
};

const {addons: { PureRenderMixin }} = addons;

@ReactMixin.decorate(PureRenderMixin)
class AddContact extends React.Component {
  static childContextTypes = {
    muiTheme: React.PropTypes.object
  };

  getChildContext() {
    return {
      muiTheme: ThemeManager.getCurrentTheme()
    };
  }

  constructor(props) {
    super(props);

    this.state = _.assign({
      phone: ''
    }, getStateFromStores());

    ThemeManager.setTheme(ActorTheme);
    ThemeManager.setComponentThemes({
      textField: {
        textColor: 'rgba(0,0,0,.87)',
        focusColor: '#68a3e7',
        backgroundColor: 'transparent',
        borderColor: '#68a3e7'
      }
    });

    AddContactStore.addChangeListener(this.onChange);
  }

  componentWillUnmount() {
    AddContactStore.removeChangeListener(this.onChange);
  }

  componentWillUpdate(nextProps, nextState) {
    if (nextState.isOpen && !this.state.isOpen) {
      document.addEventListener('keydown', this.onKeyDown, false);
    } else if (!nextState.isOpen && this.state.isOpen) {
      document.removeEventListener('keydown', this.onKeyDown, false);
    }
  }

  render() {
    const { isOpen, message, phone } = this.state;

    const messageClassName = classNames({
      'error-message': true,
      'error-message--shown': message
    });

    if (isOpen) {
      return (
        <Modal className="modal-new modal-new--add-contact"
               closeTimeoutMS={150}
               isOpen={isOpen}
               style={{width: 320}}>

          <header className="modal-new__header">
            <a className="modal-new__header__close modal-new__header__icon material-icons"
               onClick={this.onClose}>clear</a>
            <h3 className="modal-new__header__title">Add contact</h3>
          </header>

          <div className="modal-new__body">
            <TextField className="login__form__input"
                       floatingLabelText="Phone number"
                       fullWidth
                       onChange={this.onPhoneChange}
                       type="text"
                       value={phone}/>
          </div>

          <span className={messageClassName}>{message}</span>

          <footer className="modal-new__footer text-right">
            <FlatButton hoverColor="rgba(74,144,226,.12)"
                        label="Add"
                        onClick={this.onAddContact}
                        secondary={true} />
          </footer>

        </Modal>
      );
    } else {
      return null;
    }
  }

  onClose = () => {
    AddContactActionCreators.closeModal();
  };

  onPhoneChange = event => {
    this.setState({phone: event.target.value});
  };

  onAddContact = () => {
    AddContactActionCreators.findUsers(this.state.phone);
  };

  onChange = () => {
    this.setState(getStateFromStores());
  };

  onKeyDown = (event) => {
    if (event.keyCode === KeyCodes.ESC) {
      event.preventDefault();
      this.onClose();
    }
  }
}

export default AddContact;
