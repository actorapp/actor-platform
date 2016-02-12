/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { findDOMNode } from 'react-dom';
import { Container } from 'flux/utils';
import Modal from 'react-modal';

import { KeyCodes } from '../../constants/ActorAppConstants';

import EditGroupStore from '../../stores/EditGroupStore';
import CropAvatarStore from '../../stores/CropAvatarStore';

import EditGroupActionCreators from '../../actions/EditGroupActionCreators';
import CropAvatarActionCreators from '../../actions/CropAvatarActionCreators';

import TextField from '../common/TextField.react';
import AvatarItem from '../common/AvatarItem.react';
import CropAvatarModal from './CropAvatar.react';

class EditGroup extends Component {
  constructor(props) {
    super(props);
  }

  static contextTypes = {
    intl: PropTypes.object
  };

  static getStores = () => [EditGroupStore, CropAvatarStore];

  static calculateState() {
    return {
      isOpen: EditGroupStore.isOpen(),
      group: EditGroupStore.getGroup(),
      isAdmin: EditGroupStore.isAdmin(),
      title: EditGroupStore.getTitle(),
      about: EditGroupStore.getAbout(),
      isCropModalOpen: CropAvatarStore.isOpen()
    }
  }

  componentWillUnmount() {
    this.removeListeners();
  }

  componentWillUpdate(nextProps, nextState) {
    if (nextState.isOpen) {
      nextState.isCropModalOpen ? this.removeListeners() : this.setListeners();
    } else {
      nextState.isCropModalOpen ? this.setListeners() : this.removeListeners();
    }
  }

  setListeners = () => document.addEventListener('keydown', this.onKeyDown, false);
  removeListeners = () => document.removeEventListener('keydown', this.onKeyDown, false);

  onClose = () => EditGroupActionCreators.hide();
  onTitleChange = event => this.setState({title: event.target.value});
  onAboutChange = event => this.setState({about: event.target.value});

  onKeyDown = event => {
    if (event.keyCode === KeyCodes.ESC) {
      event.preventDefault();
      this.onClose();
    }
  };

  onSave = () => {
    const { group, title, about, isAdmin } = this.state;
    EditGroupActionCreators.editGroupTitle(group.id, title);
    if (isAdmin) {
      EditGroupActionCreators.editGroupAbout(group.id, about);
    }
    this.onClose();
  };

  onProfilePictureInputChange = () => {
    const imageInput = findDOMNode(this.refs.imageInput);
    const imageForm = findDOMNode(this.refs.imageForm);
    const file = imageInput.files[0];

    let reader = new FileReader();
    reader.onload = (event) => {
      CropAvatarActionCreators.show(event.target.result);
      imageForm.reset();
    };
    reader.readAsDataURL(file);
  };

  onChangeAvatarClick = () => {
    const imageInput = findDOMNode(this.refs.imageInput);
    imageInput.click()
  };

  changeGroupAvatar = (croppedImage) => {
    const { group } = this.state;
    EditGroupActionCreators.changeGroupAvatar(group.id, croppedImage);
  };

  onProfilePictureRemove = () =>  {
    const { group } = this.state;
    EditGroupActionCreators.removeGroupAvatar(group.id);
  };

  render() {
    const { isOpen, group, isCropModalOpen, title, about, isAdmin } = this.state;
    const { intl } = this.context;

    const cropAvatar = isCropModalOpen ? <CropAvatarModal onCropFinish={this.changeGroupAvatar}/> : null;
    const modalStyle = {
      content : {
        position: null,
        top: null,
        left: null,
        right: null,
        bottom: null,
        border: null,
        background: null,
        overflow: null,
        outline: null,
        padding: null,
        borderRadius: null,
        width: 440
      }
    };

    if (isOpen) {
      return (
        <Modal className="modal-new modal-new--edit-group"
               closeTimeoutMS={150}
               isOpen={isOpen}
               style={modalStyle}>

          <header className="modal-new__header">
            <a className="modal-new__header__icon material-icons">edit</a>
            <h3 className="modal-new__header__title">{intl.messages['modal.group.title']}</h3>
            <div className="pull-right">
              <button className="button button--lightblue" onClick={this.onSave}>{intl.messages['button.done']}</button>
            </div>
          </header>

          <div className="modal-new__body row">
            <div className="col-xs">
              <TextField className="input__material--wide"
                         floatingLabel={intl.messages['modal.group.name']}
                         onChange={this.onTitleChange}
                         ref="name"
                         value={title}/>

              {
                isAdmin
                  ? <div className="about">
                      <label htmlFor="about">{intl.messages['modal.group.about']}</label>
                      <textarea className="textarea" value={about} onChange={this.onAboutChange} id="about"/>
                    </div>
                  : null
              }
            </div>
            <div className="profile-picture text-center">
              <div className="profile-picture__changer">
                <AvatarItem image={group.bigAvatar}
                            placeholder={group.placeholder}
                            size="big"
                            title={group.name}/>
                <a onClick={this.onChangeAvatarClick}>
                  <span>{intl.messages['modal.group.avatarChange']}</span>
                </a>
              </div>
              <div className="profile-picture__controls">
                <a onClick={this.onProfilePictureRemove}>{intl.messages['modal.group.avatarRemove']}</a>
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

export default Container.create(EditGroup, {pure: false});
