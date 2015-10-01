/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { union, without } from 'lodash';
import React, { Component } from 'react';
import classnames from 'classnames';

let targetCollection = [];

export default class DropZone extends Component {
  static propTypes = {
    onDropComplete: React.PropTypes.func.isRequired
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
    event.preventDefault();

    if (targetCollection.length === 0) {
      this.setState({isActive: true});
    }

    targetCollection = union(targetCollection, [event.target]);
  };
  onWindowDragOver = (event) => event.preventDefault();
  onWindowDragLeave = (event) => {
    event.preventDefault();

    targetCollection = without(targetCollection, event.target);

    if (targetCollection.length === 0) {
      this.setState({isActive: false});
    }
  };

  onDragEnter = () => this.setState({isHovered: true});
  onDragLeave = () => this.setState({isHovered: false});
  onDrop = (event) => {
    this.onDragLeave();
    this.onWindowDragLeave(event);
    this.props.onDropComplete(event.dataTransfer.files);
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
          Drop your files here.
        </div>
      );
    } else {
      return null;
    }
  }
}
