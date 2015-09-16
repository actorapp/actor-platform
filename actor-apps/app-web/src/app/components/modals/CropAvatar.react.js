/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import { Container } from 'flux/utils';
import classnames from 'classnames';

import Modal from 'react-modal';

import { KeyCodes } from 'constants/ActorAppConstants';

import CropAvatarActionCreators from 'actions/CropAvatarActionCreators';

import CropAvatarStore from 'stores/CropAvatarStore'

class CropAvatarModal extends Component {
  static propTypes = {
    onCropFinish: React.PropTypes.func.isRequired
  };

  static getStores = () => [CropAvatarStore];

  static calculateState() {
    return {
      isOpen: CropAvatarStore.isOpen(),
      pictureSource: CropAvatarStore.getPictureSource(),
      cropPosition: {
        x: 0,
        y: 0
      },
      cropSize: {
        height: 200,
        width: 200
      }
    };
  }

  componentDidMount() {
    document.addEventListener('keydown', this.onKeyDown, false);
  }
  componentWillUnmount() {
    document.removeEventListener('keydown', this.onKeyDown, false);
  }

  onClose = () => CropAvatarActionCreators.hide();

  onKeyDown = (event) => {
    if (event.keyCode === KeyCodes.ESC) {
      event.preventDefault();
      this.onClose();
    }
  };

  onStartMoving = (event) => {
    const { cropPosition } = this.state;

    event.preventDefault();

    const overlay = React.findDOMNode(this.refs.overlay);
    const overlayRect = overlay.getBoundingClientRect();

    const dragOffset = {
      x: (event.pageX - overlayRect.left) - cropPosition.x,
      y: (event.pageY - overlayRect.top) - cropPosition.y
    };
    this.setState({dragOffset});

    overlay.addEventListener('mousemove', this.onMoving);
    overlay.addEventListener('touchmove', this.onMoving);
  };
  onMoving = (event) => {
    const { dragOffset, cropSize } = this.state;
    const overlay = React.findDOMNode(this.refs.overlay);
    const overlayRect = overlay.getBoundingClientRect();

    event.preventDefault();
    event.stopPropagation();

    let cropPosition = {
      x: (event.pageX - overlayRect.left) - dragOffset.x,
      y: (event.pageY - overlayRect.top) - dragOffset.y
    };

    if (cropPosition.x < 0) {
      cropPosition.x = 0;
    } else if (cropPosition.x > overlayRect.width - cropSize.width) {
      cropPosition.x = overlayRect.width - cropSize.width;
    }

    if (cropPosition.y < 0) {
      cropPosition.y = 0;
    } else if (cropPosition.y > overlayRect.height - cropSize.height) {
      cropPosition.y = overlayRect.height - cropSize.height;
    }

    this.setState({cropPosition})
  };
  onEndMoving = (event) => {
    const overlay = React.findDOMNode(this.refs.overlay);

    event.preventDefault();

    overlay.removeEventListener('mousemove', this.onMoving);
    overlay.removeEventListener('touchmove', this.onMoving);
  };

  onCrop = () => {
    const { cropPosition, cropSize } = this.state;
    const { onCropFinish } = this.props;

    const cropImage = React.findDOMNode(this.refs.cropImage);
    let canvas = document.createElement('canvas');

    canvas.width = cropSize.width;
    canvas.height = cropSize.height;

    let context = canvas.getContext('2d');
    context.drawImage(cropImage, cropPosition.x, cropPosition.y, cropSize.width, cropSize.height, 0, 0, cropSize.width, cropSize.height);

    const croppedImage = canvas.toDataURL();

    onCropFinish(croppedImage);
    this.onClose();
  };

  render() {
    const { isOpen, pictureSource, cropPosition } = this.state;

    if (isOpen) {
      return (
        <Modal className="modal-new modal-new--profile-picture"
               closeTimeoutMS={150}
               isOpen={isOpen}>

          <div className="modal-new__header">
            <i className="modal-new__header__icon material-icons">crop</i>
            <h3 className="modal-new__header__title">Crop picture</h3>
            <div className="pull-right">
              <button className="button button--lightblue" onClick={this.onCrop}>Done</button>
            </div>
          </div>

          <div className="modal-new__body">
            <div className="crop-wrapper" ref="overlay">
              <div className="crop-wrapper__overlay"
                   onMouseDown={this.onStartMoving}
                   onMouseUp={this.onEndMoving}
                   onTouchEnd={this.onEndMoving}
                   onTouchStart={this.onStartMoving}
                   style={{left: cropPosition.x, top: cropPosition.y}}>
                <img className="crop-wrapper__image-crop"
                     draggable="false"
                     ref="cropImage"
                     src={pictureSource}
                     style={{left: -cropPosition.x, top: -cropPosition.y}}/>
              </div>
              <img className="crop-wrapper__image-original"
                   draggable="false"
                   ref="originalImage"
                   src={pictureSource}/>
            </div>
          </div>
        </Modal>
      );
    } else {
      return null;
    }
  }
}

export default Container.create(CropAvatarModal);
