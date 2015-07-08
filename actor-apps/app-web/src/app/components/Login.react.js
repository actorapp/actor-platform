import _ from 'lodash';

import React from 'react';
import classNames from 'classnames';
import { Styles, RaisedButton, TextField } from 'material-ui';

import { AuthSteps } from '../constants/ActorAppConstants';

import LoginActionCreators from '../actions/LoginActionCreators';
import LoginStore from '../stores/LoginStore';

import ActorTheme from '../constants/ActorTheme';

const ThemeManager = new Styles.ThemeManager();

let getStateFromStores = function () {
  return ({
    step: LoginStore.getStep(),
    errors: LoginStore.getErrors(),
    smsRequested: LoginStore.isSmsRequested(),
    signupStarted: LoginStore.isSignupStarted(),
    codeSent: false
  });
};

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

  componentWillMount() {
    ThemeManager.setTheme(ActorTheme);

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
      name: '',
      code: ''
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
              <a href="//actor.im/ios">iPhone</a>
              <a href="//actor.im/android">Android</a>
            </div>
          </footer>
        </div>

        <div className="login-new__form col-xs-6 col-md-4 row center-xs middle-xs">
          <div>
            <h1 className="login-new__heading">Sign in</h1>
            <form className={requestFormClassName} onSubmit={this.onRequestSms}>
              <TextField className="login__form__input"
                         disabled={this.state.step > AuthSteps.PHONE_WAIT}
                         errorText={this.state.errors.phone}
                         floatingLabelText="Phone number"
                         onChange={this.onPhoneChange}
                         tabindex="1"
                         type="tel"
                         value={this.state.phone}/>

              <footer className="text-center">
                <RaisedButton label="Request code" type="submit"/>
              </footer>
            </form>
            <form className={checkFormClassName} onSubmit={this.onSendCode}>
              <TextField className="login__form__input"
                         disabled={this.state.step > AuthSteps.CODE_WAIT}
                         errorText={this.state.errors.code}
                         floatingLabelText="Auth code"
                         onChange={this.onCodeChange}
                         type="text"
                         value={this.state.code}/>

              <footer className="text-center">
                <RaisedButton label="Check code" type="submit"/>
              </footer>
            </form>
            <form className={signupFormClassName} onSubmit={this.onSignupRequested}>
              <TextField className="login__form__input"
                         errorText={this.state.errors.signup}
                         floatingLabelText="Your name"
                         onChange={this.onNameChange}
                         type="text"
                         value={this.state.name}/>

              <footer className="text-center">
                <RaisedButton label="Sign up" type="submit"/>
              </footer>
            </form>
          </div>
        </div>
      </section>
    );
  }
}

export default Login;
