package behaviorTree;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Sequence extends Node {

    public Sequence() {
        super();
        this.currentRoutine = null;
    }

    private Node currentRoutine;
    List<Node> routines = new LinkedList<Node>();
    Queue<Node> routineQueue = new LinkedList<Node>();


    public void addRoutine(Node routine) {
        routines.add(routine);
    }

    @Override
    public void reset() {
        for (Node routine : routines) {
            routine.reset();
        }
    }

    @Override
    public void start() {
        // start the current sequence
        super.start();
        // reset the current queue and copy the routines from setup
        routineQueue.clear();
        routineQueue.addAll(routines);
        currentRoutine = routineQueue.poll();
        currentRoutine.start();
    }

    @Override
    public void act() {

        currentRoutine.act();
        // if is still running, then carry on
        if (currentRoutine.isRunning()) {
            return;
        }

        // check if there are more routines in the queue
        // and if there are then step forward or set the sequence
        // state if finished
        if (routineQueue.peek() == null) {
            // we processed the last routine in the sequence so set the state to that
            this.state = currentRoutine.getState();
            return;
        }

        // We need to progress the sequence. If there are no more routines
        // then the state is the last routine's state. (Success for OR was already handled)
        if (routineQueue.peek() == null) {
            this.state = currentRoutine.getState();
        } else {
            currentRoutine = routineQueue.poll();
            currentRoutine.start();
        }

    }
}
