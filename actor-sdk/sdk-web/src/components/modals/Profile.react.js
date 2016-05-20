/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { Container } from 'flux/utils';
import Modal from 'react-modal';
import { FormattedMessage } from 'react-intl';
import { ModalTypes } from '../../constants/ActorAppConstants';

import ProfileStore from '../../stores/ProfileStore';

import ProfileActionCreators from '../../actions/ProfileActionCreators';

import ModalCloseButton from './ModalCloseButton.react';
import TextField from '../common/TextField.react';
import PictureChanger from './profile/PictureChanger.react';

class Profile extends Component {
  static getStores() {
    return [ProfileStore];
  }

  static calculateState(prevState) {
    const state = ProfileStore.getState();
    return {
      profile: state.profile,
      name: prevState ? prevState.name : state.profile.name,
      nick: prevState ? prevState.nick : state.profile.nick,
      about: prevState ? prevState.about : state.profile.about,
      errors: prevState ? prevState.errors : {}
    };
  }

  static contextTypes = {
    intl: PropTypes.object
  };

  constructor(props, context) {
    super(props, context);

    this.handleClose = this.handleClose.bind(this);
    this.handleSave = this.handleSave.bind(this);
    this.handleNameChange = this.handleNameChange.bind(this);
    this.handleNickChange = this.handleNickChange.bind(this);
    this.handleAboutChange = this.handleAboutChange.bind(this);
    this.handleAvatarChange = this.handleAvatarChange.bind(this);
  }

  handleClose() {
    ProfileActionCreators.hide();
  }

  handleSave() {
    const { nick, name, about, profile, errors } = this.state;

    if (!errors.nick) {
      if (name !== profile.name) ProfileActionCreators.editMyName(name);
      if (nick !== profile.nick) ProfileActionCreators.editMyNick(nick);
      if (about !== profile.about) ProfileActionCreators.editMyAbout(about);
      this.handleClose();
    }
  }

  handleNameChange(event) {
    this.setState({ name: event.target.value });
  }

  handleNickChange(event) {
    const nick = event.target.value;

    this.setState({
      nick,
      errors: {
        nick: this.validateNick(nick)
      }
    });
  }

  validateNick(nick) {
    const { intl } = this.context;

    if (nick.length < 5 || nick.length > 32) {
      return intl.messages['modal.profile.errors.nick.length'];
    }

    if (!/^[0-9a-zA-Z_]+$/.test(nick)) {
      return intl.messages['modal.profile.errors.nick.chars'];
    }

    return null;
  }

  handleAboutChange(event) {
    this.setState({ about: event.target.value });
  }

  handleAvatarChange(croppedImage) {
    ProfileActionCreators.changeMyAvatar(croppedImage);
  }

  handleAvatarRemove() {
    ProfileActionCreators.removeMyAvatar()
  }

  renderControls() {
    const { errors } = this.state;

    return (
      <div className="controls">
        <button className="button" onClick={this.handleClose}>
          <FormattedMessage id="button.cancel"/>
        </button>
        <button
          className="button button--rised"
          disabled={errors.nick}
          onClick={this.handleSave}>
          <FormattedMessage id="button.save"/>
        </button>
      </div>
    );
  }

  renderName() {
    const { name } = this.state;

    return (
      <div className="name">
        <TextField
          className="input__material--wide"
          floatingLabel={<FormattedMessage id="modal.profile.name"/>}
          onChange={this.handleNameChange}
          type="text"
          value={name}/>
      </div>
    );
  }

  renderNick() {
    const { nick, errors } = this.state;

    return (
      <div className="nick">
        <TextField
          className="input__material--wide"
          floatingLabel={<FormattedMessage id="modal.profile.nick"/>}
          errorText={errors.nick}
          onChange={this.handleNickChange}
          type="text"
          value={nick}/>
      </div>
    );
  }

  renderPhones() {
    const { phones } = this.state.profile;
    if (phones.length === 0) return null;

    return phones.map((phone, index) => {
      return (
        <div className="phone" key={`p${index}`}>
          <TextField
            className="input__material--wide"
            floatingLabel={<FormattedMessage id="modal.profile.phone"/>}
            disabled
            type="tel"
            value={phone.number}/>
        </div>
      );
    });
  }

  renderEmails() {
    const { emails } = this.state.profile;
    if (emails.length === 0) return null;

    return emails.map((email, index) => {
      return (
        <div className="email" key={`e${index}`}>
          <TextField
            className="input__material--wide"
            floatingLabel={<FormattedMessage id="modal.profile.email"/>}
            disabled
            type="email"
            value={email.email}/>
        </div>
      );
    });
  }

  renderAbout() {
    const { about } = this.state;

    return (
      <div className="about">
        <label htmlFor="about"><FormattedMessage id="modal.profile.about"/></label>
        <textarea
          className="textarea"
          id="about"
          onChange={this.handleAboutChange}
          value={about}/>
      </div>
    );
  }

  render() {
    const { profile } = this.state;

    return (
      <Modal
        overlayClassName="modal-overlay modal-overlay--white"
        className="modal modal--fullscreen"
        onRequestClose={this.handleClose}
        shouldCloseOnOverlayClick={false}
        isOpen>

        <ModalCloseButton onClick={this.handleClose}/>

        <div className="profile">
          <div className="modal__content">

            <header className="modal__header">
              <FormattedMessage id="modal.profile.title" tagName="h1"/>
            </header>

            <div className="modal__body row">
              <div className="col-xs">
                {this.renderName()}
                {this.renderNick()}
                {this.renderPhones()}
                {this.renderEmails()}
                {this.renderAbout()}
              </div>

              <PictureChanger {...profile}
                fromModal={ModalTypes.PROFILE}
                onRemove={this.handleAvatarRemove}
                onChange={this.handleAvatarChange}/>
            </div>

            <footer className="modal__footer">
              {this.renderControls()}
            </footer>

          </div>
        </div>

      </Modal>
    );
  }
}

export default Container.create(Profile);
