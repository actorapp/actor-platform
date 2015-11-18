/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';

class Deactivated extends Component {
  constructor(props){
    super(props);
  }

  render() {
    return (
      <div className="deactivated row center-xs middle-xs">
        <div className="deactivated__window">
          <h2>Tab deactivated</h2>
          <p>
            Oops, you have opened another tab with Actor, so we had to deactivate this one to prevent some dangerous things happening.
          </p>
        </div>
      </div>
    );
  }
}

export default Deactivated;
