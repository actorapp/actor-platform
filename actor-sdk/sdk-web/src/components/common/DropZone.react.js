/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import EventListener from 'fbjs/lib/EventListener';
import classnames from 'classnames';

class DropZone extends Component {
  static propTypes = {
    children: PropTypes.node.isRequired,
    onDropComplete: PropTypes.func.isRequired
  };

  constructor(props) {
    super(props);

    this.windowDragging = false;
    this.zoneDragging = false;

    this.state = {
      isActive: false,
      isHovered: false
    };

    this.onWindowDrop = this.onWindowDrop.bind(this);
    this.onWindowDragEnter = this.onWindowDragEnter.bind(this);
    this.onWindowDragOver = this.onWindowDragOver.bind(this);
    this.onWindowDragLeave = this.onWindowDragLeave.bind(this);

    this.onDrop = this.onDrop.bind(this);
    this.onDragEnter = this.onDragEnter.bind(this);
    this.onDragOver = this.onDragOver.bind(this);
    this.onDragLeave = this.onDragLeave.bind(this);
  }

  shouldComponentUpdate(nextProps, nextState) {
    return nextState.isActive !== this.state.isActive ||
           nextState.isHovered !== this.state.isHovered;
  }

  componentDidMount() {
    this.listeners = [
      EventListener.listen(window, 'drop', this.onWindowDrop),
      EventListener.listen(window, 'dragenter', this.onWindowDragEnter),
      EventListener.listen(window, 'dragover', this.onWindowDragOver),
      EventListener.listen(window, 'dragleave', this.onWindowDragLeave)
    ];
  }

  componentWillUnmount() {
    this.listeners.forEach((listener) => {
      listener.remove();
    });

    this.listeners = null;
  }

  onWindowDrop(event) {
    event.preventDefault();
    event.stopPropagation();

    this.setState({ isActive: false, isHovered: false });
  }

  onWindowDragEnter() {
    this.windowDragging = true;
    clearTimeout(this.windowTimeout);

    if (this.state.isActive) {
      return;
    }

    this.setState({ isActive: true });
  }

  onWindowDragOver(event) {
    event.preventDefault();
    event.stopPropagation();

    this.windowDragging = true;
    clearTimeout(this.windowTimeout);
  }

  onWindowDragLeave() {
    this.windowDragging = false;
    clearTimeout(this.windowTimeout);

    this.windowTimeout = setTimeout(() => {
      if (!this.windowDragging) {
        this.setState({ isActive: false });
      }
    }, 60);
  }

  onDrop(event) {
    event.preventDefault();
    event.stopPropagation();

    this.onDragLeave();
    this.onWindowDragLeave();
    this.props.onDropComplete(event.dataTransfer.files);
  }

  onDragEnter() {
    this.zoneDragging = true;
    this.windowDragging = true;
    clearTimeout(this.zoneTimeout);
    clearTimeout(this.windowTimeout);

    if (this.state.isHovered) {
      return;
    }

    this.setState({ isHovered: true });
  }

  onDragOver(event) {
    event.preventDefault();
    event.stopPropagation();

    this.zoneDragging = true;
    this.windowDragging = true;
    clearTimeout(this.zoneTimeout);
    clearTimeout(this.windowTimeout);

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
  }

  onDragLeave() {
    this.zoneDragging = false;
    clearTimeout(this.zoneTimeout);

    this.zoneTimeout = setTimeout(() => {
      if (!this.zoneDragging) {
        this.setState({ isHovered: false });
      }
    }, 60);
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
