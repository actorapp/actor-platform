/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';

export default class Pending extends Component {
  static propTypes = {
    children: PropTypes.node
  };

  render() {
    return this.props.children;
  }
}
