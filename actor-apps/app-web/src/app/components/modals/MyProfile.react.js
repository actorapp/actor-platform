//import _ from 'lodash';

import React from 'react';

import { KeyCodes } from 'constants/ActorAppConstants';

import MyProfileActions from 'actions/MyProfileActions';
import MyProfileStore from 'stores/MyProfileStore';

import AvatarItem from 'components/common/AvatarItem.react';

import Modal from 'react-modal';
//import classNames from 'classnames';
import { Styles, TextField, FlatButton } from 'material-ui';
import ActorTheme from 'constants/ActorTheme';

const ThemeManager = new Styles.ThemeManager();


const getStateFromStores = () => {
  return {
    profile: MyProfileStore.getProfile(),
    name: MyProfileStore.getName(),
    isOpen: MyProfileStore.isModalOpen(),
    isNameEditable: false
  };
};

class MyProfile extends React.Component {
  static childContextTypes = {
    muiTheme: React.PropTypes.object
  };

  getChildContext() {
    return {
      muiTheme: ThemeManager.getCurrentTheme()
    };
  }

  componentWillUnmount() {
    this.unsubscribe();
    document.removeEventListener('keydown', this.onKeyDown, false);
  }

  constructor(props) {
    super(props);

    this.state = getStateFromStores();

    this.unsubscribe = MyProfileStore.listen(this.onChange);
    document.addEventListener('keydown', this.onKeyDown, false);

    ThemeManager.setTheme(ActorTheme);
    ThemeManager.setComponentThemes({
      button: {
        minWidth: 60
      },
      textField: {
        textColor: 'rgba(0,0,0,.87)',
        focusColor: '#68a3e7',
        backgroundColor: 'transparent',
        borderColor: '#68a3e7',
        disabledTextColor: 'rgba(0,0,0,.4)'
      }
    });
  }

  onClose = () => {
    MyProfileActions.modalClose();
  }

  onKeyDown = event => {
    if (event.keyCode === KeyCodes.ESC) {
      event.preventDefault();
      this.onClose();
    }
  }

  onChange = () => {
    this.setState(getStateFromStores());
  }

  onNameChange = event => {
    this.setState({name: event.target.value});
  }

  onNameSave = () => {
    MyProfileActions.setName(this.state.name);
    this.onClose();
  }

  render() {
    let isOpen = this.state.isOpen;
    let profile = this.state.profile;

    if (profile !== null && isOpen === true) {
      return (
        <Modal className="modal-new modal-new--profile"
               closeTimeoutMS={150}
               isOpen={isOpen}
               style={{width: 340}}>
          <header className="modal-new__header">
            <a className="modal-new__header__icon material-icons">person</a>
            <h4 className="modal-new__header__title">Profile</h4>
            <div className="pull-right">
              <FlatButton hoverColor="rgba(74,144,226,.12)"
                          label="Done"
                          labelStyle={{padding: '0 8px'}}
                          onClick={this.onNameSave}
                          secondary={true}
                          style={{marginTop: -6}}/>
            </div>

          </header>
          <div className="modal-new__body row">
            <AvatarItem image={profile.bigAvatar}
                        placeholder={profile.placeholder}
                        size="big"
                        title={profile.name}/>
            <div className="col-xs">
              <div className="name">
                <TextField className="login__form__input"
                           floatingLabelText="Username"
                           fullWidth
                           onChange={this.onNameChange}
                           type="text"
                           value={this.state.name}/>
              </div>
              <div className="phone">
                <TextField className="login__form__input"
                           disabled
                           floatingLabelText="Phone number"
                           fullWidth
                           type="tel"
                           value={this.state.profile.phones[0].number}/>
              </div>
              {/*
              <ul className="modal-new__body__list hide">
                <li>
                  <a>
                    Send message
                  </a>
                </li>
                <li>
                  <a className="color--red">
                    Block user
                  </a>
                </li>
              </ul>
              */}
            </div>
          </div>
        </Modal>
      );
    } else {
      return null;
    }
  }
}

export default MyProfile;
