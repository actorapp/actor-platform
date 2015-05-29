var ActorClient = require('../utils/ActorClient');

var React = require('react');
var AvatarItem = require('./common/AvatarItem.react');

var DialogStore = require('../stores/DialogStore');

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
        <div className="toolbar__peer">
          <AvatarItem title={info.name}
                      image={info.avatar}
                      placeholder={info.placeholder}
                      size="small"/>
          <div className="toolbar__peer__body">
            <span className="toolbar__peer__title">{info.name}</span>
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

  _onChange: function() {
    this.setState({dialogInfo: DialogStore.getSelectedDialogInfo()});
  }
});

module.exports = ToolbarSection;
