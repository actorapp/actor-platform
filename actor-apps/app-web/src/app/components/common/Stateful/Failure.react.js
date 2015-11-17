/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';

export default class Failure extends Component {
  static propTypes = {
    children: PropTypes.node
  };

  render() {
    return this.props.children;
  }
}
