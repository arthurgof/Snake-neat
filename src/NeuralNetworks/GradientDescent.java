package NeuralNetworks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public class GradientDescent {

    private Tunable model;

    private double learning_rate;
    private NeuralNetwork.LossFunction loss_function;
    private int max_iterations;

    private List<Double> loss_values;
    private int last_loss_index;
    private boolean is_busy = false;
    private boolean stop = false;
    private int iteration_count = 0;

    public static int min_data_interval = 250;

    private DescentThread thread;

    private boolean dynamic_lr = false;
    private double growth_rate = 1.25;
    private int acceleration_interval = 5;
    private double minimum_lr = 0.01;
    private double maximum_lr = 0.6;

    private boolean exploration = false;
    private double exploration_magnitude = 0.5;
    private int exploration_interval = 5;
    /** (1 - min_loss_advantage) * current_loss becomes the threshold to beat (lower loss is better) */
    private double min_loss_advantage = 0.1;
    private Random explorator = new Random();
    private Tunable exploration_model;

    private List<double[][][]> stored_weights_exp = new ArrayList<>();
    private List<Double> avg_loss_exp = new ArrayList<>();
    private List<Integer> next_index_tested_exp = new ArrayList<>();

    private List<double[][]> stored_data_exp = new ArrayList<>();
    private boolean current_net_added_to_exp = false;
    private int current_net_index_exp = -1;
    private int exploration_replacements = 0;

    /**
     * @param model the model to be optimized
     * @param loss loss function being traversed
     * @param learning_rate learning rate of the descent
     * @param max_iterations maximum number of iterations before algorithm stops (set to -1 to raise restriction)
     */
    public GradientDescent(Tunable model, NeuralNetwork.LossFunction loss, double learning_rate, int max_iterations) {
        this.model = model;
        this.learning_rate = learning_rate;
        this.loss_function = loss;
        this.max_iterations = max_iterations;
        loss_values = new ArrayList<>();
        last_loss_index = 0;
    }
    public GradientDescent(Tunable ann, NeuralNetwork.LossFunction loss, double learning_rate) {
        this(ann, loss, learning_rate, -1);
    }
    public GradientDescent(Tunable ann, double learning_rate) {
        this(ann);
        this.learning_rate = learning_rate;
    }
    public GradientDescent(Tunable ann) {
        this(ann, NeuralNetwork.HALF_SQUARE_ERROR, 0.1, -1);
    }

    /**
     * @param set
     * @param growth_rate may be {@code null}: sets to default
     * @param acceleration_interval may be {@code null}: sets to default
     */
    public void setDynamicLR(boolean set, Double growth_rate, Integer acceleration_interval) {
        dynamic_lr = set;
        this.growth_rate = (growth_rate != null)? growth_rate : this.growth_rate;
        this.acceleration_interval = (acceleration_interval != null)? acceleration_interval : this.acceleration_interval;
    }

    
    /** 
     * @param min_lr
     * @param max_lr
     */
    public void setMinMaxLR(Double min_lr, Double max_lr) {
        minimum_lr = (min_lr != null)? min_lr : minimum_lr;
        maximum_lr = (max_lr != null)? max_lr : maximum_lr;
    }

    
    /** 
     * @param loss_function
     * @param learning_rate
     * @param max_iterations
     */
    public void setLossLrMax(NeuralNetwork.LossFunction loss_function, Double learning_rate, Integer max_iterations) {
        this.loss_function = (loss_function != null)? loss_function : this.loss_function;
        this.learning_rate = (learning_rate != null)? learning_rate : this.learning_rate;
        this.max_iterations = (max_iterations != null)? max_iterations : this.max_iterations;
    }

    /**
     * @param set
     * @param exploration_magnitude may be {@code null}: sets to default
     * @param exploration_interval may be {@code null}: sets to default
     * @param exploration_threshold may be {@code null}: sets to default
     * @param seed may be {@code null}: sets to random
     */
    public void setExploration(boolean set, Double exploration_magnitude, Integer exploration_interval, Double exploration_threshold, Long seed) {
        exploration = set;
        this.exploration_interval = (exploration_interval != null)? exploration_interval : this.exploration_interval;
        this.exploration_magnitude = (exploration_magnitude != null)? exploration_magnitude : this.exploration_magnitude;
        min_loss_advantage = (exploration_threshold != null)? exploration_threshold : min_loss_advantage;
        explorator = (seed != null)? new Random(seed) : explorator;
    }
    
    /** 
     * @param set
     * @param exploration_magnitude
     * @param exploration_interval
     * @param exploration_threshold
     */
    public void setExploration(boolean set, Double exploration_magnitude, Integer exploration_interval, Double exploration_threshold) {
        setExploration(set, exploration_magnitude, exploration_interval, exploration_threshold, null);
    }

    /**
     * Starts the gradient descent if it hasn't started yet
     * @param data a supplier of data points, which are as follows:<br>
     *   data.get() = { input_array, target_array }<br>Data points are allowed to be repeated
     *   and can be supplied randomly, this is up to the implementer.
     * @return {@code true} if this call caused the algorithm to start, {@code false} otherwise
     */
    public boolean start(Supplier<double[][]> data) {
        if (is_busy || data == null) return false;
        reuse_data = false;
        thread = new DescentThread(data);
        thread.start();
        return true;
    }

    
    /** 
     * @return boolean
     */
    public boolean startReuseData() {
        if (is_busy || stored_data_exp.size() == 0) return false;
        reuse_data = true;
        thread = new DescentThread(new Supplier<double[][]>() {
            private int index = 0;
            @Override
            public double[][] get() {
                if (index >= stored_data_exp.size()) index = 0;
                return stored_data_exp.get(index++);
            }
        });
        thread.start();
        return true;
    }
    private boolean reuse_data = false;

    
    /** 
     * @param input
     * @param target
     */
    private void trainingStep(double[] input, double[] target) {
        double[] output = model.computeOutput(input);
        double loss_value = loss_function.calculate(output, target);
        loss_values.add(loss_value);
        double[][][] gradients = model.calculateLossGradients(loss_function, target);
        double[][][] weights = model.getAllWeights();

        if (exploration) {
            if (!current_net_added_to_exp)
                addNetToExp(weights, true);
            if (!reuse_data) stored_data_exp.add(new double[][] {input, target});
            if (iteration_count % exploration_interval == 0)
                runExplorationTests(weights);
        }

        // clone the weights when dynamic LR step is activated: (just in case we revert)
        if (dynamic_lr && (iteration_count % acceleration_interval == 0) && clone_weights == null)
            clone_weights = cloneWeights(weights);

        adjustWeights(weights, gradients, learning_rate);

        // dynamic learning_rate test:
        if (dynamic_lr && (iteration_count % acceleration_interval == 0)) {
            double expected = estimateNextLoss(gradients, loss_value);
            double actual_loss = loss_function.calculate(model.computeOutput(input), target);
            if (learning_rate > minimum_lr * growth_rate*growth_rate && actual_loss > loss_value) {
                model.setAllWeights(clone_weights); // go back to previous weights
                learning_rate /= growth_rate*growth_rate;
            } else if (learning_rate < maximum_lr / growth_rate && expected > actual_loss) {
                learning_rate *= growth_rate;
            }
        }
        clone_weights = null;
    }

    private double[][][] clone_weights = null;

    
    /** 
     * @param weights
     * @param currentNet
     */
    private void addNetToExp(double[][][] weights, boolean currentNet) {
        if (currentNet && !current_net_added_to_exp) {
            stored_weights_exp.add(weights);
            avg_loss_exp.add(0d);
            next_index_tested_exp.add(0);
            current_net_added_to_exp = true;
            current_net_index_exp = stored_weights_exp.size()-1;
        } else if (!currentNet) {
            stored_weights_exp.add(weights);
            avg_loss_exp.add(0d);
            next_index_tested_exp.add(0);
        }
    }

    
    /** 
     * @param exp_index
     * @param currentWeights
     */
    private void setCurrentNet(int exp_index, double[][][] currentWeights) {
        stored_weights_exp.set(current_net_index_exp, currentWeights);
        current_net_index_exp = exp_index;
        clone_weights = cloneWeights(stored_weights_exp.get(exp_index));
        model.setAllWeights(clone_weights);
        exploration_replacements++;
    }

    
    /** 
     * @param currentWeights
     */
    private void runExplorationTests(double[][][] currentWeights) {
        setupExplorationModel();
        double min_loss = Double.POSITIVE_INFINITY;
        int min_index = -1;
        int new_index = stored_weights_exp.size() - 1;
        for (int i=new_index; i >= 0; i--) {

            double total_loss = avg_loss_exp.get(i) * next_index_tested_exp.get(i);
            Tunable model = (current_net_index_exp == i)? this.model : exploration_model;
            if (model == exploration_model && i == new_index) exploration_model.setAllWeights(stored_weights_exp.get(i));

            for (int j=next_index_tested_exp.get(i); j < stored_data_exp.size(); j++) {
                double[][] datapoint = stored_data_exp.get(j);
                total_loss += loss_function.calculate(model.computeOutput(datapoint[0]), datapoint[1]);
            }
            avg_loss_exp.set(i, total_loss / stored_data_exp.size());
            next_index_tested_exp.set(i, stored_data_exp.size());
            if (avg_loss_exp.get(i) < min_loss) {
                min_loss = avg_loss_exp.get(i);
                min_index = i;
            }
        }
        boolean can_stay = min_index == new_index;
        double current_loss = avg_loss_exp.get(current_net_index_exp);
        if (min_loss < current_loss - min_loss_advantage * current_loss) {
            //System.out.println(((min_index == new_index)? "new " : "")+"network "+min_index+" has "+min_loss+" << "+current_loss);
            setCurrentNet(min_index, currentWeights);
            exploration_replacements++;
        }
        else can_stay = false;
        if (!can_stay)
            stored_weights_exp.remove(new_index);
    }

    
    /** 
     * @param weights
     * @return double[][][]
     */
    private double[][][] cloneWeights(double[][][] weights) {
        double[][][] clone_weights = new double[weights.length][][];
        for (int i=0; i < weights.length; i++) {
            clone_weights[i] = new double[weights[i].length][weights[i][0].length];
            for (int j=0; j < weights[i].length; j++)
                for (int k=0; k < weights[i][j].length; k++)
                    clone_weights[i][j][k] = weights[i][j][k]; }
        return clone_weights;
    }

    private void setupExplorationModel() {
        exploration_model = model.clone();
        double[][][] weights = exploration_model.getAllWeights();
        for (int i=0; i < weights.length; i++)
            for (int j=0; j < weights[i].length; j++)
                for (int k=0; k < weights[i][j].length; k++)
                    weights[i][j][k] += (2 * explorator.nextDouble() - 1) * exploration_magnitude;
        addNetToExp(weights, false);
    }

    
    /** 
     * @param gradients
     * @param loss
     * @return double
     */
    private double estimateNextLoss(double[][][] gradients, double loss) {
        double sum = 0;
        for (int i=0; i < gradients.length; i++)
            for (int j=0; j < gradients[i].length; j++)
                for (int k=0; k < gradients[i][j].length; k++)
                    sum -= learning_rate * gradients[i][j][k] * gradients[i][j][k];
        return loss + sum;
    }

    
    /** 
     * @param weights
     * @param gradients
     * @param learning_rate
     */
    private void adjustWeights(double[][][] weights, double[][][] gradients, double learning_rate) {
        double[][][] old_weights = new double[weights.length][][];
        for (int i=0; i < weights.length; i++) {
            old_weights[i] = new double[weights[i].length][];
            for (int j=0; j < weights[i].length; j++)
                old_weights[i][j] = Arrays.copyOf(weights[i][j], weights[i][j].length);
        }

        for (int l=0; l < weights.length; l++)
            for (int i=0; i < weights[l].length; i++)
                for (int j=0; j < weights[l][i].length; j++)
                    weights[l][i][j] -= learning_rate * gradients[l][i][j];

        boolean problem = false;
        for (int l=0; l < weights.length; l++)
            for (int i=0; i < weights[l].length; i++)
                for (int j=0; j < weights[l][i].length; j++) {
                    if (Double.isNaN(weights[l][i][j]))
                        problem = true;
                }
        if (problem) {
            System.out.println("Model got out of bounds! try a smaller learning rate!");
            System.exit(0);
        }
    }

    
    /** 
     * @return double
     */
    public double getLearningRate() {
        return learning_rate;
    }
    
    /** 
     * @return int
     */
    public int getExplorationReplacements() {
        return exploration_replacements;
    }

    private class DescentThread extends Thread {

        private Supplier<double[][]> data;
        /**
         * @param data a supplier of data points, which are as follows:<br>
         *   data.get() = { input_array, target_array }<br>Data points are allowed to be repeated
         *   and can be supplied randomly, this is up to the implementer.
         */
        public DescentThread(Supplier<double[][]> data) {
            this.data = data;
        }

        @Override
        public void run() {
            is_busy = true;
            while (!stop && (iteration_count <= max_iterations || max_iterations < 0)) {
                double[][] data_entry;
                data_entry = data.get();
                trainingStep(data_entry[0], data_entry[1]);
                iteration_count++;
            }
            is_busy = false;
        }
    }

    
    /** 
     * @return boolean
     */
    public boolean hasNewData() {
        return last_loss_index < loss_values.size();
    }

    
    /** 
     * @return int
     */
    public int getIterations() {
        return iteration_count;
    }

    private synchronized void updateLatestData() {
        if (!hasNewData()) return;
        double sum = 0;
        int size = loss_values.size();
        int start_index = last_loss_index;
        if (size - start_index < min_data_interval)
            start_index = size - min_data_interval;
        if (start_index < 0) start_index = 0;
        for (int i=start_index; i < size; i++)
            sum += loss_values.get(i);
        double avg = sum / size;
        double var = 0;
        for (int i=start_index; i < size; i++)
            var += Math.pow(loss_values.get(i) - avg, 2);
        var /= size;
        latest_loss_avg = avg;
        latest_loss_sd = Math.sqrt(var);
        last_loss_index = size;
    }

    private double latest_loss_avg;
    private double latest_loss_sd;

    
    /** 
     * @return double
     */
    public synchronized double getCurrentLoss() {
        updateLatestData();
        return latest_loss_avg;
    }
    
    /** 
     * @return double
     */
    public synchronized double getCurrentLossSD() {
        updateLatestData();
        return latest_loss_sd;
    }

    
    /** 
     * @return boolean
     */
    public boolean isBusy() {
        return is_busy;
    }

    public void stop() {
        if (is_busy && !stop) stop = true;
    }
    
    /** 
     * @return Tunable
     */
    public Tunable getModel() {
        return model;
    }

}
