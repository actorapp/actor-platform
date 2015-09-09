/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React from 'react';
import { KeyCodes } from 'constants/ActorAppConstants'

class Confirm extends React.Component {
  static propTypes = {
    message: React.PropTypes.string.isRequired,
    description: React.PropTypes.string,
    abortLabel: React.PropTypes.string,
    confirmLabel: React.PropTypes.string
  };

  constructor(props) {
    super(props);

    this.promise = new Promise((resolve, reject) => {
      this.reject = reject;
      this.resolve = resolve;
    });
  }

  componentDidMount() {
    React.findDOMNode(this.refs.confirm).focus();
    document.addEventListener('keydown', this.onKeyDown, false);
  }

  componentWillUnmount() {
    document.removeEventListener('keydown', this.onKeyDown, false);
  }

  render() {
    const { message, description, abortLabel, confirmLabel } = this.props;

    const confirmDescription = description ? <div className="modal__body">{description}</div> : null;

    return (
      <div className="modal modal--confirm">
        <header className="modal__header">
          <h4 className="modal__header__title">{message}</h4>
        </header>
        {confirmDescription}
        <footer className="modal__footer text-right">
          <button className="button button" onClick={this.reject}>
            {abortLabel || 'Cancel'}
          </button>
          <button className="button button--lightblue" onClick={this.resolve} ref="confirm">
            {confirmLabel || 'OK'}
          </button>
        </footer>
      </div>
    );
  }

  onKeyDown = (event) => {
    if (event.keyCode === KeyCodes.ESC) {
      event.preventDefault();
      this.reject();
    }
  }
}

export default function confirm(message, options = {})  {
  let element = document.createElement('div');
  element.className = 'modal-backdrop';
  const wrapper = document.body.appendChild(element);
  const component = React.render(React.createElement(Confirm, {message, ...options}), wrapper);

  function cleanup() {
    React.unmountComponentAtNode(wrapper);
    setTimeout(() => wrapper.remove(), 0);
  }

  // Unmount component and remove it from DOM
  component.promise.then(
    () => cleanup(),
    () => cleanup()
  );

  return component.promise;
}
