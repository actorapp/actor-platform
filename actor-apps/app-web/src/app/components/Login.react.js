import _ from 'lodash';

import React from 'react';
import classNames from 'classnames';
import { Styles, TextField } from 'material-ui';

import { AuthSteps } from 'constants/ActorAppConstants';

import Banner from 'components/common/Banner.react';

import LoginActionCreators from 'actions/LoginActionCreators';
import LoginStore from 'stores/LoginStore';

import ActorTheme from 'constants/ActorTheme';

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

  onChange = () => {
    this.setState(getStateFromStores());
  }

  onPhoneChange = event => {
    this.setState({phone: event.target.value});
  }

  onCodeChange = event => {
    this.setState({code: event.target.value});
  }

  onNameChange = event => {
    this.setState({name: event.target.value});
  }

  onRequestSms = event => {
    event.preventDefault();
    LoginActionCreators.requestSms(this.state.phone);
  }

  onSendCode = event => {
    event.preventDefault();
    LoginActionCreators.sendCode(this.context.router, this.state.code);
  }

  onSignupRequested = event => {
    event.preventDefault();
    LoginActionCreators.sendSignup(this.context.router, this.state.name);
  }

  onWrongNumberClick = event => {
    event.preventDefault();
    LoginActionCreators.wrongNumberClick();
  }


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
        <Banner/>

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
              <a className="wrong" onClick={this.onWrongNumberClick}>Wrong?</a>
              <TextField className="login__form__input"
                         disabled={this.state.step > AuthSteps.PHONE_WAIT}
                         errorText={this.state.errors.phone}
                         floatingLabelText="Phone number"
                         onChange={this.onPhoneChange}
                         ref="phone"
                         type="text"
                         value={this.state.phone}/>

              <footer className="text-center">
                <button className="button button--rised" type="submit">Request code</button>
              </footer>
            </form>
            <form className={checkFormClassName} onSubmit={this.onSendCode}>
              <TextField className="login__form__input"
                         disabled={this.state.step > AuthSteps.CODE_WAIT}
                         errorText={this.state.errors.code}
                         floatingLabelText="Auth code"
                         onChange={this.onCodeChange}
                         ref="code"
                         type="text"
                         value={this.state.code}/>

              <footer className="text-center">
                <button className="button button--rised" type="submit">Check code</button>
              </footer>
            </form>
            <form className={signupFormClassName} onSubmit={this.onSignupRequested}>
              <TextField className="login__form__input"
                         errorText={this.state.errors.signup}
                         floatingLabelText="Your name"
                         onChange={this.onNameChange}
                         ref="name"
                         type="text"
                         value={this.state.name}/>

              <footer className="text-center">
                <button className="button button--rised" type="submit">Sign up</button>
              </footer>
            </form>
          </div>
        </div>
      </section>
    );
  }
}

export default Login;
