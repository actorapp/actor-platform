/*
 * Copyright (C) 2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';

import ConnectionState from './common/ConnectionState.react';

class Archive extends Component {
  constructor(props){
    super(props);
  }

  render() {
    return (
    <section className="main">
      <div className="flexrow">
        <section className="archive">
          <ConnectionState/>
          <h1>section.archive</h1>
        </section>
      </div>
    </section>
    );
  }
}

export default Archive;
