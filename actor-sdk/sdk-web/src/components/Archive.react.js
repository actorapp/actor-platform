/*
 * Copyright (C) 2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';

class Archive extends Component {
  constructor(props){
    super(props);
  }

  static contextTypes = {
    delegate: PropTypes.object
  };

  render() {
    const { delegate } = this.context;

    return (
      <section className="archive">
        <h1>section.archive</h1>
      </section>
    );
  }
}

export default Archive;
