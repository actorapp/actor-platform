import React from 'react';

import LoginActionCreators from '../actions/LoginActionCreators';
import LoginStore from '../stores/LoginStore';

//import classNames from 'classnames';

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
    //let requestFormClassName = classNames('login__form', 'login__form--request', {
    //  'login__form--done': this.state.smsRequested,
    //  'login__form--active': !this.state.smsRequested && !this.state.signupStarted
    //});
    //let checkFormClassName = classNames('login__form', 'login__form--check', {
    //  'login__form--done': this.state.signupStarted,
    //  'login__form--active': this.state.smsRequested && !this.state.signupStarted
    //});
    //let signupFormClassName = classNames('login__form', 'login__form--signup', {
    //  'login__form--active': this.state.signupStarted
    //});
    //
    //let stepMesssageText = <p>Please enter your full <strong>phone</strong> number to receive <strong>authorization
    //  code</strong>.</p>;
    //let smsRequested = this.state.smsRequested;
    //let signupStarted = this.state.signupStarted;
    //
    //if (smsRequested) {
    //  stepMesssageText =
    //    <p>We sent <strong>authorization code</strong> to your <strong>phone</strong>. Please enter it below.</p>;
    //}
    //if (signupStarted) {
    //  stepMesssageText =
    //    <p>To complete your <strong>registration</strong>, please enter your <strong>name</strong>.</p>;
    //}


    //return (
    //  <div className="login row center-xs middle-xs">
    //    <div className="login__window">
    //      <h2>Sign in to Actor messenger</h2>
    //      {stepMesssageText}
    //      <form className={requestFormClassName} onSubmit={this.onRequestSms}>
    //        <a href="#">Wrong?</a>
    //        <input disabled={this.state.smsRequested}
    //               name="phone"
    //               onChange={this.onPhoneChange}
    //               placeholder="Phone number"
    //               type="phone" />
    //        <button className="button button--primary button--wide">Request code</button>
    //      </form>
    //      <form className={checkFormClassName} onSubmit={this.onSendCode}>
    //        <input disabled={!this.state.smsRequested || this.state.signupStarted}
    //               name="code"
    //               onChange={this.onCodeChange}
    //               placeholder="Auth code"
    //               type="number"/>
    //        <button className="button button--primary button--wide">Validate code</button>
    //      </form>
    //      <form className={signupFormClassName} onSubmit={this.onSignupRequested}>
    //        <input name="name"
    //               onChange={this.onNameChange}
    //               placeholder="Name"
    //               type="text" />
    //        <button className="button button--primary button--wide">Sign up</button>
    //      </form>
    //    </div>
    //  </div>
    //);
    return (
      <section className="login-new row center-xs middle-xs">
        <div className="login-new__welcome col-xs row center-xs middle-xs">
          <img alt="Acor messenger" className="logo" src="/assets/img/logo.png"/>

          <article>
            <h1 className="login-new__heading">Добро пожаловать в <strong>Actor Web</strong></h1>
            <p>
              Здесь можно разместить небольшое описание всего того, что здесь находится.
              Всего несколько строк, побуждающих к действию, заставляющих использовать наш мессенджер и описывающий все
              его возможности. Люди любят, когда с ними говорят. Давайте говорить с людьми тоже.
            </p>
            <a href="#" className="button button--blue">Рассказать друзьям</a>
          </article>

          <footer>
            <div className="pull-left">
              Actor Messenger © 2015
            </div>
            <div className="pull-right">
              <a href="#">iPhone</a>
              <a href="https://play.google.com/store/apps/details?id=im.actor.cloud">Android</a>
            </div>
          </footer>
        </div>
        <div className="login-new__form col-xs-6 col-md-4 row center-xs middle-xs">
          <form action="#">
            <h1 className="login-new__heading">Вход</h1>
            <fieldset>
              <input disabled={this.state.smsRequested}
                     name="phone"
                     onChange={this.onPhoneChange}
                     placeholder="Введите номер"
                     type="phone"/>
              <input disabled={this.state.smsRequested}
                     name="code"
                     onChange={this.onPhoneChange}
                     placeholder="Введите код"
                     type="code"/>
            </fieldset>
            <footer className="text-right">
              <button className="button button--light">Регистрация</button>
              <button className="button button--blue">Вход</button>
            </footer>
          </form>
        </div>
      </section>
    );
  }
}

Login.contextTypes = {
  router: React.PropTypes.func
};

export default Login;
