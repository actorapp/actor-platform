/** @jsx React.DOM */

var MessageWithAvatar = React.createClass({
  propTypes: {
    message: React.PropTypes.object.isRequired
  },
  render: function() {
    var message = this.props.message;

    return(
      <div className="messages-list__item">
        <Avatar sender={message.sender}/>
        <Message message={message}/>
      </div>
    );
  }
});

angular
  .module('actorWeb')
  .factory('Messages', ['$filter', function($filter) {
  return React.createClass({
    propTypes: {
      messages : React.PropTypes.array.isRequired
    },
    render: function() {
      return (
        <div>
          {
            this.props.messages.map(function(message) {
              return (
                <MessageWithAvatar key={message.sortKey} message={message}/>
              );
            })
          }
        </div>
      );
    }
  });
}]);

