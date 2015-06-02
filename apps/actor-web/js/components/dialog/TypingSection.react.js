'use strict';

var React = require('react');

var TypingSection = React.createClass({
  propTypes: {
    typing: React.PropTypes.object.isRequired
  },

  render: function() {
    return (
      <div className="typing">
        <i className="material-icons">keyboard</i>Someone typing
      </div>
    );
  }
});

module.exports = TypingSection;
