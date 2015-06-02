var ActorClient = require('../utils/ActorClient');

var React = require('react');
var AvatarItem = require('./common/AvatarItem.react');

var DialogStore = require('../stores/DialogStore');

var ActorAppConstants = require('../constants/ActorAppConstants');
var ProfileActionCreators = require('../actions/ProfileActionCreators');

var ToolbarSection = React.createClass({
  getInitialState: function() {
    return({dialogInfo: null})
  },

  componentWillMount: function() {
    DialogStore.addSelectedChangeListener(this._onChange);
  },

  componentWillUnmount: function() {
    DialogStore.removeSelectedChangeListener(this._onChange);
  },

  render: function() {
    var info = this.state.dialogInfo;
    var dialogElement;

    if (info != null) {
      dialogElement =
        <div className="toolbar__peer row">
          <a onClick={this._onClick}>
            <AvatarItem title={info.name}
                        image={info.avatar}
                        placeholder={info.placeholder}
                        size="small"/>
          </a>
          <div className="toolbar__peer__body col-xs">
            <span className="toolbar__peer__title" onClick={this._onClick}>{info.name}</span>
            <span className="toolbar__peer__presence">{info.presence}</span>
          </div>
        </div>
    } else {
      dialogElement = null;
    }

    return(
      <header className="toolbar">
        {dialogElement}
      </header>
    );
  },

  _onClick: function() {
    var peer = this.state.dialogInfo;

    if (typeof peer.adminId == "undefined") {
      ProfileActionCreators.clickUser(peer.id);
    } else {
      ProfileActionCreators.clickGroup(peer.id);
    }
  },

  _onChange: function() {
    this.setState({dialogInfo: DialogStore.getSelectedDialogInfo()});
  }
});

module.exports = ToolbarSection;
