import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class q_learning {
    public static void main(String[] args) {
        String mazeInputFileName = args[0];
        String valueOutputFileName = args[1];
        String qValueOutputFileName = args[2];
        String policyOutputFileName = args[3];
        int episodes = Integer.parseInt(args[4]);
        int maxLength = Integer.parseInt(args[5]);
        double learningRate = Double.parseDouble(args[6]);
        double discountFactor = Double.parseDouble(args[7]);
        double epsilon = Double.parseDouble(args[8]);

        environment env = new environment(mazeInputFileName);
        ArrayList<ArrayList<environment.State>> maze = env.getMaze();

        int reward;

        ArrayList<ArrayList<Double>> v = new ArrayList<>();
        for(int row = 0; row < maze.size(); row++) {
            ArrayList<Double> vRow = new ArrayList<>();
            for(int col = 0; col < maze.get(row).size(); col++) {
                environment.State state = maze.get(row).get(col);
                if(!(state.isObstacle())) {
                    vRow.add(col, 0.0);
                } else {
                    vRow.add(col, Double.NEGATIVE_INFINITY);
                }
            }
            v.add(vRow);
        }

        ArrayList<ArrayList<ArrayList<Double>>> q = new ArrayList<>();
        for(int row = 0; row < maze.size(); row++) {
            ArrayList<ArrayList<Double>> intermediateList = new ArrayList<>();

            for(int col = 0; col < maze.get(row).size(); col++) {
                ArrayList<Double> actionQs = new ArrayList<>();

                if(!(maze.get(row).get(col).isObstacle())) {
                    for(int action = 0; action < 4; action++) {
                        actionQs.add(0.0);
                    }
                } else {
                    for(int action = 0; action < 4; action++) {
                        actionQs.add(Double.NEGATIVE_INFINITY);
                    }
                }
                intermediateList.add(actionQs);
            }
            q.add(intermediateList);
        }

//        System.out.println("Initial Q: ");
//        for(ArrayList<ArrayList<Double>> intermediateQ : q) {
//            for(ArrayList<Double> actionQs : intermediateQ) {
//                System.out.println(actionQs.toString());
//            }
//            System.out.println("Next row: ");
//        }

        ArrayList<ArrayList<Integer>> pi = new ArrayList<>();
        for(int row = 0; row < q.size(); row++) {
            ArrayList<Integer> actions = new ArrayList<>();
            for(int col = 0; col < q.get(row).size(); col++) {
                if(!q.get(row).get(col).contains(Double.NEGATIVE_INFINITY)) {
                    actions.add(0);
                } else {
                    actions.add(-1);
                }
            }
            pi.add(actions);
        }

        long startTime = System.currentTimeMillis();
        long endTime;
        double averageLength = 0;
        for(int e = 0; e < episodes; e++) {
            env.reset();
            for(int length = 0; length < maxLength; length++) {
                environment.State currentState = env.getCurrentState();
                int row = currentState.getX();
                int col = currentState.getY();

                ArrayList<ArrayList<Double>> intermediateQ = new ArrayList<>(q.get(row));

                ArrayList<Double> updatedQs = new ArrayList<>(intermediateQ.get(col));

                int action = -1;

                if(ThreadLocalRandom.current().nextDouble(0, 1) < (1-epsilon)) {
                    double maxAPrimeQ = Double.NEGATIVE_INFINITY;
                    for(int aPrime = 0; aPrime < 4; aPrime++) {
                        double aPrimeQ = q.get(row).get(col).get(aPrime);
                        if(aPrimeQ > maxAPrimeQ) {
                            maxAPrimeQ = aPrimeQ;
                            action = aPrime;
                        }
                    }
                } else {
                    action = new Random().nextInt(4);
                }

                double actionQ = q.get(row).get(col).get(action);
                environment.EnvTuple outcome = env.step(action);
                environment.State statePrime = outcome.getNextState();
                env.setCurrentState(statePrime);
                reward = outcome.getReward();
                int rowPrime = statePrime.getX();
                int colPrime = statePrime.getY();

                double maxAPrimeQ = Double.NEGATIVE_INFINITY;
                int argmaxAPrime = -1;
                for(int aPrime = 0; aPrime < 4; aPrime++) {
                    double aPrimeQ = q.get(rowPrime).get(colPrime).get(aPrime);
                    if(aPrimeQ > maxAPrimeQ) {
                        maxAPrimeQ = aPrimeQ;
                        argmaxAPrime = aPrime;
                    }
                }
                double discountedMaxAPrimeQ = discountFactor * maxAPrimeQ;
                double updatedQ = (1 - learningRate) * actionQ + learningRate * (reward + discountedMaxAPrimeQ);
                updatedQs.set(action, updatedQ);

                intermediateQ.set(col, updatedQs);
                q.set(row, intermediateQ);

//                System.out.println("After " + (length+1) + " action = " + action + ", we are at state row = " + env.getCurrentState().getX() + ", col = " + env.getCurrentState().getY() + ", which has optimal action of " + argmaxAPrime  + " for " + maxAPrimeQ +". The updated Qs are " + updatedQs.toString());


                if(env.getCurrentState().isGoal()) {
                    averageLength+=(length+1);
                    System.out.println("In episode " + e + ", it took " + (length+1) + " actions to reach the goal.");
                    break;
                }
            }
        }

        for(int row = 0; row < maze.size(); row++) {
            ArrayList<Double> vRow = new ArrayList<>(v.get(row));
            ArrayList<Integer> piRow = new ArrayList<>(pi.get(row));
            ArrayList<ArrayList<Double>> intermediateQ = new ArrayList<>(q.get(row));
            for(int col = 0; col < maze.get(row).size(); col++) {
                ArrayList<Double> actionQs = new ArrayList<>(intermediateQ.get(col));
                int argmaxActionIndex = -1;
                double maxActionQ = Double.NEGATIVE_INFINITY;
                for(int action = 0; action < 4; action++) {
                    double actionQ = actionQs.get(action);
                    if(actionQ > maxActionQ) {
                        maxActionQ = actionQ;
                        argmaxActionIndex = action;
                    }
                }
                piRow.set(col, argmaxActionIndex);
                pi.set(row, piRow);
                vRow.set(col, maxActionQ);
                v.set(row, vRow);
            }
            pi.set(row, piRow);
            v.set(row, vRow);
        }

        System.out.println("It took " + (System.currentTimeMillis()-startTime) + " milliseconds to finish.");
        System.out.println("Average steps: " + (averageLength/episodes));

        System.out.println("\nQ: ");
        for(ArrayList<ArrayList<Double>> qRow : q) {
            for(ArrayList<Double> actionQs : qRow) {
                System.out.println(actionQs.toString());
            }
            System.out.println("Next row: ");
        }

        value_iteration.printV(v, valueOutputFileName);
        value_iteration.printQ(q, qValueOutputFileName);
        value_iteration.printPi(pi, policyOutputFileName);
    }
}
