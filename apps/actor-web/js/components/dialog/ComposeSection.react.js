var React = require('react');
var PureRenderMixin = require('react/addons').addons.PureRenderMixin;

var MessageActionCreators = require('../../actions/MessageActionCreators');

var ENTER_KEY_CODE = 13;

var ComposeSection = React.createClass({
  mixins: [PureRenderMixin],

  propTypes: {
    dialog: React.PropTypes.object.isRequired
  },

  getInitialState: function() {
    return({
      text: ''
    });
  },

  render: function() {
    return (
      <section className="compose">
        <textarea className="compose__message" value={this.state.text} onChange={this._onChange} onKeyDown={this._onKeyDown}></textarea>
        <footer className="compose__footer row">
          <button className="button" onClick={this._onSendFileClick}>
            <img src="assets/img/icons/ic_attachment_24px.svg" alt=""/> Send file
          </button>
          <button className="button" onClick={this._onSendPhotoClick}>
            <img src="assets/img/icons/ic_photo_camera_24px.svg" alt=""/> Send photo
          </button>
          <span className="col-xs"></span>
          <button className="button button--primary">Send</button>
        </footer>

        <div className="compose__hidden">
          <input type="file"
                 id="composeFileInput"
                 onChange={this._onFileInputChange}/>
          <input type="file"
                 id="composePhotoInput"
                 onChange={this._onPhotoInputChange}/>
        </div>
      </section>
    )
  },

  _onChange: function(event, value) {
    this.setState({text: event.target.value});
  },

  _onKeyDown: function(event) {
    if (event.keyCode === ENTER_KEY_CODE) {
      event.preventDefault();
      var text = this.state.text;
      if (text) {
        MessageActionCreators.sendTextMessage(this.props.dialog, text);
      }
      this.setState({text: ''});
    }
  },

  _onSendFileClick: function() {
    var fileInput = document.getElementById('composeFileInput');
    fileInput.click();
  },

  _onSendPhotoClick: function() {
    var photoInput = document.getElementById('composePhotoInput');
    photoInput.accept = "image/*";
    photoInput.click();
  },

  _onFileInputChange: function() {
    var files = document.getElementById('composeFileInput').files;
    MessageActionCreators.sendFileMessage(this.props.dialog, files[0]);
  },

  _onPhotoInputChange: function() {
    var photos = document.getElementById('composePhotoInput').files;
    MessageActionCreators.sendPhotoMessage(this.props.dialog, photos[0]);
  }
});

module.exports = ComposeSection;
