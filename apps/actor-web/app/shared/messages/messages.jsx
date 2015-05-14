/** @jsx React.DOM */


var Avatar = React.createClass({
  propTypes: {
    sender : React.PropTypes.object.isRequired
  },
  render: function() {
    var sender = this.props.sender;

    return (
      <div class="messages-list__item__avatar avatar avatar--small avatar--{sender.placeholder}">

        <span>{sender.title[0]}</span>
      </div>
    )
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
                <div class="messages-list__item">
                  <Avatar sender={message.sender}/>
                  <p>{message.content.text}</p>
                </div>
              );
            })
          }
        </div>
      );
    }
  });
}]);


/*
 <div class="messages-list__item__avatar avatar avatar--small avatar--{{ ::message.sender.placeholder }}"
 ng-hide="::message.content.content == 'service'">
 <img ng-src="{{ ::message.sender.avatar }}" alt="{{ ::message.sender.title }}"
 ng-if="::message.sender.avatar"/>
 <span ng-bind="::message.sender.title | limitTo:1"></span>
 </div>
 */