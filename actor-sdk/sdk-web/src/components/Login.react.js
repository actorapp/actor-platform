/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { Container } from 'flux/utils';
import classnames from 'classnames';
import ReactMixin from 'react-mixin';
import { IntlMixin, FormattedHTMLMessage } from 'react-intl';
import { Styles, TextField } from 'material-ui';
import SharedContainer from '../utils/SharedContainer';
import { appName, AuthSteps } from '../constants/ActorAppConstants';

import LoginActionCreators from '../actions/LoginActionCreators';

import LoginStore from '../stores/LoginStore';

import ActorTheme from '../constants/ActorTheme';

const ThemeManager = new Styles.ThemeManager();

class Login extends Component {
  constructor(props){
    super(props);

    const SharedActor = SharedContainer.get();
    this.appName = SharedActor.appName ? SharedActor.appName : appName;
  }

  static contextTypes = {
    router: PropTypes.func
  };

  static propTypes = {
    query: PropTypes.object
  };

  static childContextTypes = {
    muiTheme: PropTypes.object
  };

  getChildContext() {
    return {
      muiTheme: ThemeManager.getCurrentTheme()
    }
  };

  static getStores() {
    return [LoginStore];
  }

  static calculateState() {
    return {
      login: LoginStore.getLogin(),
      code: LoginStore.getCode(),
      name: LoginStore.getName(),
      step: LoginStore.getStep(),
      errors: LoginStore.getErrors(),
      isCodeRequested: LoginStore.isCodeRequested(),
      isCodeSended: LoginStore.isCodeSended(),
      isSignupStarted: LoginStore.isSignupStarted()
    }
  };

  componentWillMount() {
    const { query } = this.props;

    ThemeManager.setTheme(ActorTheme);
  }

  componentDidMount() {
    this.handleFocus();
  }

  componentDidUpdate() {
    this.handleFocus();
  }

  // From change handlers
  onLoginChange = event => {
    event.preventDefault();
    LoginActionCreators.changeLogin(event.target.value);
  };
  onCodeChange = event => {
    event.preventDefault();
    LoginActionCreators.changeCode(event.target.value);
  };
  onNameChange = event => {
    event.preventDefault();
    LoginActionCreators.changeName(event.target.value);
  };

  // Form submit handlers
  onRequestCode = event => {
    event.preventDefault();
    LoginActionCreators.requestSms(this.state.login);
  };
  onSendCode = event => {
    event.preventDefault();
    LoginActionCreators.sendCode(this.state.code);
  };
  onSignupRequested = event => {
    event.preventDefault();
    LoginActionCreators.sendSignup(this.state.name);
  };

  handleRestartAuthClick = event => {
    event.preventDefault();
    LoginActionCreators.restartAuth();
  };

  handleFocus = () => {
    const { step } = this.state;

    switch (step) {
      case AuthSteps.LOGIN_WAIT:
        this.refs.login.focus();
        break;
      case AuthSteps.CODE_WAIT:
        this.refs.code.focus();
        break;
      case AuthSteps.NAME_WAIT:
        this.refs.name.focus();
        break;
      default:
    }
  };

  render() {
    const { step, errors, login, code, name, isCodeRequested, isCodeSended, isSignupStarted } = this.state;

    let requestFormClassName = classnames('login__form', 'login__form--request', {
      'login__form--active': step === AuthSteps.LOGIN_WAIT,
      'login__form--done': step !== AuthSteps.LOGIN_WAIT && isCodeRequested
    });
    let checkFormClassName = classnames('login__form', 'login__form--check', {
      'login__form--active': step === AuthSteps.CODE_WAIT && isCodeRequested,
      'login__form--done': step !== AuthSteps.CODE_WAIT && isCodeSended
    });
    let signupFormClassName = classnames('login__form', 'login__form--signup', {
      'login__form--active': step === AuthSteps.NAME_WAIT
    });

    const spinner = (
      <div className="spinner">
        <div/><div/><div/><div/><div/><div/>
        <div/><div/><div/><div/><div/><div/>
      </div>
    );

    return (
      <section className="login-new row center-xs middle-xs">
        <div className="login-new__welcome col-xs row center-xs middle-xs">
          <img alt={`${this.appName} messenger`}
               className="logo"
               src="assets/images/logo.png"
               srcSet="assets/images/logo@2x.png 2x"/>

          <article>
            <h1 className="login-new__heading">
              <FormattedHTMLMessage message={this.getIntlMessage('login.welcome.header')} appName={this.appName}/>
            </h1>
            <FormattedHTMLMessage message={this.getIntlMessage('login.welcome.text')} appName={this.appName}/>
          </article>

          <footer>
            <div className="pull-left">{this.appName} Messenger © 2015</div>
            <div className="pull-right">
              <a href="//actorapp.ghost.io/desktop-apps">Desktop</a>&nbsp;&nbsp;•&nbsp;&nbsp;
              <a href="//actor.im/ios">iPhone</a>&nbsp;&nbsp;•&nbsp;&nbsp;
              <a href="//actor.im/android">Android</a>
            </div>
          </footer>
        </div>

        <div className="login-new__form col-xs-6 col-md-4 row center-xs middle-xs">
          <div>
            <h1 className="login-new__heading">{this.getIntlMessage('login.signIn')}</h1>

            <form className={requestFormClassName} onSubmit={this.onRequestCode}>
              <a className="wrong" onClick={this.handleRestartAuthClick}>{this.getIntlMessage('login.wrong')}</a>
              <TextField className="login__form__input"
                         disabled={isCodeRequested || step !== AuthSteps.LOGIN_WAIT}
                         errorText={errors.login}
                         floatingLabelText={this.getIntlMessage('login.phone')}
                         onChange={this.onLoginChange}
                         ref="login"
                         value={login}/>

              <footer className="text-center">
                <button className="button button--rised button--wide"
                        type="submit"
                        disabled={isCodeRequested}>
                  {this.getIntlMessage('button.requestCode')}
                  {isCodeRequested ? spinner : null}
                </button>
              </footer>
            </form>

            <form className={checkFormClassName} onSubmit={this.onSendCode}>
              <TextField className="login__form__input"
                         disabled={isCodeSended || step !== AuthSteps.CODE_WAIT}
                         errorText={errors.code}
                         floatingLabelText={this.getIntlMessage('login.authCode')}
                         onChange={this.onCodeChange}
                         ref="code"
                         type="text"
                         value={code}/>

              <footer className="text-center">
                <button className="button button--rised button--wide"
                        type="submit"
                        disabled={isCodeSended}>
                  {this.getIntlMessage('button.checkCode')}
                  {isCodeSended ? spinner : null}
                </button>
              </footer>
            </form>

            <form className={signupFormClassName} onSubmit={this.onSignupRequested}>
              <TextField className="login__form__input"
                         disabled={isSignupStarted || step === AuthSteps.COMPLETED}
                         errorText={errors.signup}
                         floatingLabelText={this.getIntlMessage('login.yourName')}
                         onChange={this.onNameChange}
                         ref="name"
                         type="text"
                         value={name}/>

              <footer className="text-center">
                <button className="button button--rised button--wide"
                        type="submit"
                        disabled={isSignupStarted}>
                  {this.getIntlMessage('button.signUp')}
                  {isSignupStarted ? spinner : null}
                </button>
              </footer>
            </form>
          </div>
        </div>
      </section>
    );
  }
}

ReactMixin.onClass(Login, IntlMixin);

export default Container.create(Login, {pure: false});
