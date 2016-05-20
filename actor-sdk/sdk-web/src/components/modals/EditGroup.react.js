/*
 * Copyright (C) 2016 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import { Container } from 'flux/utils';
import { FormattedMessage } from 'react-intl';
import Modal from 'react-modal';
import { ModalTypes } from '../../constants/ActorAppConstants';

import EditGroupStore from '../../stores/EditGroupStore';

import EditGroupActionCreators from '../../actions/EditGroupActionCreators';

import TextField from '../common/TextField.react';
import PictureChanger from './profile/PictureChanger.react';

class EditGroup extends Component {
  static getStores() {
    return [EditGroupStore];
  }

  static calculateState() {
    return {
      group: EditGroupStore.getGroup(),
      isAdmin: EditGroupStore.isAdmin(),
      title: EditGroupStore.getTitle(),
      about: EditGroupStore.getAbout()
    }
  }

  constructor(props) {
    super(props);

    this.handleClose = this.handleClose.bind(this);
    this.handleTitleChange = this.handleTitleChange.bind(this);
    this.handleAboutChange = this.handleAboutChange.bind(this);
    this.handleSave = this.handleSave.bind(this);
    this.handleChangeGroupAvatar = this.handleChangeGroupAvatar.bind(this);
    this.handleRemoveGroupPicture = this.handleRemoveGroupPicture.bind(this);
  }


  handleClose() {
    EditGroupActionCreators.hide();
  }

  handleTitleChange(event) {
    this.setState({ title: event.target.value });
  }

  handleAboutChange(event) {
    this.setState({ about: event.target.value });
  }

  handleSave() {
    const { group, title, about, isAdmin } = this.state;
    EditGroupActionCreators.editGroupTitle(group.id, title);
    if (isAdmin) {
      EditGroupActionCreators.editGroupAbout(group.id, about);
    }
    this.handleClose();
  }

  handleChangeGroupAvatar(croppedImage) {
    const { group } = this.state;
    EditGroupActionCreators.changeGroupAvatar(group.id, croppedImage);
  }

  handleRemoveGroupPicture() {
    const { group } = this.state;
    EditGroupActionCreators.removeGroupAvatar(group.id);
  }

  renderTitle() {
    const { title } = this.state;

    return (
      <TextField
        className="input__material--wide"
        floatingLabel={<FormattedMessage id="modal.group.name"/>}
        onChange={this.handleTitleChange}
        ref="name"
        value={title}/>
    );
  }

  renderAbout() {
    const { isAdmin, about } = this.state;
    if (!isAdmin) return null;

    return (
      <div className="about">
        <label htmlFor="about"><FormattedMessage id="modal.group.about"/></label>
        <textarea className="textarea" value={about} onChange={this.handleAboutChange} id="about"/>
      </div>
    );
  }

  render() {
    const { group } = this.state;

    return (
      <Modal
        overlayClassName="modal-overlay"
        className="modal"
        onRequestClose={this.handleClose}
        isOpen>

        <div className="edit-group">
          <div className="modal__content">

            <header className="modal__header">
              <i className="modal__header__icon material-icons">edit</i>
              <FormattedMessage id="modal.group.title" tagName="h1"/>
              <button className="button button--lightblue" onClick={this.handleSave}>
                <FormattedMessage id="button.done"/>
              </button>
            </header>

            <div className="modal__body row">
              <div className="col-xs">
                {this.renderTitle()}
                {this.renderAbout()}
              </div>

              <PictureChanger {...group}
                small
                fromModal={ModalTypes.EDIT_GROUP}
                onRemove={this.handleRemoveGroupPicture}
                onChange={this.handleChangeGroupAvatar}/>
            </div>

          </div>
        </div>

      </Modal>
    );
  }
}

export default Container.create(EditGroup, { pure: false });
