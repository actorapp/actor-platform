/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import { Container } from 'flux/utils';

import LoggerStore from '../../stores/LoggerStore';
import { loggerToggle } from '../../actions/LoggerActionCreators';

import Scrollbar from '../common/Scrollbar.react';
import LoggerFilter from './LoggerFilter.react';
import LoggerRow from './LoggerRow.react';

class LoggerSection extends Component {
  static getStores() {
    return [LoggerStore];
  }

  static calculateState() {
    const isOpen = LoggerStore.isOpen();
    if (!isOpen) {
      return { isOpen: false };
    }

    const logs = LoggerStore.getLogs();
    return {
      isOpen,
      logs,
      length: logs.length
    };
  }

  onClose() {
    loggerToggle();
  }

  renderLogs() {
    const result = [];

    const { logs } = this.state;
    for (let i = logs.length - 1; i >= 0; i--) {
      result.push(
        <LoggerRow data={logs[i]} key={i} />
      );
    }

    return result;
  }

  render() {
    if (!this.state.isOpen) {
      return <section className="activity logger" />;
    }

    return (
      <section className="activity logger activity--shown">
        <div className="activity__body logger__body">
          <div className="logger__controls">
            <button className="button button--icon" type="button" onClick={this.onClose}>
              <i className="material-icons">close</i>
            </button>
          </div>
          <LoggerFilter />
          <Scrollbar>
            <div className="logger__container">
              {this.renderLogs()}
            </div>
          </Scrollbar>
        </div>
      </section>
    );
  }
}

export default Container.create(LoggerSection);
