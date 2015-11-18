/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import { Container } from 'flux/utils';
import Modal from 'react-modal';
import ReactMixin from 'react-mixin';
import { IntlMixin } from 'react-intl';

import ActorClient from '../../utils/ActorClient';
import { KeyCodes } from '../../constants/ActorAppConstants';

import MyProfileActions from '../../actions/MyProfileActionCreators';
import CropAvatarActionCreators from '../../actions/CropAvatarActionCreators';

import MyProfileStore from '../../stores/MyProfileStore';
import CropAvatarStore from '../../stores/CropAvatarStore';

import AvatarItem from '../common/AvatarItem.react';
import CropAvatarModal from './CropAvatar.react';

import { Styles, TextField } from 'material-ui';
import ActorTheme from '../../constants/ActorTheme';

const ThemeManager = new Styles.ThemeManager();

class MyProfile extends Component {
  constructor(props) {
    super(props);
  }

  static childContextTypes = {
    muiTheme: React.PropTypes.object
  };

  getChildContext() {
    return {
      muiTheme: ThemeManager.getCurrentTheme()
    };
  }

  static getStores = () => [MyProfileStore, CropAvatarStore];

  static calculateState() {
    return {
      profile: MyProfileStore.getProfile(),
      name: MyProfileStore.getName(),
      nick: MyProfileStore.getNick(),
      about: MyProfileStore.getAbout(),
      isOpen: MyProfileStore.isModalOpen(),
      isCropModalOpen: CropAvatarStore.isOpen()
    };
  }

  componentWillMount() {
    ThemeManager.setTheme(ActorTheme);
    ThemeManager.setComponentThemes({
      textField: {
        textColor: 'rgba(0,0,0,.87)',
        focusColor: '#68a3e7',
        backgroundColor: 'transparent',
        borderColor: '#68a3e7',
        disabledTextColor: 'rgba(0,0,0,.4)'
      }
    });
    this.setListeners();
  }

  componentWillUnmount() {
    this.removeListeners();
  }

  componentWillUpdate(nextProps, nextState) {
    if (nextState.isOpen) {
      if (nextState.isCropModalOpen) {
        this.removeListeners();
      } else {
        this.setListeners();
      }
    }
  }

  setListeners = () => document.addEventListener('keydown', this.onKeyDown, false);
  removeListeners = () => document.removeEventListener('keydown', this.onKeyDown, false);

  onClose = () => MyProfileActions.hide();

  onKeyDown = event => {
    if (event.keyCode === KeyCodes.ESC) {
      event.preventDefault();
      this.onClose();
    }
  };

  onNameChange = event => this.setState({name: event.target.value});
  onNicknameChange = event => this.setState({nick: event.target.value});
  onAboutChange = event => this.setState({about: event.target.value});

  onSave = () => {
    const { nick, name, about } = this.state;

    MyProfileActions.saveName(name);
    MyProfileActions.saveNickname(nick);
    MyProfileActions.editMyAbout(about);
    this.onClose();
  };

  onProfilePictureInputChange = () => {
    const imageInput = React.findDOMNode(this.refs.imageInput);
    const imageForm = React.findDOMNode(this.refs.imageForm);
    const file = imageInput.files[0];

    let reader = new FileReader();
    reader.onload = (event) => {
      CropAvatarActionCreators.show(event.target.result);
      imageForm.reset();
    };
    reader.readAsDataURL(file);
  };

  onChangeAvatarClick = () => {
    const imageInput = React.findDOMNode(this.refs.imageInput);
    imageInput.click()
  };

  onProfilePictureRemove = () => MyProfileActions.removeMyAvatar();

  changeMyAvatar = (croppedImage) => MyProfileActions.changeMyAvatar(croppedImage);

  render() {
    const { isOpen, isCropModalOpen, profile, nick, name, about } = this.state;

    const cropAvatar = isCropModalOpen ? <CropAvatarModal onCropFinish={this.changeMyAvatar}/> : null;

    if (profile !== null && isOpen) {
      return (
        <Modal className="modal-new modal-new--profile"
               closeTimeoutMS={150}
               isOpen={isOpen}
               style={{width: 440}}>

          <header className="modal-new__header">
            <a className="modal-new__header__icon material-icons">person</a>
            <h3 className="modal-new__header__title">{this.getIntlMessage('modal.profile.title')}</h3>
            <div className="pull-right">
              <button className="button button--lightblue" onClick={this.onSave}>{this.getIntlMessage('button.done')}</button>
            </div>
          </header>
          <div className="modal-new__body row">
            <div className="col-xs">
              <div className="name">
                <TextField className="login__form__input"
                           floatingLabelText={this.getIntlMessage('modal.profile.name')}
                           fullWidth
                           onChange={this.onNameChange}
                           type="text"
                           value={name}/>
              </div>
              <div className="nick">
                <TextField className="login__form__input"
                           floatingLabelText={this.getIntlMessage('modal.profile.nick')}
                           fullWidth
                           onChange={this.onNicknameChange}
                           type="text"
                           value={nick}/>
              </div>
              {
                profile.phones[0]
                  ? <div className="phone">
                      <TextField className="login__form__input"
                                 disabled
                                 floatingLabelText={this.getIntlMessage('modal.profile.phone')}
                                 fullWidth
                                 type="tel"
                                 value={(profile.phones[0] || {}).number}/>
                    </div>
                  : null
              }
              {
                profile.emails[0]
                  ? <div className="phone">
                      <TextField className="login__form__input"
                                 disabled
                                 floatingLabelText={this.getIntlMessage('modal.profile.email')}
                                 fullWidth
                                 type="email"
                                 value={(profile.emails[0] || {}).email}/>
                    </div>
                  : null
              }
              <div className="about">
                <label htmlFor="about">{this.getIntlMessage('modal.profile.about')}</label>
                <textarea className="textarea"
                          id="about"
                          onChange={this.onAboutChange}
                          value={about}/>
              </div>
            </div>
            <div className="profile-picture text-center">
              <div className="profile-picture__changer">
                <AvatarItem image={profile.bigAvatar}
                            placeholder={profile.placeholder}
                            size="big"
                            title={profile.name}/>
                <a onClick={this.onChangeAvatarClick}>
                  <span>
                    {this.getIntlMessage('modal.profile.avatarChange')}
                  </span>
                </a>
              </div>
              <div className="profile-picture__controls">
                <a onClick={this.onProfilePictureRemove}>{this.getIntlMessage('modal.profile.avatarRemove')}</a>
              </div>
              <form className="hide" ref="imageForm">
                <input onChange={this.onProfilePictureInputChange} ref="imageInput" type="file"/>
              </form>
            </div>
          </div>

          {cropAvatar}
        </Modal>
      );
    } else {
      return null;
    }
  }
}

ReactMixin.onClass(MyProfile, IntlMixin);

export default Container.create(MyProfile, {pure: false});
