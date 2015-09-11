/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import { Container } from 'flux/utils';

import Modal from 'react-modal';

import { KeyCodes } from 'constants/ActorAppConstants';

import ProfilePictureActionCreators from 'actions/ProfilePictureActionCreators';

import ProfilePictureStore from 'stores/ProfilePictureStore'

class ProfilePictureModal extends Component {
  static getStores = () => [ProfilePictureStore];

  static calculateState() {
    return {
      isOpen: ProfilePictureStore.isOpen()
    };
  }

  componentWillUpdate(nextProps, nextState) {
    const { isOpen } = nextState;

    if (isOpen) {
      document.addEventListener('keydown', this.onKeyDown, false);
    } else {
      document.removeEventListener('keydown', this.onKeyDown, false);
    }
  }

  onClose = () => {
    ProfilePictureActionCreators.hide();
    this.setState({profilePhoto: null});
  };

  onKeyDown = (event) => {
    if (event.keyCode === KeyCodes.ESC) {
      event.preventDefault();
      this.onClose();
    }
  };

  onProfilePhotoChange = () => {
    const input = React.findDOMNode(this.refs.image);
    const imageForm = React.findDOMNode(this.refs.imageForm);
    const file = input.files[0];
    let reader = new FileReader();

    reader.onload = (event) => {
      this.setState({profilePhoto: event.target.result});
      imageForm.reset();
    };
    reader.readAsDataURL(file);
  };

  render() {
    const { isOpen, profilePhoto } = this.state;

    const modalBody = profilePhoto ? (
        <img src={profilePhoto}/>
    ) : [
      <h3>Chose image to set profile picture</h3>
    ,
      <form ref="imageForm">
        <input type="file" ref="image" onChange={this.onProfilePhotoChange}/>
      </form>
    ];

    console.debug(modalBody);

    if (isOpen) {
      return (
        <Modal className="modal-new modal-new--profile-picture"
               closeTimeoutMS={150}
               isOpen={isOpen}>

          <div className="modal-new__header">
            <i className="modal-new__header__icon material-icons">portrait</i>

            <h3 className="modal-new__header__title">Profile picture</h3>

            <div className="pull-right">
              <button className="button button--lightblue" onClick={this.onClose}>Done</button>
            </div>
          </div>

          <div className="modal-new__body">
            {modalBody}
          </div>
        </Modal>
      );
    } else {
      return null;
    }
  }
}

export default Container.create(ProfilePictureModal, {pure: false});
