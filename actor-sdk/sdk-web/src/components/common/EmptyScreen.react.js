/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';

import ConnectionState from './ConnectionState.react';

class EmptyScreen extends Component {
  constructor(props) {
    super(props);
  }

  render() {
    return (
      <section className="dialog dialog--empty row center-xs middle-xs">
        <ConnectionState/>

        <div className="advice">
          <div className="actor-logo">
            <svg className="icon icon--gray"
                 dangerouslySetInnerHTML={{__html: '<use xlink:href="assets/images/icons.svg#star"/>'}}/>
          </div>
          <h2>Try to be better than yesterday!</h2>
        </div>
      </section>
    );
  }
}

export default EmptyScreen;
