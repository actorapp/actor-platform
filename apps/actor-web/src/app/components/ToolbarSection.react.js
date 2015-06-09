import React from 'react';
import AvatarItem from './common/AvatarItem.react';

import DialogStore from '../stores/DialogStore';

import ActivityActionCreators from '../actions/ActivityActionCreators';

export default React.createClass({
  getInitialState: function() {
    return {dialogInfo: null};
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
        </div>;
    } else {
      dialogElement = null;
    }

    return (
      <header className="toolbar">
        {dialogElement}
      </header>
    );
  },

  _onClick: function() {
    ActivityActionCreators.show();
  },

  _onChange: function() {
    this.setState({dialogInfo: DialogStore.getSelectedDialogInfo()});
  }
});
