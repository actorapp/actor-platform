/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import { Container } from 'flux/utils';
import classNames from 'classnames';

import LoggerStore from '../../stores/LoggerStore';

import LoggerFilter from './LoggerFilter.react';
import LoggerRow from './LoggerRow.react';

class LoggerSection extends Component {
  static getStores = () => [LoggerStore];

  static calculateState() {
    return LoggerStore.getState();
  }

  renderLogs() {
    return this.state.logs.map((data, index) => (
      <LoggerRow {...data} key={index} />
    ));
  }

  renderBody() {
    return (
      <div className="activity__body logger">
        <LoggerFilter />
        <div className="logger__row__container">
          {this.renderLogs()}
        </div>
      </div>
    );
  }

  render() {
    const className = classNames('activity', {
      'activity--shown': this.state.isOpen
    });

    return (
      <section className={className}>
        {this.renderBody()}
      </section>
    );
  }
}

export default Container.create(LoggerSection);
