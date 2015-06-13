import React from 'react';

import LoginActionCreators from '../actions/LoginActionCreators';
import LoginStore from '../stores/LoginStore';

import classNames from 'classnames';

let getStateFromStores = function () {
  return ({
    smsRequested: LoginStore.isSmsRequested(),
    signupStarted: LoginStore.isSignupStarted(),
    phone: '',
    codeSent: false,
    code: '',
    name: ''
  });
};

class Login extends React.Component {
  componentWillMount() {
    if (LoginStore.isLoggedIn()) {
      window.setTimeout(() => this.context.router.replaceWith('/'), 0);
    } else {
      LoginStore.addChangeListener(this.onChange);
    }
  }

  componentWillUnmount() {
    LoginStore.removeChangeListener(this.onChange);
  }

  constructor() {
    super();

    this.onChange = this.onChange.bind(this);
    this.onPhoneChange = this.onPhoneChange.bind(this);
    this.onCodeChange = this.onCodeChange.bind(this);
    this.onNameChange = this.onNameChange.bind(this);
    this.onRequestSms = this.onRequestSms.bind(this);
    this.onSendCode = this.onSendCode.bind(this);
    this.onSignupRequested = this.onSignupRequested.bind(this);

    this.state = getStateFromStores();
  }

  onChange() {
    this.setState(getStateFromStores());
  }

  onPhoneChange(event) {
    this.setState({phone: event.target.value});
  }

  onCodeChange(event) {
    this.setState({code: event.target.value});
  }

  onNameChange(event) {
    this.setState({name: event.target.value});
  }

  onRequestSms(event) {
    event.preventDefault();
    LoginActionCreators.requestSms(this.state.phone);
  }

  onSendCode(event) {
    event.preventDefault();
    LoginActionCreators.sendCode(this.context.router, this.state.code);
  }

  onSignupRequested(event) {
    event.preventDefault();
    LoginActionCreators.sendSignup(this.context.router, this.state.name);
  }

  render() {
    let requestFormClassName = classNames('login__form', 'login__form--request', {
      'login__form--done': this.state.smsRequested,
      'login__form--active': !this.state.smsRequested && !this.state.signupStarted
    });
    let checkFormClassName = classNames('login__form', 'login__form--check', {
      'login__form--done': this.state.signupStarted,
      'login__form--active': this.state.smsRequested && !this.state.signupStarted
    });
    let signupFormClassName = classNames('login__form', 'login__form--signup', {
      'login__form--active': this.state.signupStarted
    });

    let stepMesssageText = <p>Please enter your full <strong>phone</strong> number to receive <strong>authorization
      code</strong>.</p>;
    let smsRequested = this.state.smsRequested;
    let signupStarted = this.state.signupStarted;

    if (smsRequested) {
      stepMesssageText =
        <p>We sent <strong>authorization code</strong> to your <strong>phone</strong>. Please enter it below.</p>;
    }
    if (signupStarted) {
      stepMesssageText =
        <p>To complete your <strong>registration</strong>, please enter your <strong>name</strong>.</p>;
    }

    return (
      <div className="login row center-xs middle-xs">
        <div className="login__window">
          <h2>Sign in to Actor messenger</h2>
          {stepMesssageText}
          <form className={requestFormClassName} onSubmit={this.onRequestSms}>
            <a href="#">Wrong?</a>
            <input disabled={this.state.smsRequested}
                   name="phone"
                   onChange={this.onPhoneChange}
                   placeholder="Phone number"
                   type="phone" />
            <button className="button button--primary button--wide">Request code</button>
          </form>
          <form className={checkFormClassName} onSubmit={this.onSendCode}>
            <input disabled={!this.state.smsRequested || this.state.signupStarted}
                   name="code"
                   onChange={this.onCodeChange}
                   placeholder="Auth code"
                   type="number"/>
            <button className="button button--primary button--wide">Validate code</button>
          </form>
          <form className={signupFormClassName} onSubmit={this.onSignupRequested}>
            <input name="name"
                   onChange={this.onNameChange}
                   placeholder="Name"
                   type="text" />
            <button className="button button--primary button--wide">Sign up</button>
          </form>
        </div>
      </div>
    );
  }
}

Login.contextTypes = {
  router: React.PropTypes.func
};

export default Login;
