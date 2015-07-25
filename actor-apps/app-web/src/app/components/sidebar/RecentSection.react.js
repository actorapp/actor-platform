import _ from 'lodash';

import React from 'react';
import { Styles, RaisedButton } from 'material-ui';
import ActorTheme from 'constants/ActorTheme';

import DialogActionCreators from 'actions/DialogActionCreators';
import DialogStore from 'stores/DialogStore';

import CreateGroupActionCreators from 'actions/CreateGroupActionCreators';

import RecentSectionItem from './RecentSectionItem.react';
import CreateGroupModal from 'components/modals/CreateGroup.react';
import CreateGroupStore from 'stores/CreateGroupStore';

const ThemeManager = new Styles.ThemeManager();
const LoadDialogsScrollBottom = 100;

const getStateFromStore = () => {
  return {
    isCreateGroupModalOpen: CreateGroupStore.isModalOpen(),
    dialogs: DialogStore.getAll()
  };
};

class RecentSection extends React.Component {
  static childContextTypes = {
    muiTheme: React.PropTypes.object
  };

  getChildContext() {
    return {
      muiTheme: ThemeManager.getCurrentTheme()
    };
  }

  constructor(props) {
    super(props);

    this.state = getStateFromStore();

    DialogStore.addChangeListener(this.onChange);
    DialogStore.addSelectListener(this.onChange);
    CreateGroupStore.addChangeListener(this.onChange);
    ThemeManager.setTheme(ActorTheme);
  }

  componentWillUnmount() {
    DialogStore.removeChangeListener(this.onChange);
    DialogStore.removeSelectListener(this.onChange);
    CreateGroupStore.removeChangeListener(this.onChange);
  }

  onChange = () => {
    this.setState(getStateFromStore());
  }

  openCreateGroup = () => {
    CreateGroupActionCreators.openModal();
  }

  onScroll = event => {
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

    let createGroupModal;
    if (this.state.isCreateGroupModalOpen) {
      createGroupModal = <CreateGroupModal/>;
    }

    return (
      <section className="sidebar__recent">
        <ul className="sidebar__list sidebar__list--recent" onScroll={this.onScroll}>
          {dialogs}
        </ul>
        <footer>
          <RaisedButton label="Create group" onClick={this.openCreateGroup} style={{width: '100%'}}/>
          {createGroupModal}
        </footer>
      </section>
    );
  }
}

export default RecentSection;
