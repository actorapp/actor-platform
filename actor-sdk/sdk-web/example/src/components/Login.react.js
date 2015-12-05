/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';

export default class Login extends Component {
  constructor(props){
    super(props);
  }

  render() {
    return (
      <section className="login-new row center-xs middle-xs">
          <article>
            <h1 className="login-new__heading">This is <strong>custom login comonent</strong></h1>
          </article>
      </section>
    );
  }
}
