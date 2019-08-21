import java.io.*;
import java.util.ArrayList;

public class environment {
    private State initialState = new State();
    private State currentState = new State();
    private ArrayList<ArrayList<State>> maze = new ArrayList<>();

    public State getInitialState() {
        return initialState;
    }

    public void setInitialState(State initialState) {
        this.initialState = initialState;
    }

    public State getCurrentState() {
        return currentState;
    }

    public void setCurrentState(State currentState) {
        this.currentState = currentState;
    }

    public ArrayList<ArrayList<State>> getMaze() {
        return maze;
    }

    public environment(String mazeFile) {
        BufferedReader reader;
        String input;
        try {
            reader = new BufferedReader(new FileReader(mazeFile));
            int x = 0;
            while((input = reader.readLine()) != null) {
                ArrayList<State> thisRow = new ArrayList<>();
                input = input.replaceAll(" ", "");
                char[] chars = input.toCharArray();
                int y = 0;
                for(char c : chars) {
                    if(c == '.') {
                        thisRow.add(new State(x, y, false, false, false));
                    }
                    if(c == '*') {
                        thisRow.add(new State(x, y, false, false, true));
                    }
                    if(c == 'G') {
                        thisRow.add(new State(x, y, false, true, false));
                    }
                    if(c == 'S') {
                        thisRow.add(new State(x, y, true, false, false));
                        initialState = new State(x, y, true, false, false);
                        currentState = initialState;
                    }
                    y++;
                }
                maze.add(thisRow);
//                System.out.println("This row: ");
//                for(State s : thisRow) {
//                    System.out.print("(" + s.getX() + ", " + s.getY() + ")");
//                }
//                System.out.println();
                x++;
            }
        } catch (NullPointerException e) {
            System.err.println("Null pointer error: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("IO Error: " + e.getMessage());
        }
    }
    public EnvTuple step(int action) {
        State nextState = new State();
        if(action == 0) { //west
            if((currentState.getY()-1) >= 0) {
                nextState = maze.get(currentState.getX()).get(currentState.getY()-1);
            } else {
                nextState = currentState;
            }

        }
        if(action == 1) { //north
            if((currentState.getX()-1)>=0) {
                nextState = maze.get(currentState.getX() - 1).get(currentState.getY());
            } else {
                nextState = currentState;
            }

        }
        if(action == 2) { //east
            if((currentState.getY()+1) < maze.get(currentState.getX()).size()) {
                nextState = maze.get(currentState.getX()).get(currentState.getY()+1);
            } else {
                nextState = currentState;
            }

        }
        if(action == 3) { //south
            if((currentState.getX()+1) < maze.size()) {
                nextState = maze.get(currentState.getX()+1).get(currentState.getY());
            } else {
                nextState = currentState;
            }
        }
        int reward = -1;
        int isTerminal = nextState.isGoal() ? 1 : 0;
        if(nextState.isObstacle()) {
            nextState = currentState;
        }
        return new EnvTuple(nextState, reward, isTerminal);
    }

    public State reset() {
        currentState = initialState;
        return initialState;
    }

    public class State {
        private int x;
        private int y;
        private boolean isStart;
        private boolean isGoal;
        private boolean isObstacle;
        State(int x, int y, boolean isStart, boolean isGoal, boolean isObstacle) {
            this.x = x;
            this.y = y;
            this.isStart = isStart;
            this.isGoal = isGoal;
            this.isObstacle = isObstacle;
        }
        State(int x, int y) {
            this.x = x;
            this.y = y;
        }
        State() {}
        int getX() {
            return this.x;
        }
        void setX(int x) {
            this.x = x;
        }
        int getY() {
            return this.y;
        }
        void setY(int y) {
            this.y = y;
        }
        boolean isStart() {
            return isStart;
        }
        boolean isGoal() {
            return isGoal;
        }
        boolean isObstacle() {
            return isObstacle;
        }
    }

    public class EnvTuple {
        private State nextState;
        private int reward;
        private int isTerminal;

        EnvTuple(State nextState, int reward, int isTerminal) {
            this.nextState = nextState;
            this.reward = reward;
            this.isTerminal = isTerminal;
        }

        State getNextState() {
            return this.nextState;
        }
        int getReward() {
            return this.reward;
        }
        int getIsTerminal() {
            return this.isTerminal;
        }
    }

    public static void main(String[] args) {
        String mazeFileName = args[0];
        String feedbackFileName = args[1];
        String actionsFileName = args[2];

        environment env = new environment(mazeFileName);
        ArrayList<Integer> actions = readActionsFile(actionsFileName);
//        System.out.println("Actions: " + actions.toString());
        ArrayList<EnvTuple> feedback = new ArrayList<>();
        for(int action : actions) {
//            System.out.println("We were in state " + env.getCurrentState().getX() + "," + env.getCurrentState().getY());
            EnvTuple result = env.step(action);
//            System.out.println("We took action " + action);
//            System.out.println("We are now in state " + result.getNextState().getX() + "," + result.getNextState().getY());
            env.setCurrentState(result.getNextState());
            feedback.add(result);
        }
        printFeedback(feedback, feedbackFileName);
    }

    private static ArrayList<Integer> readActionsFile(String actionsFileName) {
        ArrayList<Integer> numberActions = new ArrayList<>();
        BufferedReader reader;
        String input;
        try {
            reader = new BufferedReader(new FileReader(actionsFileName));
            input = reader.readLine();
            String[] actions = input.split(" ");
            for(String action : actions) {
                numberActions.add(Integer.parseInt(action));
            }
        } catch (NullPointerException e) {
            System.err.println("Null pointer error: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("IO Error: " + e.getMessage());
        }
        return numberActions;
    }

    private static void printFeedback(ArrayList<EnvTuple> feedback, String feedbackFileName) {
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(feedbackFileName));
            String s;
            StringBuilder sb = new StringBuilder();

            for(EnvTuple e : feedback) {
                sb.append(e.getNextState().getX());
                sb.append(" ");
                sb.append(e.getNextState().getY());
                sb.append(" ");
                sb.append(e.getReward());
                sb.append(" ");
                sb.append(e.getIsTerminal());
                sb.append('\n');
            }

            s = sb.toString();
            writer.write(s);
            writer.close();
            System.out.println("Finished writing feedback file.");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
