package com.droidkit.actors.tasks;

import com.droidkit.actors.Actor;
import com.droidkit.actors.ActorRef;
import com.droidkit.actors.messages.PoisonPill;
import com.droidkit.actors.tasks.messages.*;

import java.util.HashSet;

/**
 * Actor for performing various async tasks
 *
 * @author Stepan Ex3NDR Korshakov (me@ex3ndr.com)
 */
public abstract class TaskActor<T> extends Actor {
    private final HashSet<TaskListener> requests = new HashSet<TaskListener>();

    private T result;
    private boolean isCompleted;
    private boolean isCompletedSuccess;
    private long dieTimeout = 300;

    /**
     * Timeout for dying after task complete
     *
     * @param timeOut timeout in ms
     */
    public void setTimeOut(long timeOut) {
        dieTimeout = timeOut;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public boolean isCompletedSuccess() {
        return isCompletedSuccess;
    }

    @Override
    public void preStart() {
        // Logger.d(getClass().getSimpleName(), getPath() + hashCode() + "# preStart at " + Thread.currentThread().getName());
        self().send(new TaskStart());
    }

    @Override
    public void onReceive(Object message) {
        // Logger.d(getClass().getSimpleName(), getPath() + " Raw On Message " + message);
        if (message instanceof TaskStart) {
            startTask();
        } else if (message instanceof Obsolete) {
            onTaskObsolete();
            context().stopSelf();
        } else if (message instanceof TaskRequest) {
            TaskRequest request = (TaskRequest) message;
            if (isCompleted) {
                if (isCompletedSuccess) {
                    reply(result);
                }
            } else {
                TaskListener listener = new TaskListener(request.getRequestId(), sender());
                requests.add(listener);
                // self().sendOnce(new Obsolete(), 24 * 60 * 60 * 1000L);
            }
        } else if (message instanceof TaskCancel) {
            TaskCancel cancel = (TaskCancel) message;

            if (isCompleted) {
                return;
            }
            TaskListener listener = new TaskListener(cancel.getRequestId(), sender());
            requests.remove(listener);
            if (requests.size() == 0) {
                self().sendOnce(new Obsolete(), dieTimeout);
            }
        } else if (message instanceof Progress) {
            Progress progress = (Progress) message;
            if (!isCompleted) {
                for (TaskListener request : requests) {
                    request.getSender().send(new TaskProgress(request.getRequestId(), progress.progress));
                }
            }
        } else if (message instanceof Result) {
            if (!isCompleted) {
                Result res = (Result) message;
                isCompleted = true;
                isCompletedSuccess = true;
                result = (T) res.getRes();
                for (TaskListener request : requests) {
                    request.getSender().send(new TaskResult<T>(request.getRequestId(), result));
                }
                self().send(PoisonPill.INSTANCE, dieTimeout);
            }
        } else if (message instanceof Error) {
            if (!isCompleted) {
                isCompleted = true;
                Error error = (Error) message;
                for (TaskListener request : requests) {
                    request.getSender().send(new TaskError(request.getRequestId(), error.getError()));
                }
                context().stopSelf();
            }
        }
    }

    @Override
    public void postStop() {
        // Logger.d(getClass().getSimpleName(), getPath() + " Stopped");
    }

    /**
     * Starting of task execution
     */
    public abstract void startTask();

    /**
     * Called before killing actor after clearing TaskListeners
     */
    public void onTaskObsolete() {

    }

    /**
     * Call this method in any thread after task complete
     *
     * @param res result of task
     */
    public void complete(T res) {
        self().send(new Result(res));
    }

    /**
     * Call this method in any thread after task exception
     *
     * @param t exception
     */
    public void error(Throwable t) {
        self().send(new Error(t));
    }

    /**
     * Call this method in any thread for notification about progress
     *
     * @param progress progress object
     */
    public void progress(Object progress) {
        self().send(new Progress(progress));
    }

    private static class Error {
        private Throwable error;

        private Error(Throwable error) {
            this.error = error;
        }

        public Throwable getError() {
            return error;
        }
    }

    private static class Result {
        private Object res;

        private Result(Object res) {
            this.res = res;
        }

        public Object getRes() {
            return res;
        }
    }

    private static class Progress {
        private Object progress;

        private Progress(Object progress) {
            this.progress = progress;
        }

        public Object getProgress() {
            return progress;
        }
    }

    private static class Obsolete {

    }

    private static class TaskListener {
        private int requestId;
        private ActorRef sender;

        private TaskListener(int requestId, ActorRef sender) {
            this.requestId = requestId;
            this.sender = sender;
        }

        public int getRequestId() {
            return requestId;
        }

        public ActorRef getSender() {
            return sender;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TaskListener that = (TaskListener) o;

            if (requestId != that.requestId) return false;
            if (!sender.equals(that.sender)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = requestId;
            result = 31 * result + sender.hashCode();
            return result;
        }
    }
}