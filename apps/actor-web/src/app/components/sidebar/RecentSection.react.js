import React from 'react';
import _ from 'lodash';

import DialogActionCreators from '../../actions/DialogActionCreators';
import DialogStore from '../../stores/DialogStore';

import RecentSectionItem from './RecentSectionItem.react';

const LoadDialogsScrollBottom = 100;

var getStateFromStore = function() {
  return {
    dialogs: DialogStore.getAll()
  };
};

export default React.createClass({
  getInitialState: function() {
    return getStateFromStore();
  },

  componentWillMount: function() {
    DialogStore.addChangeListener(this._onChange);
    DialogStore.addSelectListener(this._onChange);
  },

  componentWillUnmount: function() {
    DialogStore.removeChangeListener(this._onChange);
    DialogStore.removeSelectListener(this._onChange);
  },

  render: function() {
    var dialogs = _.map(this.state.dialogs, function(dialog, index) {
      return (
        <RecentSectionItem key={index} dialog={dialog}/>
      );
    }, this);

    return (
      <ul className="sidebar__list" onScroll={this._onScroll}>
        {dialogs}
      </ul>
    );
  },

  _onChange: function() {
    this.setState(getStateFromStore());
  },

  _onScroll: function(event) {
    if (event.target.scrollHeight - event.target.scrollTop - event.target.clientHeight <= LoadDialogsScrollBottom) {
      DialogActionCreators.onDialogsEnd();
    }
  }
});
