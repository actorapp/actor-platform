var React = require('react');

var Avatar = React.createClass({
  propTypes: {
    sender : React.PropTypes.object.isRequired
  },
  render: function() {
    var sender = this.props.sender;
    var image = null;
    if (sender.avatar) {
      image = <img className="avatar__image" src={sender.avatar} alt={sender.title}/>;
    }

    var placeholderClass = 'avatar__placeholder avatar__placeholder--' + sender.placeholder;

    return (
      <div className="avatar avatar--small">
        {image}
        <span className={placeholderClass}>{sender.title[0]}</span>
      </div>
    )
  }
});

module.exports = Avatar;
