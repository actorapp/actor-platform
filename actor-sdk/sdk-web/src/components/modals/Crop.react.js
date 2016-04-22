/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import { findDOMNode } from 'react-dom';
import { Container } from 'flux/utils';
import { dataURItoBlob } from '../../utils/ImageUtils';
import { FormattedMessage } from 'react-intl';
import Modal from 'react-modal';

import CropActionCreators from '../../actions/CropActionCreators';

import CropStore from '../../stores/CropAvatarStore';

import ModalCloseButton from './ModalCloseButton.react';

const MIN_CROP_SIZE = 100;

class CropAvatarModal extends Component {
  static getStores() {
    return [CropStore];
  }

  static calculateState() {
    return {
      pictureSource: CropStore.getState().source,
      callback: CropStore.getState().callback,
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
    const originalImage = findDOMNode(this.refs.originalImage);
    window.addEventListener('resize', this.storeScaledSizes, false);
    originalImage.addEventListener('load', this.storeScaledSizes, false);
  }

  componentWillUnmount() {
    window.removeEventListener('resize', this.storeScaledSizes, false);
  }

  handleClose = () => CropActionCreators.hide();

  onStartMoving = (event) => {
    const { cropPosition } = this.state;

    event.preventDefault();

    const wrapper = findDOMNode(this.refs.wrapper);
    const wrapperRect = wrapper.getBoundingClientRect();

    const dragOffset = {
      x: (event.pageX - wrapperRect.left) - cropPosition.x,
      y: (event.pageY - wrapperRect.top) - cropPosition.y
    };
    this.setState({ dragOffset });

    wrapper.addEventListener('mousemove', this.onMoving);
    wrapper.addEventListener('touchmove', this.onMoving);
  };
  onMoving = (event) => {
    const { dragOffset, cropSize } = this.state;
    const wrapper = findDOMNode(this.refs.wrapper);
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

    this.setState({ cropPosition })
  };

  onStartResizeTop = (event) => {
    const wrapper = findDOMNode(this.refs.wrapper);
    const resizeLastCoord = event.pageY;
    event.preventDefault();
    this.setState({ resizeLastCoord });
    wrapper.addEventListener('mousemove', this.onResizeTop);
    wrapper.addEventListener('touchmove', this.onResizeTop);
  };

  onStartResizeRight = (event) => {
    const wrapper = findDOMNode(this.refs.wrapper);
    const resizeLastCoord = event.pageX;
    event.preventDefault();
    this.setState({ resizeLastCoord });
    wrapper.addEventListener('mousemove', this.onResizeRight);
    wrapper.addEventListener('touchmove', this.onResizeRight);
  };

  onStartResizeBottom = (event) => {
    const wrapper = findDOMNode(this.refs.wrapper);
    const resizeLastCoord = event.pageY;
    event.preventDefault();
    this.setState({ resizeLastCoord });
    wrapper.addEventListener('mousemove', this.onResizeBottom);
    wrapper.addEventListener('touchmove', this.onResizeBottom);
  };

  onStartResizeLeft = (event) => {
    const wrapper = findDOMNode(this.refs.wrapper);
    const resizeLastCoord = event.pageX;
    event.preventDefault();
    this.setState({ resizeLastCoord });
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

    if (resizedCropSize < MIN_CROP_SIZE || resizedCropSize > scaledWidth || resizedCropSize > scaledHeight) {
      resizedCropSize = cropSize;
      resizeCropPosition = cropPosition;
    }

    this.setState({ resizeLastCoord: axisCoord });
    this.updateCropSize(resizedCropSize, resizeCropPosition);
  };

  removeListeners = () => {
    const wrapper = findDOMNode(this.refs.wrapper);

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

  updateCropSize = (cropSize, cropPosition) => this.setState({ cropSize, cropPosition });

  onCrop = () => {
    const { cropPosition, cropSize, scaleRatio, callback } = this.state;
    const cropImage = findDOMNode(this.refs.cropImage);
    let canvas = document.createElement('canvas');
    let context = canvas.getContext('2d');

    canvas.width = canvas.height = cropSize;

    context.drawImage(cropImage, cropPosition.x / scaleRatio, cropPosition.y / scaleRatio, cropSize / scaleRatio, cropSize / scaleRatio, 0, 0, cropSize, cropSize);

    const croppedImage = dataURItoBlob(canvas.toDataURL());

    callback && callback(croppedImage);
    this.handleClose();
  };

  storeScaledSizes = () => {
    const { cropSize } = this.state;
    const originalImage = findDOMNode(this.refs.originalImage);
    const scaledWidth = originalImage.width;
    const scaledHeight = originalImage.height;
    const naturalWidth = originalImage.naturalWidth;
    const naturalHeight = originalImage.naturalHeight;
    const scaleRatio = scaledWidth/naturalWidth;
    const cropPosition = {
      x: ((naturalWidth / 2) - (cropSize / 2)) * scaleRatio,
      y: ((naturalHeight / 2) - (cropSize / 2)) * scaleRatio
    };

    this.setState({ cropPosition, scaledWidth, scaledHeight, naturalWidth, naturalHeight, scaleRatio });
  };

  renderControls() {
    return (
      <div className="controls">
        <button className="button" onClick={this.handleClose}>
          <FormattedMessage id="button.cancel"/>
        </button>
        <button className="button button--rised" onClick={this.onCrop}>
          <FormattedMessage id="button.done"/>
        </button>
      </div>
    );
  }

  render() {
    const { pictureSource, cropPosition, cropSize, scaledWidth, scaledHeight, maxImageHeight } = this.state;

    return (
      <Modal
        overlayClassName="modal-overlay modal-overlay--white"
        className="modal modal--fullscreen"
        onRequestClose={this.handleClose}
        shouldCloseOnOverlayClick={false}
        isOpen>

        <ModalCloseButton onClick={this.handleClose}/>

        <div className="crop">
          <div className="modal__content">

            <header className="modal__header">
                <FormattedMessage id="modal.crop.title" tagName="h1"/>
            </header>

            <div className="modal__body">
              <div className="crop-wrapper"
                   ref="wrapper"
                   onTouchEnd={this.removeListeners}
                   onMouseUp={this.removeListeners}>
                <div className="crop-wrapper__scale"
                     style={{ width: cropSize, height: cropSize, left: cropPosition.x, top: cropPosition.y }}>
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
                     style={{ width: cropSize, height: cropSize, left: cropPosition.x, top: cropPosition.y }}>
                  <img className="crop-wrapper__image-crop"
                       draggable="false"
                       ref="cropImage"
                       src={pictureSource}
                       style={{ left: -cropPosition.x, top: -cropPosition.y, width: scaledWidth, height: scaledHeight }}/>
                </div>

                <img
                  className="crop-wrapper__image-original"
                  draggable="false"
                  ref="originalImage"
                  src={pictureSource}
                  style={{ maxHeight: maxImageHeight }}/>
              </div>
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

export default Container.create(CropAvatarModal);
