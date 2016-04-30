/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { findDOMNode } from 'react-dom';
import { FormattedMessage } from 'react-intl';
import { ModalTypes } from '../../../constants/ActorAppConstants';
import classnames from 'classnames';

import CropActionCreators from '../../../actions/CropActionCreators';

import AvatarItem from '../../common/AvatarItem.react';

class PictureChanger extends Component {
  static propTypes = {
    bigAvatar: PropTypes.string,
    placeholder: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,

    small: PropTypes.bool,

    fromModal: PropTypes.oneOf([
      ModalTypes.PROFILE,
      ModalTypes.CREATE_GROUP,
      ModalTypes.EDIT_GROUP
    ]).isRequired,

    onRemove: PropTypes.func.isRequired,
    onChange: PropTypes.func.isRequired
  }

  constructor(props) {
    super(props);

    this.handleChangeAvatarClick = this.handleChangeAvatarClick.bind(this);
    this.handlePictureInputChange = this.handlePictureInputChange.bind(this);
  }

  handleChangeAvatarClick(event) {
    const imageInput = findDOMNode(this.refs.imageInput);
    imageInput.click(event);
  }

  handlePictureInputChange() {
    const { fromModal, onChange } = this.props;
    const reader = new FileReader();
    const imageForm = findDOMNode(this.refs.imageForm);
    const file = findDOMNode(this.refs.imageInput).files[0];

    reader.onload = (event) => {
      CropActionCreators.show(event.target.result, fromModal, onChange);
      imageForm.reset();
    }
    reader.readAsDataURL(file);
  }

  renderPictureChanger() {
    const { bigAvatar, placeholder, name, small } = this.props;

    return (
      <div className="picture-changer__changer">
        <AvatarItem
          className="picture-changer__avatar"
          image={bigAvatar}
          placeholder={placeholder}
          size={small ? 'big' : 'huge'}
          title={name}
        />

        <a onClick={this.handleChangeAvatarClick}>
          <FormattedMessage id="modal.profile.avatarChange"/>
        </a>

      </div>
    );
  }

  renderPictureRemover() {
    const { bigAvatar } = this.props;
    if (!bigAvatar) return null;

    return (
      <div className="picture-changer__controls">
        <a onClick={this.props.onRemove}>
          <FormattedMessage id="modal.profile.avatarRemove"/>
        </a>
      </div>
    );
  }

  render() {
    const pictureChangerClassName = classnames('picture-changer', {
      'picture-changer--small': this.props.small
    })

    return (
      <div className={pictureChangerClassName}>
        {this.renderPictureChanger()}
        {this.renderPictureRemover()}

        <form className="hide" ref="imageForm">
          <input onChange={this.handlePictureInputChange} ref="imageInput" type="file"/>
        </form>
      </div>
    );
  }

}

export default PictureChanger;
