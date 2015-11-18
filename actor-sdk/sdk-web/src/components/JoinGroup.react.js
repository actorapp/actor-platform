/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import requireAuth from '../utils/require-auth';

import JoinGroupActions from '../actions/JoinGroupActions';

class JoinGroup extends Component {
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

export default requireAuth(JoinGroup);
