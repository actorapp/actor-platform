import React from 'react';

import CreateGroupActionCreators from '../../actions/CreateGroupActionCreators';
import CreateGroupStore from '../../stores/CreateGroupStore';

import CreateGroupForm from './create-group/Form.react';

import Modal from 'react-modal';

let appElement = document.getElementById('actor-web-app');
Modal.setAppElement(appElement);

let getStateFromStores = () => {
  return {
    isShown: CreateGroupStore.isModalOpen()
  };
};

class CreateGroup extends React.Component {
  componentWillMount() {
    CreateGroupStore.addChangeListener(this.onChange);
  }

  componentWillUnmount() {
    CreateGroupStore.removeChangeListener(this.onChange);
  }

  constructor() {
    super();

    this.state = getStateFromStores();

    this.onClose = this.onClose.bind(this);
    this.onChange = this.onChange.bind(this);
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
}

CreateGroup.displayName = 'CreateGroup';


export default CreateGroup;
