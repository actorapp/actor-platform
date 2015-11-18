/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';

export default class Success extends Component {
  constructor(props) {
    super(props);
  }

  static propTypes = {
    children: PropTypes.node
  };

  render() {
    return this.props.children;
  }
}
