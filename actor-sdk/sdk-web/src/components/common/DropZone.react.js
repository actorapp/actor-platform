/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { union, without } from 'lodash';
import React, { Component, PropTypes } from 'react';
import classnames from 'classnames';

let targetCollection = [];

export default class DropZone extends Component {
  static propTypes = {
    children: PropTypes.node,

    onDropComplete: PropTypes.func.isRequired,

    // Callbacks
    onDragEnterCallback: PropTypes.func,
    onDragLeaveCallback: PropTypes.func,
    onDropCallback: PropTypes.func
  };

  constructor(props) {
    super(props);

    this.state = {
      isActive: false,
      isHovered: false
    };

    window.addEventListener('dragenter', this.onWindowDragEnter, false);
    window.addEventListener('dragover', this.onWindowDragOver, false);
    window.addEventListener('dragleave', this.onWindowDragLeave, false);
    window.addEventListener('drop', this.onWindowDragLeave, false);
  }

  componentWillUnmount() {
    window.removeEventListener('dragenter', this.onWindowDragEnter, false);
    window.removeEventListener('dragover', this.onWindowDragOver, false);
    window.removeEventListener('dragleave', this.onWindowDragLeave, false);
    window.removeEventListener('drop', this.onWindowDragLeave, false);
  }

  onWindowDragEnter = (event) => {
    const { onDragEnterCallback } = this.props;
    event.preventDefault();

    if (targetCollection.length === 0) {
      this.setState({isActive: true});
      onDragEnterCallback && onDragEnterCallback();
    }

    targetCollection = union(targetCollection, [event.target]);
  };
  onWindowDragOver = (event) => event.preventDefault();
  onWindowDragLeave = (event) => {
    const { onDragLeaveCallback } = this.props;
    event.preventDefault();

    targetCollection = without(targetCollection, event.target);

    if (targetCollection.length === 0) {
      this.setState({isActive: false});
      onDragLeaveCallback && onDragLeaveCallback();
    }
  };

  onDragEnter = () => this.setState({isHovered: true});
  onDragLeave = () => this.setState({isHovered: false});
  onDrop = (event) => {
    const { onDropCallback, onDropComplete } = this.props;
    this.onDragLeave();
    this.onWindowDragLeave(event);
    onDropCallback && onDropCallback();
    onDropComplete(event.dataTransfer.files);
  };

  render() {
    const { isActive, isHovered } = this.state;

    const dropzoneClassName = classnames('dropzone', {
      'dropzone--hover': isHovered
    });

    if (isActive) {
      return (
        <div className={dropzoneClassName}
             onDragEnter={this.onDragEnter}
             onDragLeave={this.onDragLeave}
             onDrop={this.onDrop}>
          {this.props.children || 'Drop here'}
        </div>
      );
    } else {
      return null;
    }
  }
}
