/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import classnames from 'classnames';
import ReactMixin from 'react-mixin';
import { IntlMixin } from 'react-intl';

class Voice extends Component {
  static propTypes = {
    content: PropTypes.object.isRequired,
    className: PropTypes.string
  };

  constructor(props) {
    super(props);

    this.state = {
      isPlaying: false,
      currentTime: 0,
      duration: props.content.duration / 1000
    };

    this.audio = new Audio(props.content.fileUrl);
    this.audio.volume = 1;
    this.audio.addEventListener('timeupdate', this.handleTimeUpdate);
    this.audio.addEventListener('ended', this.handlePlayEnding);
  }

  componentWillUnmount() {
    this.audio.removeEventListener('timeupdate', this.handleTimeUpdate);
    this.audio.removeEventListener('ended', this.handlePlayEnding);
  }

  humanTime = (millis) => {
    const minutes = Math.floor(millis / 60000);
    const seconds = ((millis % 60000) / 1000).toFixed(0);
    return minutes + ':' + (seconds < 10 ? '0' : '') + seconds;
  };

  handleTimeUpdate = (event) => {
    this.setState({
      currentTime: this.audio.currentTime,
      duration: this.audio.duration
    })
  };

  handlePlayClick = () => {
    this.audio.play();
    this.setState({isPlaying: true})
  };

  handlePauseClick = () => {
    this.audio.pause();
    this.handlePlayEnding();
  };

  handlePlayEnding = () => this.setState({isPlaying: false});

  handleRewind = (event) => {
    const rewindRect = React.findDOMNode(this.refs.rewind).getBoundingClientRect();
    const rewindPosition = (event.clientX - rewindRect.left) / rewindRect.width;

    this.audio.currentTime = this.audio.duration * rewindPosition;
  };

  render() {
    const { className } = this.props;
    const { isPlaying, currentTime, duration } = this.state;
    const voiceClassName = classnames(className, 'row');

    const current = this.humanTime(currentTime * 1000);
    const total = this.humanTime(duration * 1000);
    const progress = (currentTime / duration) * 100;

    return (
      <div className={voiceClassName}>
        <div className="voice row">
          <div className="voice__controls">
            {
              isPlaying
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
            <div className="voice__rewind" onClick={this.handleRewind} ref="rewind">
              <div className="played" style={{width: progress + '%'}}/>
            </div>
          </div>
        </div>
        <div className="col-xs"></div>
      </div>
    );
  }
}

ReactMixin.onClass(Voice, IntlMixin);

export default Voice;
