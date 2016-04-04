/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import classnames from 'classnames';

class DropZone extends Component {
  static propTypes = {
    children: PropTypes.node.isRequired,
    onDropComplete: PropTypes.func.isRequired
  };

  constructor(props) {
    super(props);

    this.dragging = false;
    this.state = {
      isActive: false,
      isHovered: false
    };

    this.onWindowDragEnter = this.onWindowDragEnter.bind(this);
    this.onWindowDragOver = this.onWindowDragOver.bind(this);
    this.onWindowDragLeave = this.onWindowDragLeave.bind(this);

    this.onDrop = this.onDrop.bind(this);
    this.onDragEnter = this.onDragEnter.bind(this);
    this.onDragLeave = this.onDragLeave.bind(this);
  }

  componentDidMount() {
    window.addEventListener('dragenter', this.onWindowDragEnter, false);
    window.addEventListener('dragover', this.onWindowDragOver, false);
    window.addEventListener('dragleave', this.onWindowDragLeave, false);
  }

  componentWillUnmount() {
    window.addEventListener('dragenter', this.onWindowDragEnter, false);
    window.addEventListener('dragover', this.onWindowDragOver, false);
    window.addEventListener('dragleave', this.onWindowDragLeave, false);
  }

  onWindowDragEnter() {
    this.dragging = true;
    if (this.state.isActive) {
      return;
    }

    this.setState({isActive: true});
  }

  onWindowDragOver() {
    this.dragging = true;
  }

  onWindowDragLeave() {
    this.dragging = false;
    clearTimeout(this.timeout);
    this.timeout = setTimeout(() => {
      if (!this.dragging) {
        this.setState({isActive: false});
      }
    }, 300);
  }

  onDrop(event) {
    event.preventDefault();
    event.stopPropagation();

    this.onDragLeave();
    this.props.onDropComplete(event.dataTransfer.files);
  }

  onDragOver(event) {
    // Makes it possible to drag files from chrome's download bar
    // http://stackoverflow.com/questions/19526430/drag-and-drop-file-uploads-from-chrome-downloads-bar
    try {
      const effect = event.dataTransfer.effectAllowed;
      if (effect === 'move' || effect === 'linkMove') {
        event.dataTransfer.dropEffect = 'move';
      } else {
        event.dataTransfer.dropEffect = 'copy';
      }
    } catch (e) {
      // do nothing
    }

    event.preventDefault();
    event.stopPropagation();
  }

  onDragEnter() {
    this.setState({isHovered: true});
  }

  onDragLeave() {
    this.setState({isHovered: false});
  }

  render() {
    const { isActive, isHovered } = this.state;

    if (!isActive) {
      return null;
    }

    const className = classnames('dropzone', {
      'dropzone--hover': isHovered
    });

    return (
      <div
        className={className}
        onDrop={this.onDrop}
        onDragOver={this.onDragOver}
        onDragEnter={this.onDragEnter}
        onDragLeave={this.onDragLeave}
      >
        {this.props.children}
      </div>
    );
  }
}

export default DropZone;
