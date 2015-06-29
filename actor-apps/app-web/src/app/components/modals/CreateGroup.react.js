import React from 'react';

import CreateGroupActionCreators from '../../actions/CreateGroupActionCreators';
import CreateGroupStore from '../../stores/CreateGroupStore';

import CreateGroupForm from './create-group/Form.react';

import Modal from 'react-modal';

import { KeyCodes } from '../../constants/ActorAppConstants';

const appElement = document.getElementById('actor-web-app');
Modal.setAppElement(appElement);

let getStateFromStores = () => {
  return {
    isShown: CreateGroupStore.isModalOpen()
  };
};

class CreateGroup extends React.Component {
  componentWillMount() {
    CreateGroupStore.addChangeListener(this.onChange);
    document.addEventListener('keydown', this.onKeyDown, false);
  }

  componentWillUnmount() {
    CreateGroupStore.removeChangeListener(this.onChange);
    document.removeEventListener('keydown', this.onKeyDown, false);
  }

  constructor() {
    super();

    this.onClose = this.onClose.bind(this);
    this.onKeyDown = this.onKeyDown.bind(this);
    this.onChange = this.onChange.bind(this);

    this.state = getStateFromStores();
  }

  render() {
    let isShown = this.state.isShown;

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

  onChange() {
    this.setState(getStateFromStores());
  }

  onClose() {
    CreateGroupActionCreators.closeModal();
  }

  onKeyDown(event) {
    if (event.keyCode === KeyCodes.ESC) {
      console.warn('Esc pressed, close modal');
      event.preventDefault();
      this.onClose();
    } else {
      console.warn(event.keyCode, ' pressed, do nothing');
    }
  }
}

CreateGroup.displayName = 'CreateGroup';


export default CreateGroup;
