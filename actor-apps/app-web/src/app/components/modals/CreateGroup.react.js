import React from 'react';

import CreateGroupActionCreators from '../../actions/CreateGroupActionCreators';
import CreateGroupStore from '../../stores/CreateGroupStore';

import CreateGroupForm from './create-group/Form.react';

import Modal from 'react-modal';

import { KeyCodes } from '../../constants/ActorAppConstants';

const appElement = document.getElementById('actor-web-app');
Modal.setAppElement(appElement);

const getStateFromStores = () => {
  return {
    isShown: CreateGroupStore.isModalOpen()
  };
};

class CreateGroup extends React.Component {
  constructor(props) {
    super(props);

    this.state = getStateFromStores();

    CreateGroupStore.addChangeListener(this.onChange);
    document.addEventListener('keydown', this.onKeyDown, false);
  }

  componentWillUnmount() {
    CreateGroupStore.removeChangeListener(this.onChange);
    document.removeEventListener('keydown', this.onKeyDown, false);
  }

  render() {
    const isShown = this.state.isShown;

    return (
      <Modal className="modal-new modal-new--create-group" closeTimeoutMS={150} isOpen={isShown}>

        <header className="modal-new__header">
          <a className="modal-new__header__close material-icons" onClick={this.onClose}>clear</a>
          <h3 className="modal-new__header__title">Create group</h3>
        </header>

        <div className="modal-new__body">
          <CreateGroupForm/>
        </div>

      </Modal>
    );
  }

  onChange = () => {
    this.setState(getStateFromStores());
  }

  onClose = () => {
    CreateGroupActionCreators.closeModal();
  }

  onKeyDown = (event) => {
    if (event.keyCode === KeyCodes.ESC) {
      event.preventDefault();
      this.onClose();
    }
  }
}

CreateGroup.displayName = 'CreateGroup';


export default CreateGroup;
