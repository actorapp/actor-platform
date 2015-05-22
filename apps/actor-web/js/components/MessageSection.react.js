var React = require('react');

var MessageSection = React.createClass({
  render: function() {
    return(
      <section className="main col-xs">
        <header className="toolbar">
          <div className="toolbar__peer">
            <div className="toolbar__peer__avatar avatar avatar--small">
              <img className="avatar__image"
                   alt="Степан Коршаков"
                   src="https://actor-files-rev2.s3.amazonaws.com/file_-6329121226674808854?AWSAccessKeyId=AKIAI4G6WK7W42PV4YEA&Expires=1432307448&Signature=QbCRHE7BLZ%2BDe%2BOvxGcJko4B2Cg%3D"/>
                <span className="avatar__placeholder avatar__placeholder--yellow">С</span>
            </div>
            <div className="toolbar__peer__body">
              <span className="toolbar__peer__title">Степан Коршаков</span>
              <span className="toolbar__peer__presence">last seen yesterday at 17:00</span>
            </div>
          </div>
        </header>
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
      </section>
    )
  }
});

module.exports = MessageSection;
