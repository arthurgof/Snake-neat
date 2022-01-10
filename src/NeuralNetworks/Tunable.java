package NeuralNetworks;

public interface Tunable {

    /**
     * @return all weights of the model in a tunable array (changes made to this array change the model itself)
     */
    double[][][] getAllWeights();

    void setAllWeights(double[][][] weights);

    /**
     * Model should store the input array from a prior call of {@linkplain #computeOutput(double[])} if it's needed to compute the gradients.
     * @param E the loss function
     * @param target the target value of a certain training instance
     * @return a matching array of loss gradients for all weights returned by {@linkplain #getAllWeights()} (and in the same order)
     */
    double[][][] calculateLossGradients(NeuralNetwork.LossFunction E, double[] target);

    /**
     * The model should store this input if necessary for computing the loss gradients corresponding to the same training instance
     * @param input the input vector of a training instance
     * @return the output vector as the model computes it given the input
     */
    double[] computeOutput(double[] input);

    /**
     * Must properly implement the clone method as deep-clone!
     */
    Tunable clone();

}