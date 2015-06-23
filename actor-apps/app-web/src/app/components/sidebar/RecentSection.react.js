import React from 'react';
import _ from 'lodash';

import DialogActionCreators from '../../actions/DialogActionCreators';
import DialogStore from '../../stores/DialogStore';

import RecentSectionItem from './RecentSectionItem.react';

const LoadDialogsScrollBottom = 100;

let getStateFromStore = () => {
  return {
    dialogs: DialogStore.getAll()
  };
};

class RecentSection extends React.Component {
  componentWillMount() {
    DialogStore.addChangeListener(this._onChange);
    DialogStore.addSelectListener(this._onChange);
  }

  componentWillUnmount() {
    DialogStore.removeChangeListener(this._onChange);
    DialogStore.removeSelectListener(this._onChange);
  }

  constructor() {
    super();

    this._onChange = this._onChange.bind(this);
    this._onScroll = this._onScroll.bind(this);

    this.state = getStateFromStore();
  }

  _onChange() {
    this.setState(getStateFromStore());
  }

  _onScroll(event) {
    if (event.target.scrollHeight - event.target.scrollTop - event.target.clientHeight <= LoadDialogsScrollBottom) {
      DialogActionCreators.onDialogsEnd();
    }
  }

  render() {
    let dialogs = _.map(this.state.dialogs, (dialog, index) => {
      return (
        <RecentSectionItem dialog={dialog} key={index}/>
      );
    }, this);

    return (
      <ul className="sidebar__list sidebar__list--absolute" onScroll={this._onScroll}>
        {dialogs}
      </ul>
    );
  }
}

export default RecentSection;
