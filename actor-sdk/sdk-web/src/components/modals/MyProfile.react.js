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

import TextField from '../common/TextField.react';

let currentName = '',
    currentNick = '',
    currentAbout = '';

class MyProfile extends Component {
  constructor(props) {
    super(props);
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
    const { name, nick, about } = this.state;

    currentName = name;
    currentNick = nick;
    currentAbout = about;

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

  handleClose = () => MyProfileActions.hide();

  onKeyDown = event => {
    if (event.keyCode === KeyCodes.ESC) {
      event.preventDefault();
      this.handleClose();
    }
  };

  handleNameChange = event => this.setState({name: event.target.value});
  handleNicknameChange = event => this.setState({nick: event.target.value});
  handleAboutChange = event => this.setState({about: event.target.value});

  isProfileChanged = () => {
    const { name, nick, about } = this.state;
    return name !== currentName || nick !== currentNick || about !== currentAbout
  };

  handleSave = () => {
    const { nick, name, about } = this.state;

    if (name !== currentName) MyProfileActions.saveName(name);
    if (nick !== currentNick) MyProfileActions.saveNickname(nick);
    if (about !== currentAbout) MyProfileActions.editMyAbout(about);

    this.handleClose();
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

  handleChangeAvatarClick = () => {
    const imageInput = React.findDOMNode(this.refs.imageInput);
    imageInput.click()
  };

  onProfilePictureRemove = () => MyProfileActions.removeMyAvatar();

  changeMyAvatar = (croppedImage) => MyProfileActions.changeMyAvatar(croppedImage);

  render() {
    const { isOpen, isCropModalOpen, profile, nick, name, about } = this.state;
    const isProfileChanged = this.isProfileChanged();

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
              {
                isProfileChanged
                  ? <button className="button button--lightblue" onClick={this.handleSave}>{this.getIntlMessage('button.save')}</button>
                  : <button className="button" onClick={this.handleClose}>{this.getIntlMessage('button.close')}</button>
              }
            </div>
          </header>
          <div className="modal-new__body row">
            <div className="col-xs">
              <div className="name">
                <TextField className="input__material--wide"
                           floatingLabel={this.getIntlMessage('modal.profile.name')}
                           onChange={this.handleNameChange}
                           type="text"
                           value={name}/>
              </div>
              <div className="nick">
                <TextField className="input__material--wide"
                           floatingLabel={this.getIntlMessage('modal.profile.nick')}
                           onChange={this.handleNicknameChange}
                           type="text"
                           value={nick}/>
              </div>
              {
                profile.phones[0]
                  ? <div className="phone">
                      <TextField className="input__material--wide"
                                 floatingLabel={this.getIntlMessage('modal.profile.phone')}
                                 disabled
                                 type="tel"
                                 value={(profile.phones[0] || {}).number}/>
                    </div>
                  : null
              }
              {
                profile.emails[0]
                  ? <div className="phone">
                      <TextField className="input__material--wide"
                                 floatingLabel={this.getIntlMessage('modal.profile.email')}
                                 disabled
                                 type="email"
                                 value={(profile.emails[0] || {}).email}/>
                    </div>
                  : null
              }
              <div className="about">
                <label htmlFor="about">{this.getIntlMessage('modal.profile.about')}</label>
                <textarea className="textarea"
                          id="about"
                          onChange={this.handleAboutChange}
                          value={about}/>
              </div>
            </div>
            <div className="profile-picture text-center">
              <div className="profile-picture__changer">
                <AvatarItem image={profile.bigAvatar}
                            placeholder={profile.placeholder}
                            size="big"
                            title={profile.name}/>
                <a onClick={this.handleChangeAvatarClick}>
                  <span>{this.getIntlMessage('modal.profile.avatarChange')}</span>
                </a>
              </div>
              {
                profile.bigAvatar
                  ? <div className="profile-picture__controls">
                      <a onClick={this.onProfilePictureRemove}>{this.getIntlMessage('modal.profile.avatarRemove')}</a>
                    </div>
                  : null
              }
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
