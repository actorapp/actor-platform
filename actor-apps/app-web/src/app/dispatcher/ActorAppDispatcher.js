import { Dispatcher } from 'flux';

const ActorAppDispatcher = new Dispatcher();

ActorAppDispatcher.register(action => {
  console.info(action);
});

export default ActorAppDispatcher;
