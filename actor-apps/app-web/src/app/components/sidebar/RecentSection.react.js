/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import _ from 'lodash';

import React from 'react';
import { Styles, RaisedButton } from 'material-ui';
import ActorTheme from 'constants/ActorTheme';

import DialogActionCreators from 'actions/DialogActionCreators';
import CreateGroupActionCreators from 'actions/CreateGroupActionCreators';

import DialogStore from 'stores/DialogStore';
import CreateGroupStore from 'stores/CreateGroupStore';

import RecentSectionItem from './RecentSectionItem.react';
import CreateGroupModal from 'components/modals/CreateGroup.react';

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

    ThemeManager.setTheme(ActorTheme);

    DialogStore.addChangeListener(this.onChange);
    CreateGroupStore.addChangeListener(this.onChange);
  }

  componentWillUnmount() {
    DialogStore.removeChangeListener(this.onChange);
    CreateGroupStore.removeChangeListener(this.onChange);
  }

  onChange = () => {
    this.setState(getStateFromStore());
  };

  openCreateGroup = () => {
    CreateGroupActionCreators.openModal();
  };

  onScroll = event => {
    const { scrollHeight, scrollTop, clientHeight } = event.target;

    if (scrollHeight - scrollTop - clientHeight <= LoadDialogsScrollBottom) {
      DialogActionCreators.onDialogsEnd();
    }
  };

  render() {
    const { dialogs, isCreateGroupModalOpen } = this.state;

    const dialogList = _.map(dialogs, (dialog, index) => {
      return (
        <RecentSectionItem dialog={dialog} key={index}/>
      );
    }, this);
    const createGroupModal = isCreateGroupModalOpen ? <CreateGroupModal/> : null;

    return (
      <section className="sidebar__recent">
        <ul className="sidebar__list sidebar__list--recent" onScroll={this.onScroll}>
          {dialogList}
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
