/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import { Container } from 'flux/utils';

import TypingStore from '../../stores/TypingStore';

class Typing extends Component {
  static getStores() {
    return [TypingStore];
  }

  static calculateState() {
    return TypingStore.getState();
  }

  render() {
    const { typing } = this.state;

    if (!typing) {
      return <div className="typing" />;
    }

    return (
      <div className="typing">
        <div className="typing__indicator">
          <i></i><i></i><i></i>
        </div>
        <span className="typing__text">{typing}</span>
      </div>
    );
  }
}

export default Container.create(Typing);
