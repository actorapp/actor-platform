import React from 'react';

import Modal from 'react-modal';
import AddContactStore from '../../stores/AddContactStore';
import AddContactActionCreators from '../../actions/AddContactActionCreators';
// import { KeyCodes } from '../../constants/ActorAppConstants';

const appElement = document.getElementById('actor-web-app');
Modal.setAppElement(appElement);

const getStateFromStores = () => {
  return {
    isShown: AddContactStore.isModalOpen()
  };
};

class AddContact extends React.Component {
  constructor(props) {
    super(props);

    this.state = getStateFromStores();
  }

  render() {
    let isShown = this.state.isShown;

    return (
      <Modal className="modal-new modal-new--add-contact" closeTimeoutMS={150} isOpen={isShown}>

        <header className="modal-new__header">
          <a className="modal-new__header__close material-icons" onClick={this.onClose}>clear</a>
          <h3 className="modal-new__header__title">Add contact</h3>
        </header>

        <div className="modal-new__body">

        </div>

      </Modal>
    );
  }

  onClose = () => {
    AddContactActionCreators.closeModal();
  }
}

export default AddContact;
