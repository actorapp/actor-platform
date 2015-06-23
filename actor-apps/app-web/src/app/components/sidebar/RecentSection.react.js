import React from 'react';
import _ from 'lodash';

import DialogActionCreators from '../../actions/DialogActionCreators';
import DialogStore from '../../stores/DialogStore';

import CreateGroupActionCreators from '../../actions/CreateGroupActionCreators';

import RecentSectionItem from './RecentSectionItem.react';

const LoadDialogsScrollBottom = 100;

let getStateFromStore = () => {
  return {
    dialogs: DialogStore.getAll()
  };
};

class RecentSection extends React.Component {
  componentWillMount() {
    DialogStore.addChangeListener(this.onChange);
    DialogStore.addSelectListener(this.onChange);
  }

  componentWillUnmount() {
    DialogStore.removeChangeListener(this.onChange);
    DialogStore.removeSelectListener(this.onChange);
  }

  constructor() {
    super();

    this.onChange = this.onChange.bind(this);
    this.onScroll = this.onScroll.bind(this);
    this.openCreateGroup = this.openCreateGroup.bind(this);

    this.state = getStateFromStore();
  }

  onChange() {
    this.setState(getStateFromStore());
  }

  openCreateGroup() {
    CreateGroupActionCreators.openModal();
  }

  onScroll(event) {
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
      <section className="sidebar__recent">
        <ul className="sidebar__list sidebar__list--recent" onScroll={this.onScroll}>
          {dialogs}
        </ul>
        <footer>
          <a className="button button--blue button--wide" onClick={this.openCreateGroup}>
            <i className="material-icons">group_add</i> Create group
          </a>
        </footer>
      </section>
    );
  }
}

export default RecentSection;
