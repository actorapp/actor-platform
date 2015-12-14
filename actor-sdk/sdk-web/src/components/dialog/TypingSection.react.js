/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import { Container } from 'flux/utils';
import classNames from 'classnames';

import TypingStore from '../../stores/TypingStore';

class Typing extends Component {
  static getStores = () => [TypingStore];

  static calculateState() {
    const typing = TypingStore.getTyping();
    const newState = (typing === null) ? {show: false} : {typing, show: true};
    return newState;
  }

  constructor(props) {
    super(props);

    this.state = {
      typing: null
    }
  }

  render() {
    const { show, typing } = this.state;

    const typingClassName = classNames('typing', {
      'typing--hidden': !show
    });

    return (
      <div className={typingClassName}>
        <div className="typing-indicator"><i></i><i></i><i></i></div>
        <span>{typing}</span>
      </div>
    );
  }
}

export default Container.create(Typing);
