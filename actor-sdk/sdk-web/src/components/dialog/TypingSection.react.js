/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import ReactMixin from 'react-mixin';
import addons from 'react/addons';
const {addons: { PureRenderMixin }} = addons;

import classNames from 'classnames';

import DialogStore from '../../stores/DialogStore';

class Typing extends Component {
  constructor(props) {
    super(props);

    this.state = {
      typing: null,
      show: false
    }
  }

  componentDidMount() {
    DialogStore.addTypingListener(this.onTypingChange);
  }

  componentWillUnmount() {
    DialogStore.removeTypingListener(this.onTypingChange);
  }

  onTypingChange = () => {
    const typing = DialogStore.getSelectedDialogTyping();
    if (typing === null) {
      this.setState({show: false});
    } else {
      this.setState({typing: typing, show: true});
    }
  };

  render() {
    const typing = this.state.typing;
    const show = this.state.show;
    const typingClassName = classNames('typing', {
      'typing--hidden': show === false
    });

    return (
      <div className={typingClassName}>
        <div className="typing-indicator"><i></i><i></i><i></i></div>
        <span>{typing}</span>
      </div>
    );
  }
}

ReactMixin.onClass(Typing, PureRenderMixin);

export default Typing;
