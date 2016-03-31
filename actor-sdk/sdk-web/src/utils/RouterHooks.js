import LoginStore from '../stores/LoginStore';
import DialogActionCreators from '../actions/DialogActionCreators';
import PeerUtils from './PeerUtils';

const RouterHooks = {
  requireAuth(nextState, replaceState) {
    if (!LoginStore.isLoggedIn()) {
      replaceState({
        pathname: '/auth',
        state: {
          nextPathname: nextState.location.pathname
        }
      });
    }
  }
};

export default RouterHooks;
