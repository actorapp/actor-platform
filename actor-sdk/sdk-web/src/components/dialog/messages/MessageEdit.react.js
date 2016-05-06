/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { shouldComponentUpdate } from 'react-addons-pure-render-mixin';
import { KeyCodes } from '../../../constants/ActorAppConstants';
import ComposeTextArea from '../compose/ComposeTextArea.react';

class MessageEdit extends Component {
  static propTypes = {
    message: PropTypes.object.isRequired,
    sendByEnter: PropTypes.bool.isRequired,
    onSubmit: PropTypes.func.isRequired
  };

  // TODO: pass real props
  static defaultProps = {
    sendByEnter: true
  }

  constructor(props) {
    super(props);

    this.state = {
      text: props.message.content.text
    };

    this.onSubmit = this.onSubmit.bind(this);
    this.onTyping = this.onTyping.bind(this);
    this.onKeyDown = this.onKeyDown.bind(this);

    this.shouldComponentUpdate = shouldComponentUpdate.bind(this);
  }

  componentDidMount() {
    console.debug('REPORT ABOUT MOUNT!');
  }

  onSubmit() {
    this.props.onSubmit(this.props.message, this.state.text);
  }

  onTyping(text) {
    this.setState({ text });
  }

  onKeyDown(event) {
    if (event.keyCode === KeyCodes.ESC) {
      event.preventDefault();
      this.onSubmit();
    }
  }

  render() {
    return (
      <ComposeTextArea
        autoFocus
        value={this.state.text}
        sendByEnter={this.props.sendByEnter}
        onSubmit={this.onSubmit}
        onTyping={this.onTyping}
        onKeyDown={this.onKeyDown}
      />
    );
  }
}

export default MessageEdit;
