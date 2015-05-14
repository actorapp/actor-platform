/** @jsx React.DOM */

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
                <div className="messages-list__item">
                  <Avatar sender={message.sender}/>
                  <Message message={message}/>
                </div>
              );
            })
          }
        </div>
      );
    }
  });
}]);

