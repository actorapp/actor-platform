import _ from 'lodash';

import React from 'react';

import { AuthSteps } from '../constants/ActorAppConstants';

import LoginActionCreators from '../actions/LoginActionCreators';
import LoginStore from '../stores/LoginStore';

import classNames from 'classnames';

let getStateFromStores = function () {
  return ({
    step: LoginStore.getStep(),
    errors: LoginStore.getErrors(),
    smsRequested: LoginStore.isSmsRequested(),
    signupStarted: LoginStore.isSignupStarted(),
    codeSent: false,
    code: ''
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
    this.onWrongNumberClick = this.onWrongNumberClick.bind(this);

    this.state = _.assign({
      phone: '',
      name: ''
    }, getStateFromStores());
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

  onWrongNumberClick(event) {
    event.preventDefault();
    LoginActionCreators.wrongNumberClick();
  }

  render() {
    let requestFormClassName = classNames('login__form', 'login__form--request', {
      'login__form--done': this.state.step > AuthSteps.PHONE_WAIT,
      'login__form--active': this.state.step === AuthSteps.PHONE_WAIT
    });
    let checkFormClassName = classNames('login__form', 'login__form--check', {
      'login__form--done': this.state.step > AuthSteps.CODE_WAIT,
      'login__form--active': this.state.step === AuthSteps.CODE_WAIT
    });
    let signupFormClassName = classNames('login__form', 'login__form--signup', {
      'login__form--active': this.state.step === AuthSteps.SIGNUP_NAME_WAIT
    });
    //let smsRequested = this.state.smsRequested;
    //let signupStarted = this.state.signupStarted;

    //let stepMesssageText =
    //  <p>Please enter your full <strong>phone</strong> number to receive <strong>authorization code</strong>.</p>;
    //
    //if (smsRequested) {
    //  stepMesssageText =
    //    <p>We sent <strong>authorization code</strong> to your <strong>phone</strong>. Please enter it below.</p>;
    //}
    //if (signupStarted) {
    //  stepMesssageText =
    //    <p>To complete your <strong>registration</strong>, please enter your <strong>name</strong>.</p>;
    //}

    return (
      <section className="login-new row center-xs middle-xs">
        <div className="login-new__welcome col-xs row center-xs middle-xs">
          <img alt="Actor messenger" className="logo"
               src="/assets/img/logo.png" srcSet="/assets/img/logo@2x.png 2x"/>

          <article>
            <h1 className="login-new__heading">Welcome to <strong>Actor</strong></h1>
            <p>
              Actor Messenger brings all your business network connections into one place, makes it easily accessible wherever you go.
            </p>
            <p>
              Our aim is to make your work easier, reduce your email amount, make the business world closer by reducing time to find right contacts.
            </p>
          </article>

          <footer>
            <div className="pull-left">
              Actor Messenger © 2015
            </div>
            <div className="pull-right">
              <a href="https://actor.im/ios">iPhone</a>
              <a href="https://actor.im/android">Android</a>
            </div>
          </footer>
        </div>

        <div className="login-new__form col-xs-6 col-md-4 row center-xs middle-xs">
          <div>
            <h1 className="login-new__heading">Sign in</h1>
            <form className={requestFormClassName} onSubmit={this.onRequestSms}>
              <input disabled={this.state.step > AuthSteps.PHONE_WAIT}
                     name="phone"
                     onChange={this.onPhoneChange}
                     placeholder="Phone number"
                     type="phone"
                     value={this.state.phone}/>
              <span>{this.state.errors.phone}</span>
              <footer className="text-center">
                <button className="button button--blue">Request code</button>
              </footer>
            </form>
            <form className={checkFormClassName} onSubmit={this.onSendCode}>
              <input disabled={this.state.step > AuthSteps.CODE_WAIT}
                     name="code"
                     onChange={this.onCodeChange}
                     placeholder="Auth code"
                     type="text"
                     value={this.state.code}/>
              <span>{this.state.errors.code}</span>
              <footer className="text-center">
                <button className="button button--blue">Check code</button>
              </footer>
            </form>
            <form className={signupFormClassName} onSubmit={this.onSignupRequested}>
              <input name="name"
                     onChange={this.onNameChange}
                     placeholder="Your name"
                     type="text"
                     value={this.state.name}/>
              <span>{this.state.errors.signup}</span>
              <footer className="text-center">
                <button className="button button--blue">Sign up</button>
              </footer>
            </form>
          </div>
        </div>
      </section>
    );
    //return (
    //  <div className="login row center-xs middle-xs">
    //    <div className="login__window">
    //      <h2>Sign in to Actor messenger</h2>
    //      {stepMesssageText}
    //      <form className={requestFormClassName} onSubmit={this.onRequestSms}>
    //        <a onClick={this.onWrongNumberClick}>Wrong?</a>
    //        <input disabled={this.state.step > AuthSteps.PHONE_WAIT}
    //               name="phone"
    //               onChange={this.onPhoneChange}
    //               placeholder="Phone number"
    //               type="phone" />
    //        <span>{this.state.errors.phone}</span>
    //        <button className="button button--primary button--wide">Request code</button>
    //      </form>
    //      <form className={checkFormClassName} onSubmit={this.onSendCode}>
    //        <input disabled={this.state.step > AuthSteps.CODE_WAIT}
    //               name="code"
    //               onChange={this.onCodeChange}
    //               value={this.state.code}
    //               placeholder="Auth code"
    //               type="number"/>
    //        <span>{this.state.errors.code}</span>
    //        <button className="button button--primary button--wide">Validate code</button>
    //      </form>
    //      <form className={signupFormClassName} onSubmit={this.onSignupRequested}>
    //        <input name="name"
    //               onChange={this.onNameChange}
    //               placeholder="Name"
    //               type="text" />
    //        <span>{this.state.errors.signup}</span>
    //        <button className="button button--primary button--wide">Sign up</button>
    //      </form>
    //    </div>
    //  </section>
    //);
  }
}

Login.contextTypes = {
  router: React.PropTypes.func
};

export default Login;
