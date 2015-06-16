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
            <a className="button button--blue" href="#">Рассказать друзьям</a>
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
          <div>
            <h1 className="login-new__heading">Вход</h1>
            <form className={requestFormClassName} onSubmit={this.onRequestSms}>
              <input disabled={this.state.step > AuthSteps.PHONE_WAIT}
                     name="phone"
                     onChange={this.onPhoneChange}
                     placeholder="Введите номер"
                     type="phone"/>
              <span>{this.state.errors.phone}</span>
              <footer className="text-center">
                <button className="button button--blue">Запросить код</button>
              </footer>
            </form>
            <form className={checkFormClassName} onSubmit={this.onSendCode}>
              <input disabled={this.state.step > AuthSteps.CODE_WAIT}
                     name="code"
                     onChange={this.onCodeChange}
                     placeholder="Введите код"
                     type="number"
                     value={this.state.code}/>
              <span>{this.state.errors.code}</span>
              <footer className="text-center">
                <button className="button button--blue">Проверить код</button>
              </footer>
            </form>
            <form className={signupFormClassName} onSubmit={this.onSignupRequested}>
              <input name="name"
                     onChange={this.onNameChange}
                     placeholder="Введите имя"
                     type="text"/>
              <span>{this.state.errors.signup}</span>
              <footer className="text-center">
                <button className="button button--blue">Зарегистрироваться</button>
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
