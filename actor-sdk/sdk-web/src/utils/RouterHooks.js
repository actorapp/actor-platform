import LoginStore from '../stores/LoginStore';

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
