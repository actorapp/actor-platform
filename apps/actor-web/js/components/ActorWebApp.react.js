var React = require('react');

var ActorWebApp = React.createClass({
  propTypes: {
    messenger : React.PropTypes.object.isRequired
  },

  render: function() {
    var body;

    if (this.props.messenger.getUid()) {
      body = <div>the app</div>
    } else {
      body = <div>login form</div>
    }

    return(
      <div>
        {body}
      </div>
    )
  }
});

module.exports = ActorWebApp;
