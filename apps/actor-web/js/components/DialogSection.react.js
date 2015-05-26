var React = require('react');

var MessagesSection = require('./dialog/MessagesSection.react');

var DialogSection = React.createClass({
  render: function() {
    return(
      <section className="messages">
        <MessagesSection></MessagesSection>
      </section>
    )
  }
});

module.exports = DialogSection;
