/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { findDOMNode } from 'react-dom';
import classnames from 'classnames';

let cache = [];

/**
 * Class that represents a component for display voice message content
 */
class Voice extends Component {
  static propTypes = {
    fileUrl: PropTypes.string,
    duration: PropTypes.number.isRequired,
    className: PropTypes.string
  };

  constructor(props) {
    super(props);

    this.state = {
      isLoaded: this.isCached(),
      isPlaying: false,
      currentTime: 0,
      duration: props.duration / 1000
    };
  }

  componentDidMount() {
    const { fileUrl } = this.props;

    if (fileUrl) {
      this.createAudioElement(fileUrl);
    }
  }

  componentDidUpdate() {
    const { fileUrl } = this.props;

    if (fileUrl && !this.isCached()) {
      this.createAudioElement(fileUrl);
    }
  }

  componentWillUnmount() {
    if (this.audio) {
      this.audio.removeEventListener('loadeddata', this.handleLoading);
      this.audio.removeEventListener('timeupdate', this.handleTimeUpdate);
      this.audio.removeEventListener('ended', this.handlePlayEnding);
      this.audio.removeEventListener('canplaythrough', this.handleLoading);
    }
  }

  createAudioElement(fileUrl) {
    this.audio = new Audio(fileUrl);
    this.audio.volume = 1;
    this.audio.addEventListener('loadeddata', this.handleLoading);
    this.audio.addEventListener('timeupdate', this.handleTimeUpdate);
    this.audio.addEventListener('ended', this.handlePlayEnding);
    this.audio.addEventListener('canplaythrough', this.handleLoading);
    this.setCached();
  }

  isCached() {
    const { fileUrl } = this.props;
    return cache[fileUrl] === true;
  }

  setCached() {
    const { fileUrl } = this.props;
    cache[fileUrl] = true;
    this.setState({ isLoaded: cache[fileUrl] });
  }

  humanTime = (millis) => {
    const minutes = Math.floor(millis / 60000);
    const seconds = ((millis % 60000) / 1000).toFixed(0);

    return minutes + ':' + (seconds < 10 ? '0' : '') + seconds;
  };

  handleTimeUpdate = () => {
    this.setState({
      currentTime: this.audio.currentTime,
      duration: this.audio.duration
    })
  };

  handlePlayClick = () => {
    this.audio.play();
    this.setState({ isPlaying: true })
  };

  handlePauseClick = () => {
    this.audio.pause();
    this.handlePlayEnding();
  };

  handlePlayEnding = () => {
    this.setState({ isPlaying: false });
  };

  handleRewind = (event) => {
    const rewindRect = findDOMNode(this.refs.rewind).getBoundingClientRect();
    const rewindPosition = (event.clientX - rewindRect.left) / rewindRect.width;

    this.audio.currentTime = this.audio.duration * rewindPosition;
  };

  handleLoading = () => this.setCached();

  render() {
    const { className } = this.props;
    const { isPlaying, currentTime, duration, isLoaded } = this.state;
    const voiceClassName = classnames(className, 'row');

    const current = this.humanTime(currentTime * 1000);
    const total = this.humanTime(duration * 1000);
    const progress = (currentTime / duration) * 100;

    return (
      <div className={voiceClassName}>
        <div className="voice row">
          <div className="voice__controls">
            {
              !isLoaded
                ? <i className="material-icons" style={{ opacity: 0.3 }}>play_circle_filled</i>
                : isPlaying
                    ? <i className="material-icons" onClick={this.handlePauseClick}>pause_circle_filled</i>
                    : <i className="material-icons" onClick={this.handlePlayClick}>play_circle_filled</i>
            }
          </div>
          <div className="voice__body col-xs">
            <div className="row">
              <div className="col-xs text-left">
                <time className="voice__time voice__time--current">{current}</time>
              </div>
              <div className="col-xs text-right">
                <time className="voice__time voice__time--total">{total}</time>
              </div>
            </div>
            {
              isLoaded
                ? <div className="voice__rewind" onClick={this.handleRewind} ref="rewind">
                    <div className="played" style={{ width: progress + '%' }}/>
                  </div>
                : <div className="voice__rewind voice__rewind--loading"/>
            }
          </div>
        </div>
        <div className="col-xs"/>
      </div>
    );
  }
}

export default Voice;
