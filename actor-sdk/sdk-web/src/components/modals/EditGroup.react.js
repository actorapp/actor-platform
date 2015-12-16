/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import { Container } from 'flux/utils';
import Modal from 'react-modal';
import ReactMixin from 'react-mixin';
import { IntlMixin } from 'react-intl';
//import { Styles, TextField } from 'material-ui';
//import ActorTheme from '../../constants/ActorTheme';

import { KeyCodes } from '../../constants/ActorAppConstants';

import EditGroupStore from '../../stores/EditGroupStore';
import CropAvatarStore from '../../stores/CropAvatarStore';

import EditGroupActionCreators from '../../actions/EditGroupActionCreators';
import CropAvatarActionCreators from '../../actions/CropAvatarActionCreators';

import TextField from '../common/TextField.react';
import AvatarItem from '../common/AvatarItem.react';
import CropAvatarModal from './CropAvatar.react';

//const ThemeManager = new Styles.ThemeManager();

class EditGroup extends Component {
  constructor(props) {
    super(props);
  }

  //static childContextTypes = {
  //  muiTheme: React.PropTypes.object
  //};
  //
  //getChildContext() {
  //  return {
  //    muiTheme: ThemeManager.getCurrentTheme()
  //  };
  //}

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

  //componentWillMount() {
  //  ThemeManager.setTheme(ActorTheme);
  //  ThemeManager.setComponentThemes({
  //    textField: {
  //      textColor: 'rgba(0,0,0,.87)',
  //      focusColor: '#68a3e7',
  //      backgroundColor: 'transparent',
  //      borderColor: '#68a3e7',
  //      disabledTextColor: 'rgba(0,0,0,.4)'
  //    }
  //  });
  //}

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

    const cropAvatar = isCropModalOpen ? <CropAvatarModal onCropFinish={this.changeGroupAvatar}/> : null;

    if (isOpen) {
      return (
        <Modal className="modal-new modal-new--edit-group"
               closeTimeoutMS={150}
               isOpen={isOpen}
               style={{width: 440}}>

          <header className="modal-new__header">
            <a className="modal-new__header__icon material-icons">edit</a>
            <h3 className="modal-new__header__title">{this.getIntlMessage('modal.group.title')}</h3>
            <div className="pull-right">
              <button className="button button--lightblue" onClick={this.onSave}>{this.getIntlMessage('button.done')}</button>
            </div>
          </header>

          <div className="modal-new__body row">
            <div className="col-xs">
              <TextField className="input__material--wide"
                         floatingLabel={this.getIntlMessage('modal.group.name')}
                         onChange={this.onTitleChange}
                         ref="name"
                         value={title}/>

              {
                isAdmin
                  ? <div className="about">
                      <label htmlFor="about">{this.getIntlMessage('modal.group.about')}</label>
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
                  <span>{this.getIntlMessage('modal.group.avatarChange')}</span>
                </a>
              </div>
              <div className="profile-picture__controls">
                <a onClick={this.onProfilePictureRemove}>{this.getIntlMessage('modal.group.avatarRemove')}</a>
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

ReactMixin.onClass(EditGroup, IntlMixin);

export default Container.create(EditGroup, {pure: false});
