/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';

import JoinGroupActions from '../actions/JoinGroupActions';

export default class Join extends Component {
  static propTypes = {
    params: PropTypes.object
  };

  constructor(props) {
    super(props);

    JoinGroupActions.joinGroupViaLink(props.params.token);
  }

  render() {
    return null;
  }
}
