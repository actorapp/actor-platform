var ActorClient = require('../utils/ActorClient');

var React = require('react');
var AvatarItem = require('./common/AvatarItem.react');

var ToolbarSection = React.createClass({
  getInitialState: function() {
    return({peer: null});
  },

  componentWillMount: function() {
    window.messenger.bindGroup(2043271556, this._setPeer);
  },

  render: function() {
    var peer = this.state.peer;

    return (
      <header className="toolbar">
        <div className="toolbar__peer">
          <AvatarItem title={peer.name} image={peer.avatar} placeholder={peer.placeholder} size="small"/>
          <div className="toolbar__peer__body">
            <span className="toolbar__peer__title">{peer.name}</span>
            <span className="toolbar__peer__presence">{peer.presence}</span>
          </div>
        </div>
      </header>
    )
  },

  _setPeer: function(peer) {
    this.setState({peer: peer})
  }
});

module.exports = ToolbarSection;
