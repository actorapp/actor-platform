/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import { Container } from 'flux/utils';
import classNames from 'classnames';

import DialogStore from '../../stores/DialogStore';

class Typing extends Component {
  static getStores = () => [DialogStore];

  static calculateState() {
    const typing = DialogStore.getTyping();
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
