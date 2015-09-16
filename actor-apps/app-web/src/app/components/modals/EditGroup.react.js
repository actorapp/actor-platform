/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import Modal from 'react-modal';

import { KeyCodes } from 'constants/ActorAppConstants';

import EditGroupStore from 'stores/EditGroupStore';

import EditGroupActionCreators from 'actions/EditGroupActionCreators';

import AvatarItem from 'components/common/AvatarItem.react';

const getStateFromStores = () => {
  return {
    isOpen: EditGroupStore.isOpen(),
    group: EditGroupStore.getGroup(),
    title: EditGroupStore.getTitle()
  }
};

class EditGroup extends Component {
  constructor(props) {
    super(props);

    this.state = getStateFromStores();

    EditGroupStore.addChangeListener(this.onChange);
  }

  componentWillUnmount() {
    EditGroupStore.removeChangeListener(this.onChange);
  }

  componentWillUpdate(nextProps, nextState) {
    if (nextState.isOpen && !this.state.isOpen) {
      document.addEventListener('keydown', this.onKeyDown, false);
    } else if (!nextState.isOpen && this.state.isOpen) {
      document.removeEventListener('keydown', this.onKeyDown, false);
    }
  }

  onClose = () => EditGroupActionCreators.hide();
  onChange = () => this.setState(getStateFromStores());
  onTitleChange = event => this.setState({title: event.target.value});

  onKeyDown = event => {
    if (event.keyCode === KeyCodes.ESC) {
      event.preventDefault();
      this.onClose();
    }
  };

  onSave = () => {
    const { group, title } = this.state;
    EditGroupActionCreators.editGroupTitle(group.id, title);
    this.onClose();
  };

  render() {
    const { isOpen, group, title } = this.state;

    if (isOpen) {
      return (
        <Modal className="modal-new modal-new--edit-group"
               closeTimeoutMS={150}
               isOpen={isOpen}
               style={{width: 300}}>

          <header className="modal-new__header">
            <a className="modal-new__header__icon material-icons">edit</a>
            <h4 className="modal-new__header__title">
              Edit group
            </h4>

            <div className="pull-right">
              <button className="button button--lightblue" onClick={this.onSave}>Done</button>
            </div>
          </header>

          <div className="modal-new__body row">
            <AvatarItem image={group.bigAvatar}
                        placeholder={group.placeholder}
                        size="large"
                        title={group.name}/>

            <div className="col-xs">
              <label htmlFor="title">Group title</label>
              <input className="input"
                     id="title"
                     type="text"
                     value={title}
                     onChange={this.onTitleChange}/>
            </div>
          </div>

        </Modal>
      );
    } else {
      return null;
    }
  }
}

export default EditGroup;
