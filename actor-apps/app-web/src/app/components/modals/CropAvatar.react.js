/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import { Container } from 'flux/utils';
import classnames from 'classnames';
import { dataURItoBlob } from 'utils/ImageUtils';

import Modal from 'react-modal';

import { KeyCodes } from 'constants/ActorAppConstants';

import CropAvatarActionCreators from 'actions/CropAvatarActionCreators';

import CropAvatarStore from 'stores/CropAvatarStore'

const minCropSize = 100;

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
      cropSize: 200,
      scaledWidth: 0,
      scaledHeight: 0,
      naturalWidth: 0,
      naturalHeight: 0,
      maxImageHeight: (document.body.clientHeight * .9) - 64 // 64 is modal header height.
    };
  }

  componentDidMount() {
    const originalImage = React.findDOMNode(this.refs.originalImage);
    document.addEventListener('keydown', this.onKeyDown, false);
    window.addEventListener('resize', this.storeScaledSizes, false);
    originalImage.addEventListener('load', this.storeScaledSizes, false);
  }

  componentWillUnmount() {
    const originalImage = React.findDOMNode(this.refs.originalImage);
    document.removeEventListener('keydown', this.onKeyDown, false);
    window.removeEventListener('resize', this.storeScaledSizes, false);
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

    const wrapper = React.findDOMNode(this.refs.wrapper);
    const wrapperRect = wrapper.getBoundingClientRect();

    const dragOffset = {
      x: (event.pageX - wrapperRect.left) - cropPosition.x,
      y: (event.pageY - wrapperRect.top) - cropPosition.y
    };
    this.setState({dragOffset});

    wrapper.addEventListener('mousemove', this.onMoving);
    wrapper.addEventListener('touchmove', this.onMoving);
  };
  onMoving = (event) => {
    const { dragOffset, cropSize } = this.state;
    const wrapper = React.findDOMNode(this.refs.wrapper);
    const wrapperRect = wrapper.getBoundingClientRect();

    let cropPosition = {
      x: (event.pageX - wrapperRect.left) - dragOffset.x,
      y: (event.pageY - wrapperRect.top) - dragOffset.y
    };

    if (cropPosition.x < 0) {
      cropPosition.x = 0;
    } else if (cropPosition.x > wrapperRect.width - cropSize) {
      cropPosition.x = wrapperRect.width - cropSize;
    }

    if (cropPosition.y < 0) {
      cropPosition.y = 0;
    } else if (cropPosition.y > wrapperRect.height - cropSize) {
      cropPosition.y = wrapperRect.height - cropSize;
    }

    this.setState({cropPosition})
  };

  onStartResizeTop = (event) => {
    const wrapper = React.findDOMNode(this.refs.wrapper);
    const resizeLastCoord = event.pageY;
    event.preventDefault();
    this.setState({resizeLastCoord});
    wrapper.addEventListener('mousemove', this.onResizeTop);
    wrapper.addEventListener('touchmove', this.onResizeTop);
  };

  onStartResizeRight = (event) => {
    const wrapper = React.findDOMNode(this.refs.wrapper);
    const resizeLastCoord = event.pageX;
    event.preventDefault();
    this.setState({resizeLastCoord});
    wrapper.addEventListener('mousemove', this.onResizeRight);
    wrapper.addEventListener('touchmove', this.onResizeRight);
  };

  onStartResizeBottom = (event) => {
    const wrapper = React.findDOMNode(this.refs.wrapper);
    const resizeLastCoord = event.pageY;
    event.preventDefault();
    this.setState({resizeLastCoord});
    wrapper.addEventListener('mousemove', this.onResizeBottom);
    wrapper.addEventListener('touchmove', this.onResizeBottom);
  };

  onStartResizeLeft = (event) => {
    const wrapper = React.findDOMNode(this.refs.wrapper);
    const resizeLastCoord = event.pageX;
    event.preventDefault();
    this.setState({resizeLastCoord});
    wrapper.addEventListener('mousemove', this.onResizeLeft);
    wrapper.addEventListener('touchmove', this.onResizeLeft);
  };

  onResizeTop = (event) => this.onCropResize(event, 'TOP');
  onResizeRight = (event) => this.onCropResize(event, 'RIGHT');
  onResizeBottom = (event) => this.onCropResize(event, 'BOTTOM');
  onResizeLeft = (event) => this.onCropResize(event, 'LEFT');

  onCropResize = (event, direction) => {
    const { cropPosition, resizeLastCoord, cropSize, scaledWidth, scaledHeight } = this.state;
    const axisCoord = (direction === 'RIGHT' || direction === 'LEFT') ? event.pageX : event.pageY;
    const resizeValue = resizeLastCoord - axisCoord;

    let resizeCropPosition, resizedCropSize;
    switch (direction) {
      case 'TOP':
        resizedCropSize = cropSize + resizeValue;
        resizeCropPosition = {
          x: cropPosition.x - (resizeValue / 2),
          y: cropPosition.y - resizeValue
        };
        break;
      case 'RIGHT':
        resizedCropSize = cropSize - resizeValue;
        resizeCropPosition = {
          x: cropPosition.x,
          y: cropPosition.y + (resizeValue / 2)
        };
        break;
      case 'BOTTOM':
        resizedCropSize = cropSize - resizeValue;
        resizeCropPosition = {
          x: cropPosition.x + (resizeValue / 2),
          y: cropPosition.y
        };
        break;
      case 'LEFT':
        resizedCropSize = cropSize + resizeValue;
        resizeCropPosition = {
          x: cropPosition.x - resizeValue,
          y: cropPosition.y - (resizeValue / 2)
        };
        break;
      default:
    }

    if (resizedCropSize < minCropSize || resizedCropSize > scaledWidth || resizedCropSize > scaledHeight) {
      resizedCropSize = cropSize;
      resizeCropPosition = cropPosition;
    }

    this.setState({resizeLastCoord: axisCoord});
    this.updateCropSize(resizedCropSize, resizeCropPosition);
  };

  removeListeners = () => {
    const wrapper = React.findDOMNode(this.refs.wrapper);

    wrapper.removeEventListener('mousemove', this.onMoving);
    wrapper.removeEventListener('touchmove', this.onMoving);
    wrapper.removeEventListener('mousemove', this.onResizeTop);
    wrapper.removeEventListener('touchmove', this.onResizeTop);
    wrapper.removeEventListener('mousemove', this.onResizeRight);
    wrapper.removeEventListener('touchmove', this.onResizeRight);
    wrapper.removeEventListener('mousemove', this.onResizeBottom);
    wrapper.removeEventListener('touchmove', this.onResizeBottom);
    wrapper.removeEventListener('mousemove', this.onResizeLeft);
    wrapper.removeEventListener('touchmove', this.onResizeLeft);
  };

  updateCropSize = (cropSize, cropPosition) => this.setState({cropSize, cropPosition});

  onCrop = () => {
    const { cropPosition, cropSize, scaleRatio } = this.state;
    const { onCropFinish } = this.props;
    const cropImage = React.findDOMNode(this.refs.cropImage);
    let canvas = document.createElement('canvas');
    let context = canvas.getContext('2d');

    canvas.width = canvas.height = cropSize;

    context.drawImage(cropImage, cropPosition.x / scaleRatio, cropPosition.y / scaleRatio, cropSize / scaleRatio, cropSize / scaleRatio, 0, 0, cropSize, cropSize);

    const croppedImage = dataURItoBlob(canvas.toDataURL());

    onCropFinish(croppedImage);
    this.onClose();
  };

  storeScaledSizes = (event) => {
    const originalImage = React.findDOMNode(this.refs.originalImage);
    this.setState({
      scaledWidth: originalImage.width,
      scaledHeight: originalImage.height,
      naturalWidth: originalImage.naturalWidth,
      naturalHeight: originalImage.naturalHeight,
      scaleRatio: originalImage.width/originalImage.naturalWidth
    })
  };

  render() {
    const { isOpen, pictureSource, cropPosition, cropSize, scaledWidth, scaledHeight, maxImageHeight } = this.state;

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
            <div className="crop-wrapper"
                 ref="wrapper"
                 onTouchEnd={this.removeListeners}
                 onMouseUp={this.removeListeners}>
              <div className="crop-wrapper__scale"
                   style={{width: cropSize, height: cropSize, left: cropPosition.x, top: cropPosition.y}}>
                <div className="crop-wrapper__scale__handler crop-wrapper__scale__handler--top"
                     onMouseDown={this.onStartResizeTop}
                     onTouchStart={this.onStartResizeTop}/>
                <div className="crop-wrapper__scale__handler crop-wrapper__scale__handler--right"
                     onMouseDown={this.onStartResizeRight}
                     onTouchStart={this.onStartResizeRight}/>
                <div className="crop-wrapper__scale__handler crop-wrapper__scale__handler--bottom"
                     onMouseDown={this.onStartResizeBottom}
                     onTouchStart={this.onStartResizeBottom}/>
                <div className="crop-wrapper__scale__handler crop-wrapper__scale__handler--left"
                     onMouseDown={this.onStartResizeLeft}
                     onTouchStart={this.onStartResizeLeft}/>
              </div>
              <div className="crop-wrapper__overlay"
                   onMouseDown={this.onStartMoving}
                   onTouchStart={this.onStartMoving}
                   style={{ width: cropSize, height: cropSize, left: cropPosition.x, top: cropPosition.y}}>
                <img className="crop-wrapper__image-crop"
                     draggable="false"
                     ref="cropImage"
                     src={pictureSource}
                     style={{left: -cropPosition.x, top: -cropPosition.y, width: scaledWidth, height: scaledHeight}}/>
              </div>
              <img className="crop-wrapper__image-original"
                   draggable="false"
                   ref="originalImage"
                   src={pictureSource}
                   style={{maxHeight: maxImageHeight}}/>
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
