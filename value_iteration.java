import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;

public class value_iteration {
    public static void main(String[] args) {
	// write your code here
        String mazeInput = args[0];
        String valueOutput = args[1];
        String qValueOutput = args[2];
        String policyOutput = args[3];
        int epochs = Integer.parseInt(args[4]);
        double discountFactor = Double.parseDouble(args[5]);

        environment env = new environment(mazeInput);
        ArrayList<ArrayList<environment.State>> maze = env.getMaze();

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
        for(int row = 0; row < v.size(); row++) {
            ArrayList<ArrayList<Double>> intermediateList = new ArrayList<>();

            for(int col = 0; col < v.get(row).size(); col++) {
                ArrayList<Double> actionQs = new ArrayList<>();

                if(v.get(row).get(col) != Double.NEGATIVE_INFINITY) {
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


        ArrayList<ArrayList<Integer>> pi = new ArrayList<>();
        for(int row = 0; row < q.size(); row++) {
            ArrayList<Integer> actions = new ArrayList<>();
            for(int col = 0; col < q.get(row).size(); col++) {
                if(!q.get(row).get(col).contains(Double.NEGATIVE_INFINITY)) {
                    actions.add(-1);
                } else {
                    actions.add(null);
                }
            }
            pi.add(actions);
        }
//
//        System.out.println("V: ");
//        for(ArrayList<Double> vRow : v) {
//            System.out.println(vRow.toString());
//        }
//        System.out.println("\nPi: ");
//        for(ArrayList<Integer> piRow : pi) {
//            System.out.println(piRow.toString());
//        }
//        System.out.println("\nQ: ");
//        for(ArrayList<ArrayList<Double>> intermediateQ : q) {
//            for(ArrayList<Double> actionQs : intermediateQ) {
//                System.out.println(actionQs.toString());
//            }
//        }


        boolean converged;
        int e;
        long startTime = System.currentTimeMillis();
        for(e = 0; e < epochs; e++) {
            ArrayList<ArrayList<Double>> vPrime;
            ArrayList<ArrayList<ArrayList<Double>>> qPrime = new ArrayList<>();
            converged = true;
            vPrime = new ArrayList<>();
            for (int row = 0; row < v.size(); row++) {
                ArrayList<Double> vPrimeRow = new ArrayList<>();
                ArrayList<ArrayList<Double>> intermediateQ = q.get(row);
                for (int col = 0; col < v.get(row).size(); col++) {
                    ArrayList<Double> actionQs = q.get(row).get(col);
                    double oldValue = v.get(row).get(col);
                    if (!(maze.get(row).get(col).isObstacle() || maze.get(row).get(col).isGoal())) {
                        double newValue = -1; //reward
                        double newStateOldValue = Double.NEGATIVE_INFINITY;

                        //southState
                        double southStateOldValue;
                        if (row + 1 < v.size() && v.get(row + 1).get(col) != Double.NEGATIVE_INFINITY) {
                            southStateOldValue = discountFactor * v.get(row + 1).get(col);
                            if (southStateOldValue > newStateOldValue) {
                                newStateOldValue = southStateOldValue;
                            }
                        } else {
                            southStateOldValue = discountFactor * v.get(row).get(col);
                            if (southStateOldValue > newStateOldValue) {
                                newStateOldValue = southStateOldValue;
                            }
                        }
                        actionQs.set(3, southStateOldValue - 1);
                        //westState
                        double westStateOldValue;
                        if (col - 1 > 0 && v.get(row).get(col - 1) != Double.NEGATIVE_INFINITY) {
                            westStateOldValue = discountFactor * v.get(row).get(col - 1);
                            if (westStateOldValue > newStateOldValue) {
                                newStateOldValue = westStateOldValue;
                            }
                        } else {
                            westStateOldValue = discountFactor * v.get(row).get(col);
                            if (westStateOldValue > newStateOldValue) {
                                newStateOldValue = westStateOldValue;
                            }
                        }
                        actionQs.set(0, westStateOldValue - 1);
                        //northState
                        double northStateOldValue;
                        if (row - 1 >= 0 && v.get(row - 1).get(col) != Double.NEGATIVE_INFINITY) {
                            northStateOldValue = discountFactor * v.get(row - 1).get(col);
                            if (northStateOldValue > newStateOldValue) {
                                newStateOldValue = northStateOldValue;
                            }
                        } else {
                            northStateOldValue = discountFactor * v.get(row).get(col);
                            if (northStateOldValue > newStateOldValue) {
                                newStateOldValue = northStateOldValue;
                            }
                        }
                        actionQs.set(1, northStateOldValue - 1);
                        //eastState
                        double eastStateOldValue;
                        if (col + 1 < v.get(row).size() && v.get(row).get(col + 1) != Double.NEGATIVE_INFINITY) {
                            eastStateOldValue = discountFactor * v.get(row).get(col + 1);
                            if (eastStateOldValue > newStateOldValue) {
                                newStateOldValue = eastStateOldValue;
                            }
                        } else {
                            eastStateOldValue = discountFactor * v.get(row).get(col);
                            if (eastStateOldValue > newStateOldValue) {
                                newStateOldValue = eastStateOldValue;
                            }
                        }
                        actionQs.set(2, eastStateOldValue - 1);

                        newValue += newStateOldValue;
                        vPrimeRow.add(newValue);

                    } else {
                        vPrimeRow.add(oldValue);
                    }

//                    if (e == (epochs - 1)) {
                        intermediateQ.set(col, actionQs);
//                    }
                }
                vPrime.add(vPrimeRow);
//                if (e == (epochs - 1)) {
                    qPrime.add(intermediateQ);
//                }
            }
            q = qPrime;
            boolean allInf = true;
            for(int row = 0; row < v.size(); row++) {
                for(int col = 0; col < v.get(row).size(); col++) {
                    if(Double.isFinite(v.get(row).get(col)) && Double.isFinite(vPrime.get(row).get(col))) {
                        allInf = false;
                        if(Math.abs(v.get(row).get(col)-vPrime.get(row).get(col)) >  0.001) {
                            converged = false;
                        }
                    }
                }
            }
            v = vPrime;

            if(converged && !allInf) {
                break;
            }
        }

        ArrayList<ArrayList<Integer>> piPrime = new ArrayList<>();
        ArrayList<ArrayList<Double>> vPrime = new ArrayList<>();
        for(ArrayList<ArrayList<Double>> intermediateQ : q) {
            ArrayList<Integer> piRow = new ArrayList<>();
            ArrayList<Double> vRow = new ArrayList<>();
            for(ArrayList<Double> actionQs : intermediateQ) {
                double maxDiscountedQ = Double.NEGATIVE_INFINITY;
                int argmaxActionIndex = -1;
                for(int action = 0; action < actionQs.size(); action++) {
                    double discountedQ = actionQs.get(action);
                    if(discountedQ > maxDiscountedQ) {
                        maxDiscountedQ = discountedQ;
                        argmaxActionIndex = action;
                    }
                }
                piRow.add(argmaxActionIndex);
                vRow.add(maxDiscountedQ);
            }
            piPrime.add(piRow);
            vPrime.add(vRow);
        }
        pi = piPrime;
        v = vPrime;

        System.out.println("V: ");
        for(ArrayList<Double> vRow : v) {
            System.out.println(vRow.toString());
        }
        System.out.println("\nPi: ");
        for(ArrayList<Integer> piRow : pi) {
            System.out.println(piRow.toString());
        }
        System.out.println("\nQ: ");
        int row = 0;
        for(ArrayList<ArrayList<Double>> qRow : q) {
            System.out.println("Row: " + row);
            for(ArrayList<Double> actionQs : qRow) {
                System.out.println(actionQs.toString());
            }
            row++;
        }

        System.out.println("It took " + (e+1) + " iterations to converge.");
        System.out.println("It took " + (System.currentTimeMillis()-startTime) + " milliseconds to converge");

        printV(v, valueOutput);
        printQ(q, qValueOutput);
        printPi(pi, policyOutput);

//        System.out.println("V: ");
//        for(ArrayList<Double> vRow : v) {
//            System.out.println(vRow.toString());
//        }
//        System.out.println("\nPi: ");
//        for(ArrayList<Integer> piRow : pi) {
//            System.out.println(piRow.toString());
//        }
//        System.out.println("\nQ: ");
//        for(ArrayList<ArrayList<Double>> intermediateQ : q) {
//            for(ArrayList<Double> actionQs : intermediateQ) {
//                System.out.println(actionQs.toString());
//            }
//        }
    }

    public static void printV(ArrayList<ArrayList<Double>> v, String valueOutputFileName) {
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(valueOutputFileName));
            String s;
            StringBuilder sb = new StringBuilder();

            for(int row = 0; row < v.size(); row ++) {
                for(int col = 0; col < v.get(row).size(); col++) {
                    if(v.get(row).get(col) != Double.NEGATIVE_INFINITY) {
                        sb.append(row);
                        sb.append(" ");
                        sb.append(col);
                        sb.append(" ");
                        sb.append(v.get(row).get(col));
                        sb.append('\n');
                    }
                }
            }

            s = sb.toString();
            writer.write(s);
            writer.close();
            System.out.println("Finished writing values file.");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }
    public static void printQ(ArrayList<ArrayList<ArrayList<Double>>> q, String qValueOutputFileName) {
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(qValueOutputFileName));
            String s;
            StringBuilder sb = new StringBuilder();

            for(int row = 0; row < q.size(); row ++) {
                for(int col = 0; col < q.get(row).size(); col++) {
                    for(int action = 0; action < q.get(row).get(col).size(); action++) {
                        if(q.get(row).get(col).get(action) != Double.NEGATIVE_INFINITY) {
                            sb.append(row);
                            sb.append(" ");
                            sb.append(col);
                            sb.append(" ");
                            sb.append(action);
                            sb.append(" ");
                            sb.append(q.get(row).get(col).get(action));
                            sb.append('\n');
                        }
                    }
                }
            }

            s = sb.toString();
            writer.write(s);
            writer.close();
            System.out.println("Finished writing Q file.");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
    public static void printPi(ArrayList<ArrayList<Integer>> pi, String policyOutputFileName) {
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(policyOutputFileName));
            String s;
            StringBuilder sb = new StringBuilder();
            for(int row = 0; row < pi.size(); row ++) {
                for(int col = 0; col < pi.get(row).size(); col++) {
                    if(pi.get(row).get(col) != -1) {
                        sb.append(row);
                        sb.append(" ");
                        sb.append(col);
                        sb.append(" ");
                        sb.append((double)pi.get(row).get(col));
                        sb.append('\n');
                    }
                }
            }
            s = sb.toString();
            writer.write(s);
            writer.close();
            System.out.println("Finished writing policy file.");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
