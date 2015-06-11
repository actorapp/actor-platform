import assign from 'object-assign';
import classNames from 'classnames';
import React from 'react';
import LoginActionCreators from '../actions/LoginActionCreators';
import LoginStore from '../stores/LoginStore';

var getStateFromStores = function () {
  return ({
    smsRequested: LoginStore.isSmsRequested(),
    signupStarted: LoginStore.isSignupStarted(),
    phone: '',

    codeSent: false,
    code: '',

    name: ''
  });
};

export default React.createClass({
  contextTypes: {
    router: React.PropTypes.func
  },

  getInitialState: function () {
    return (assign({
      phone: '',
      code: '',
      name: ''
    }, getStateFromStores()));
  },

  componentWillMount: function () {
    if (LoginStore.isLoggedIn()) {
      window.setTimeout(() => this.context.router.replaceWith('/'), 0);
    } else {
      LoginStore.addChangeListener(this._onChange);
    }
  },

  componentWillUnmount: function () {
    LoginStore.removeChangeListener(this._onChange);
  },

  _onChange: function () {
    this.setState(getStateFromStores());
  },

  _onPhoneChange: function (event) {
    this.setState({phone: event.target.value});
  },

  _onCodeChange: function (event) {
    this.setState({code: event.target.value});
  },

  _onNameChange: function (event) {
    this.setState({name: event.target.value});
  },

  _onRequestSms: function (event) {
    event.preventDefault();
    LoginActionCreators.requestSms(this.state.phone);
  },

  _onSendCode: function (event) {
    event.preventDefault();
    LoginActionCreators.sendCode(this.context.router, this.state.code);
  },

  _onSignupRequested: function (event) {
    event.preventDefault();
    LoginActionCreators.sendSignup(this.context.router, this.state.name);
  },

  render: function () {
    let requestFormClassName = classNames('login__form', 'login__form--request', {
      'login__form--done': this.state.smsRequested,
      'login__form--active': !this.state.smsRequested && !this.state.signupStarted
    });
    let checkFormClassName = classNames('login__form', 'login__form--check', {
      'login__form--done': this.state.codeSent,
      'login__form--active': this.state.smsRequested && !this.state.signupStarted
    });
    let signupFormClassName = classNames('login__form', 'login__form--signup', {
      'login__form--done': this.state.codeSent,
      'login__form--active': this.state.signupStarted
    });

    let stepMesssageText = <p>Please enter your full <strong>phone</strong> number to receive <strong>authorization
      code</strong>.</p>;
    let smsRequested = this.state.smsRequested;

    if (smsRequested) {
      stepMesssageText =
        <p>We sent <strong>authorization code</strong> to your <strong>phone</strong>. Please enter it below.</p>;
    }

    return (
      <div className="login row center-xs middle-xs">
        <div className="login__window">
          <h2>Sign in to Actor messenger</h2>
          {stepMesssageText}
          <form className={requestFormClassName} onSubmit={this._onRequestSms}>
            <a href="#">Wrong?</a>
            <input disabled={this.state.smsRequested}
                   name="phone"
                   onChange={this._onPhoneChange}
                   placeholder="Phone number"
                   type="phone" />
            <button className="button button--primary button--wide">Request code</button>
          </form>
          <form className={checkFormClassName} onSubmit={this._onSendCode}>
            <input disabled={!this.state.smsRequested || this.state.codeSent}
                   name="code"
                   onChange={this._onCodeChange}
                   placeholder="Auth code"
                   type="number"/>
            <button className="button button--primary button--wide">Validate code</button>
          </form>
          <form className={signupFormClassName} onSubmit={this._onSignupRequested}>
            <input name="name"
                   onChange={this._onNameChange}
                   placeholder="Name"
                   type="text" />
            <button className="button button--primary button--wide">Sign up</button>
          </form>
        </div>
      </div>
    );
  }
});
