/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import _ from 'lodash';

import React from 'react';
import classnames from 'classnames';
import ReactMixin from 'react-mixin';
import { IntlMixin } from 'react-intl';
import { Styles, TextField } from 'material-ui';

import { AuthSteps } from 'constants/ActorAppConstants';

import LoginActionCreators from 'actions/LoginActionCreators';
import LoginStore from 'stores/LoginStore';

import ActorTheme from 'constants/ActorTheme';

const ThemeManager = new Styles.ThemeManager();

let getStateFromStores = function () {
  return ({
    step: LoginStore.getStep(),
    errors: LoginStore.getErrors(),
    smsRequested: LoginStore.isSmsRequested(),
    codeSended: LoginStore.isCodeSended(),
    signupStarted: LoginStore.isSignupStarted(),
    codeSent: false
  });
};

@ReactMixin.decorate(IntlMixin)
class Login extends React.Component {
  static contextTypes = {
    router: React.PropTypes.func
  };

  static childContextTypes = {
    muiTheme: React.PropTypes.object
  };

  getChildContext() {
    return {
      muiTheme: ThemeManager.getCurrentTheme()
    };
  }

  componentWillUnmount() {
    LoginStore.removeChangeListener(this.onChange);
  }

  componentDidMount() {
    this.handleFocus();
  }

  componentDidUpdate() {
    this.handleFocus();
  }

  constructor(props) {
    super(props);

    this.state = _.assign({
      phone: '',
      name: '',
      code: ''
    }, getStateFromStores());

    ThemeManager.setTheme(ActorTheme);

    if (LoginStore.isLoggedIn()) {
      window.setTimeout(() => this.context.router.replaceWith('/'), 0);
    } else {
      LoginStore.addChangeListener(this.onChange);
    }

  }

  onChange = () => this.setState(getStateFromStores());
  onPhoneChange = event => this.setState({phone: event.target.value});
  onCodeChange = event => this.setState({code: event.target.value});
  onNameChange = event => this.setState({name: event.target.value});

  onRequestSms = event => {
    event.preventDefault();
    LoginActionCreators.requestSms(this.state.phone);
  };

  onSendCode = event => {
    event.preventDefault();
    LoginActionCreators.sendCode(this.context.router, this.state.code);
  };

  onSignupRequested = event => {
    event.preventDefault();
    LoginActionCreators.sendSignup(this.context.router, this.state.name);
  };

  onWrongNumberClick = event => {
    event.preventDefault();
    LoginActionCreators.wrongNumberClick();
  };


  handleFocus = () => {
    switch (this.state.step) {
      case AuthSteps.PHONE_WAIT:
        this.refs.phone.focus();
        break;
      case AuthSteps.CODE_WAIT:
        this.refs.code.focus();
        break;
      case AuthSteps.SIGNUP_NAME_WAIT:
        this.refs.name.focus();
        break;
      default:
        return;
    }
  };

  render() {
    const { smsRequested, codeSended, signupStarted } = this.state;

    let requestFormClassName = classnames('login__form', 'login__form--request', {
      'login__form--done': this.state.step > AuthSteps.PHONE_WAIT,
      'login__form--active': this.state.step === AuthSteps.PHONE_WAIT
    });
    let checkFormClassName = classnames('login__form', 'login__form--check', {
      'login__form--done': this.state.step > AuthSteps.CODE_WAIT,
      'login__form--active': this.state.step === AuthSteps.CODE_WAIT
    });
    let signupFormClassName = classnames('login__form', 'login__form--signup', {
      'login__form--active': this.state.step === AuthSteps.SIGNUP_NAME_WAIT
    });

    const spinner = (
      <div className="spinner">
        <div/><div/><div/><div/><div/><div/><div/><div/><div/><div/><div/><div/>
      </div>
    );
    const phoneWaitSpinner = smsRequested ? spinner : null;
    const codeWaitSpinner = codeSended ? spinner : null;
    const signupWaitSpinner = signupStarted ? spinner : null;

    return (
      <section className="login-new row center-xs middle-xs">
        <div className="login-new__welcome col-xs row center-xs middle-xs">
          <img alt="Actor messenger"
               className="logo"
               src="assets/img/logo.png"
               srcSet="assets/img/logo@2x.png 2x"/>

          <article>
            <h1 className="login-new__heading">Welcome to <strong>Actor</strong></h1>
            <p>
              Actor Messenger brings all your business network connections into one place,
              makes it easily accessible wherever you go.
            </p>
            <p>
              Our aim is to make your work easier, reduce your email amount,
              make the business world closer by reducing time to find right contacts.
            </p>
          </article>

          <footer>
            <div className="pull-left">
              Actor Messenger © 2015
            </div>
            <div className="pull-right">
              <a href="//actorapp.ghost.io/desktop-apps">Desktop</a>
              <a href="//actor.im/ios">iPhone</a>
              <a href="//actor.im/android">Android</a>
            </div>
          </footer>
        </div>

        <div className="login-new__form col-xs-6 col-md-4 row center-xs middle-xs">
          <div>
            <h1 className="login-new__heading">{this.getIntlMessage('login.signIn')}</h1>

            <form className={requestFormClassName} onSubmit={this.onRequestSms}>
              <a className="wrong" onClick={this.onWrongNumberClick}>{this.getIntlMessage('login.wrong')}</a>
              <TextField className="login__form__input"
                         disabled={smsRequested || this.state.step > AuthSteps.PHONE_WAIT}
                         errorText={this.state.errors.phone}
                         floatingLabelText={this.getIntlMessage('login.phone')}
                         onChange={this.onPhoneChange}
                         ref="phone"
                         type="text"
                         value={this.state.phone}/>

              <footer className="text-center">
                <button className="button button--rised button--wide"
                        type="submit">
                  {this.getIntlMessage('button.requestCode')}
                  {phoneWaitSpinner}
                </button>
              </footer>
            </form>
            <form className={checkFormClassName} onSubmit={this.onSendCode}>
              <TextField className="login__form__input"
                         disabled={codeSended || this.state.step > AuthSteps.CODE_WAIT}
                         errorText={this.state.errors.code}
                         floatingLabelText={this.getIntlMessage('login.authCode')}
                         onChange={this.onCodeChange}
                         ref="code"
                         type="text"
                         value={this.state.code}/>

              <footer className="text-center">
                <button className="button button--rised button--wide"
                        type="submit">
                  {this.getIntlMessage('button.checkCode')}
                  {codeWaitSpinner}
                </button>
              </footer>
            </form>
            <form className={signupFormClassName} onSubmit={this.onSignupRequested}>
              <TextField className="login__form__input"
                         errorText={this.state.errors.signup}
                         floatingLabelText={this.getIntlMessage('login.yourName')}
                         onChange={this.onNameChange}
                         ref="name"
                         type="text"
                         value={this.state.name}/>

              <footer className="text-center">
                <button className="button button--rised button--wide"
                        type="submit">
                  {this.getIntlMessage('button.signUp')}
                  {signupWaitSpinner}
                </button>
              </footer>
            </form>
          </div>
        </div>
      </section>
    );
  }
}

export default Login;
