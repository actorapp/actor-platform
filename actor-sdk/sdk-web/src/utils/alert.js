/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes, createElement } from 'react';
import { render, findDOMNode, unmountComponentAtNode } from 'react-dom';
import { KeyCodes } from '../constants/ActorAppConstants';
import ComposeActionCreators from '../actions/ComposeActionCreators';
import { IntlProvider, FormattedMessage } from 'react-intl';
import { getIntlData } from '../l18n';
import SharedContainer from '../utils/SharedContainer';

class Alert extends Component {
  static propTypes = {
    message: PropTypes.node.isRequired,
    description: PropTypes.string,
    okLabel: PropTypes.string
  };

  constructor(props) {
    super(props);

    this.promise = new Promise((resolve) => {
      this.resolve = resolve;
    });

    const SharedActor = SharedContainer.get();
    this.intlData = getIntlData(SharedActor.forceLocale);

    this.handleKeyDown = this.handleKeyDown.bind(this);
  }

  componentDidMount() {
    ComposeActionCreators.toggleAutoFocus(false);
    findDOMNode(this.refs.ok).focus();
    document.addEventListener('keydown', this.handleKeyDown, false);
  }

  componentWillUnmount() {
    ComposeActionCreators.toggleAutoFocus(true);
    document.removeEventListener('keydown', this.handleKeyDown, false);
  }

  handleKeyDown(event) {
    if (event.keyCode === KeyCodes.ESC) {
      event.preventDefault();
      this.resolve();
    }
  }

  renderDescription() {
    const { description } = this.props;
    if (!description) return null;

    return (
      <div className="modal__body">{description}</div>
    );
  }

  render() {
    const { message, okLabel } = this.props;

    return (
      <IntlProvider {...this.intlData}>
        <div className="modal">

          <div className="alert">
            <div className="modal__content">

              <header className="modal__header">
                <h1>{message}</h1>
              </header>

              {this.renderDescription()}

              <footer className="modal__footer text-right">
                <button className="button button--lightblue" onClick={this.resolve} ref="ok">
                  {okLabel ||<FormattedMessage id="button.ok"/>}
                </button>
              </footer>

            </div>
          </div>

        </div>
      </IntlProvider>
    );
  }
}

export default function alert(message, options = {})  {
  let element = document.createElement('div');
  element.className = 'modal-overlay';
  const wrapper = document.body.appendChild(element);

  const component = render(createElement(Alert, { message, ...options }), wrapper);

  function cleanup() {
    unmountComponentAtNode(wrapper);
    setImmediate(() => wrapper.remove());
  }

  // Unmount component and remove it from DOM
  component.promise.then(
    () => cleanup(),
    () => cleanup()
  );

  return component.promise;
}
