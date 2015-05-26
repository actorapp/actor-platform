var ActorClient = require('../utils/ActorClient');

var React = require('react');
var AvatarItem = require('./common/AvatarItem.react');

var DialogStore = require('../stores/DialogStore');

var getStateFromStore = function() {
  return({
    dialog: DialogStore.getSelectedDialog()
  });
};

var ToolbarSection = React.createClass({
  getInitialState: function() {
    return(getStateFromStore());
  },

  componentWillMount: function() {
    DialogStore.addSelectListener(this._onChange);
  },

  componentWillUnmount: function() {
    DialogStore.removeChangeListener(this._onChange);
  },

  render: function() {
    var dialog = this.state.dialog;
    window.t = this;
    var dialogElement;

    if (dialog != null) {
      dialogElement =
        <div className="toolbar__peer">
          <AvatarItem title={dialog.peer.title} image={dialog.peer.avatar} placeholder={dialog.peer.placeholder} size="small"/>

          <div className="toolbar__peer__body">
            <span className="toolbar__peer__title">{dialog.peer.title}</span>
            <span className="toolbar__peer__presence">{dialog.peer.presence}</span>
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

  _onChange: function() {
    this.setState(getStateFromStore());
  }
});

module.exports = ToolbarSection;
