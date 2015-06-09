'use strict';

import React from 'react';

export class JoinGroup extends React.Component {
  constructor (props) {
    super(props);

    this.contextTypes = {
      router: React.PropTypes.func
    }
  }

  componentWillMount () {
    router.replaceWith('/');
  }

  render () {
    return null;
  }
}
