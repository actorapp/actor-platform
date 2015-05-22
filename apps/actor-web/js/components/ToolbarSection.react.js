var React = require('react');

var ToolbarSection = React.createClass({
  render: function() {
    return (
      <header className="toolbar">
        <div className="toolbar__peer">
          <div className="toolbar__peer__avatar avatar avatar--small">
            <span className="avatar__placeholder avatar__placeholder--yellow">С</span>
          </div>
          <div className="toolbar__peer__body">
            <span className="toolbar__peer__title">Степан Коршаков</span>
            <span className="toolbar__peer__presence">last seen yesterday at 17:00</span>
          </div>
        </div>
      </header>
    )
  }
});

module.exports = ToolbarSection;
