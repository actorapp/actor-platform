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

class Confirm extends Component {
  static propTypes = {
    message: PropTypes.node.isRequired,
    description: PropTypes.string,
    abortLabel: PropTypes.string,
    confirmLabel: PropTypes.string
  };

  constructor(props) {
    super(props);

    this.promise = new Promise((resolve, reject) => {
      this.reject = reject;
      this.resolve = resolve;
    });

    const SharedActor = SharedContainer.get();
    this.intlData = getIntlData(SharedActor.forceLocale);
  }

  componentDidMount() {
    ComposeActionCreators.toggleAutoFocus(false);
    findDOMNode(this.refs.confirm).focus();
    document.addEventListener('keydown', this.onKeyDown, false);
  }

  componentWillUnmount() {
    ComposeActionCreators.toggleAutoFocus(true);
    document.removeEventListener('keydown', this.onKeyDown, false);
  }

  renderDescription() {
    const { description } = this.props;
    if (!description) return null;

    return (
      <div className="modal__body">{description}</div>
    );
  }

  render() {
    const { message, abortLabel, confirmLabel } = this.props;

    return (
      <IntlProvider {...this.intlData}>
        <div className="modal">

          <div className="confirm">
            <div className="modal__content">

              <header className="modal__header">
                <h1>{message}</h1>
              </header>

              {this.renderDescription()}

              <footer className="modal__footer text-right">
                <button className="button" onClick={this.reject}>
                  {abortLabel || <FormattedMessage id="button.cancel"/>}
                </button>
                <button className="button button--lightblue" onClick={this.resolve} ref="confirm">
                  {confirmLabel ||<FormattedMessage id="button.ok"/>}
                </button>
              </footer>

            </div>
          </div>

        </div>
      </IntlProvider>
    );
  }

  onKeyDown = (event) => {
    if (event.keyCode === KeyCodes.ESC) {
      event.preventDefault();
      this.reject();
    }
  };
}

export default function confirm(message, options = {})  {
  const element = document.createElement('div');
  element.className = 'modal-overlay';


  const wrapper = document.body.appendChild(element);

  const component = render(createElement(Confirm, { message, ...options }), wrapper);

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
