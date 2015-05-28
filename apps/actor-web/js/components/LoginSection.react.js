var assign = require('object-assign');

var React = require('react');

var LoginActionCreators = require('../actions/LoginActionCreators');
var LoginStore = require('../stores/LoginStore');

var getStateFromStores = function() {
  return({
    smsRequested: LoginStore.isSmsRequested(),
    phone: '',

    codeSent: false,
    code: '',

    name: ''
  })
};

var LoginSection = React.createClass({
  getInitialState: function() {
    return(assign({
      phone: '',
      code: '',
      name: ''
    }, getStateFromStores()))
  },

  componentWillMount: function() {
    LoginStore.addChangeListener(this._onChange);
  },

  componentWillUnmount: function() {
    LoginStore.removeChangeListener(this._onChange);
  },

  render: function() {
    return (
      <div className="login__window">
        <h2>Sign in to Actor messenger</h2>
        <p>Please enter your full <strong>phone</strong> number to receive <strong>authorization code</strong>.</p>
        <form className="login__form login__form--request" onSubmit={this._onRequestSms}>
          <input type="phone" name="phone" placeholder="Phone number"
                 onChange={this._onPhoneChange}
                 disabled={this.state.smsRequested}/>
          <button className="button button--primary button--wide">Request code</button>
        </form>
        <form className="login__form login__form--check" onSubmit={this._onSendCode}>
          <input type="number" name="code" placeholder="Auth code"
                 onChange={this._onCodeChange}
                 disabled={!this.state.smsRequested || this.state.codeSent}/>
          <button className="button button--primary button--wide">Validate code</button>
          <a href="#">Wrong number?</a>
        </form>
        <form className="login__form login__form--signup">
          <input type="text" name="name" placeholder="Name"/>
          <button className="button button--primary button--wide">Sign up</button>
        </form>
      </div>
    );
  },

  _onChange: function() {
    this.setState(getStateFromStores());
  },

  _onPhoneChange: function(event) {
    this.setState({phone: event.target.value});
  },

  _onCodeChange: function(event) {
    this.setState({code: event.target.value});
  },

  _onRequestSms: function(event) {
    event.preventDefault();
    LoginActionCreators.requestSms(this.state.phone);
  },

  _onSendCode: function(event) {
    event.preventDefault();
    LoginActionCreators.sendCode(this.state.code);
  }
});

module.exports = LoginSection;
