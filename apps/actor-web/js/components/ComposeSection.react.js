var React = require('react');

var ComposeSection = React.createClass({
  render: function() {
    return (
      <section className="compose">
        <textarea className="compose__message"></textarea>
        <footer className="compose__footer row">
          <button className="button">
            <img src="assets/img/icons/ic_attachment_24px.svg" alt=""/> Send file
          </button>
          <button className="button">
            <img src="assets/img/icons/ic_photo_camera_24px.svg" alt=""/> Send photo
          </button>
          <span className="col-xs"></span>
          <button className="button button--primary">
            Send
          </button>
        </footer>
      </section>
    )
  }
});

module.exports = ComposeSection;
