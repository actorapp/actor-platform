import React from 'react';
import classnames from 'classnames';
import ConnectionStateStore from 'stores/ConnectionStateStore';

const getStateFromStore = () => {
  return {
    connectionState: ConnectionStateStore.getState()
  };
};

class ConnectionState extends React.Component {
  constructor(props) {
    super(props);

    this.state = getStateFromStore();

    ConnectionStateStore.addChangeListener(this.onStateChange);
  }

  componentWillUnmount() {
    ConnectionStateStore.removeChangeListener(this.onStateChange);
  }

  onStateChange = () => {
    this.setState(getStateFromStore);
  };

  render() {
    const { connectionState } = this.state;

    const className = classnames('connection-state', {
      'connection-state--online': connectionState === 'online',
      'connection-state--connection': connectionState === 'connecting'
    });

    switch (connectionState) {
      case 'online':
        return (
          <div className={className}>'You're back online!'</div>
        );
      case 'connecting':
        return (
          <div className={className}>
            Houston, we have a problem! Connection to Actor server is lost. Trying to reconnect now...
          </div>
        );
      default:
        return null;
    }
  }
}

export default ConnectionState;
