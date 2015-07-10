import React from 'react';
import Modal from 'react-modal';
import { Styles, TextField, FlatButton } from 'material-ui';

import AddContactStore from '../../stores/AddContactStore';
import AddContactActionCreators from '../../actions/AddContactActionCreators';
// import { KeyCodes } from '../../constants/ActorAppConstants';

import ActorTheme from '../../constants/ActorTheme';

const ThemeManager = new Styles.ThemeManager();

const appElement = document.getElementById('actor-web-app');
Modal.setAppElement(appElement);

const getStateFromStores = () => {
  return {
    isShown: AddContactStore.isModalOpen(),
    phone: ''
  };
};

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

    this.state = getStateFromStores();

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
    return (
      <Modal className="modal-new modal-new--add-contact"
             closeTimeoutMS={150}
             isOpen={this.state.isShown}>

        <header className="modal-new__header">
          <a className="modal-new__header__close material-icons"
             onClick={this.onClose}>clear</a>
          <h3 className="modal-new__header__title">Add contact</h3>
        </header>

        <div className="modal-new__body">
          <TextField className="login__form__input"
                     floatingLabelText="Phone number"
                     fullWidth
                     onChange={this.onPhoneChange}
                     type="tel"
                     value={this.state.phone}/>
        </div>

        <footer className="modal-new__footer text-right">
          <FlatButton label="Add contact"
                      onClick={this.onAddContact}
                      secondary={true} />
        </footer>

      </Modal>
    );
  }

  onClose = () => {
    AddContactActionCreators.closeModal();
  }

  onPhoneChange = event => {
    this.setState({phone: event.target.value});
  }

  onAddContact = () => {
    AddContactActionCreators.findUsers(this.state.phone);
  }
}

export default AddContact;
