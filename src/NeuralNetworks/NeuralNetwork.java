package NeuralNetworks;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;

public class NeuralNetwork implements Tunable {

	
	private Layer[] hidden_layers;

	public static void main(String[] args) {
		int [] structure = new int[]{24, 18, 18, 4};
		Activation [] function = new Activation[]{SIGMOID, SIGMOID, SIGMOID};
		NeuralNetwork ann = new NeuralNetwork(structure, function);
		ann.initializeRandomWeights(-2,2);
		double [] input = new double[24];
		for (int i = 0; i < 24;i++){
			input[i] = (int) (Math.random() * 20);
		}
		double [] result = ann.forwardProp(input);
		System.out.println(Arrays.toString(result));
	}
	
	private static boolean[] getFilled(int length) {
		boolean[] result = new boolean[length];
		Arrays.fill(result, true);
		return result;
	}
	
	public NeuralNetwork(int[] network_structure, Activation[] activation_functions) {
		this(network_structure, getFilled(activation_functions.length), activation_functions);
	}
	
	public NeuralNetwork(int[] network_structure, boolean[] bias_inclusion, Activation[] activation_functions) {
		hidden_layers = new Layer[network_structure.length - 1];
		
		if (activation_functions.length < hidden_layers.length || bias_inclusion.length < hidden_layers.length)
			throw new IllegalArgumentException("not enough information provided for number of layers (requires "
					+hidden_layers.length+", got "+activation_functions.length+" and "+bias_inclusion.length+")");
		
		for (int a=0, b=1; b < network_structure.length; a++, b++)
			hidden_layers[a] = new Layer(network_structure[a], network_structure[b], bias_inclusion[a], activation_functions[a]);
	}
	
	@Override
	public NeuralNetwork clone() {
		NeuralNetwork clone = new NeuralNetwork(getLayerStructure(), getBiasInclusion(), getActivations());
		clone.loadWeights(getAllWeights());
		return clone;
	}
	
	public void storeToFile(File file) throws IOException {
		storeToFile(new NeuralNetwork[] {this}, file);
	}
	
	/**
	 * Replaces the contents of the file with a string representation of the given networks
	 * @param networks
	 * @param file
	 * @throws IOException
	 */
	public static void storeToFile(NeuralNetwork[] networks, File file) throws IOException {
		if (!file.exists()) file.createNewFile();
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		for (NeuralNetwork network : networks) {
			out.print(network+" ");
		}
		out.close();
	}
	
	public static NeuralNetwork[] readFromFile(File file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		List<StringBuilder> blocks = new ArrayList<>();
		int r = 0;
		boolean in_block = false;
		boolean skipping = false;
		boolean last_slash = false;
		while((r = br.read()) != -1) {
			char read = (char)r;
			if (read == '/') {
				if (!last_slash) last_slash = true;
				else skipping = true;
				continue;
			} else {
				if (!skipping && last_slash && blocks.size() > 0) blocks.get(blocks.size()-1).append("/");
				last_slash = false;
			}
			if (skipping) {
				if (read == '\n') {
					skipping = false;
				} else continue;
			}
			if (read == '{') {
				if (!in_block) {
					blocks.add(new StringBuilder());
					in_block = true;
				}
			}
			else if (read == '}') {
				in_block = false;
			}
			else if (blocks.size() > 0 && in_block) {
				blocks.get(blocks.size()-1).append(read);
			}
		}
		br.close();
		List<NeuralNetwork> networks = new ArrayList<NeuralNetwork>(blocks.size());
		for (StringBuilder block : blocks) {
			if (block.length() <= 0) continue;
			NeuralNetwork net = parseNetwork(block.toString());
			if (net != null) networks.add(net);
		}
		NeuralNetwork[] result = new NeuralNetwork[networks.size()];
		for (int i=0; i < result.length; i++) result[i] = networks.get(i);
		return result;
	}
	
	public static NeuralNetwork parseNetwork(String network_string) {
		List<Activation> activations = new ArrayList<>();
		List<Boolean> biases = new ArrayList<>();
		List<double[][]> layers = new ArrayList<>();
		List<Integer> structure = new ArrayList<>();
		
		String sp = "[\\s\\v\\h]*";
		String[] parts = network_string.split(sp+";"+sp);
		if (parts.length < 3) return null;
		parts[0] = parts[0].strip();
		for (int p=0; p < parts.length; p++) {
			String[] breakdown = parts[p].split(sp+"="+sp);
			String label = breakdown[0];
			String value = breakdown[1];
			switch(label) {
				case ("activation"):;
				case ("activations"):{
					String[] names = value.split(sp+","+sp);
					for (int i=0; i < names.length; i++) {
						Activation act = Activation.getFromName(names[i]);
						if (act != null) activations.add(act);
					}
				} break;
				case("bias"):;
				case("biases"):{
					String bias = value.replaceAll(sp, "");
					char[] chars = bias.toCharArray();
					for (int i=0; i < chars.length; i++) {
						if (chars[i] == '1') biases.add(true);
						if (chars[i] == '0') biases.add(false);
					}
				} break;
				case("weight"):;
				case("weights"):{
					String[] str_layers = value.split(sp+"\\),"+sp);  
					for (int i=0; i < str_layers.length; i++) {
						double[][] m = Utils.parseMatrix(str_layers[i]);
						layers.add(m);
						if (i==0) structure.add(m[0].length);
						structure.add(m.length);
					}
				} break;
				default:;
			}
		}
		if (layers.size() == 0 || layers.size() > biases.size() || layers.size() > activations.size())
			return null;
		if (biases.get(0)) structure.set(0, structure.get(0) - 1);
		
		int[] struct = new int[structure.size()];
		for (int i=0; i < struct.length; i++) struct[i] = structure.get(i);
		boolean[] bias = new boolean[biases.size()];
		for (int i=0; i < bias.length; i++) bias[i] = biases.get(i);
		double[][][] weights = new double[layers.size()][][];
		for (int i=0; i < weights.length; i++) weights[i] = layers.get(i);
		
		NeuralNetwork network = new NeuralNetwork(struct, bias, activations.toArray(new Activation[activations.size()]));
		network.loadWeights(weights);
		return network;
	}
	
	/**
	 * Initializes the weights of this neural network according to the given weight supplier.
	 * @param weight_supplier given the integer input of layer index (starting from 0, ending at {@link #getLayerCount()}{@code -1})
	 *  should generate a (possibly random) floating point number for some weight in that layer.
	 */
	public void initializeWeights(Function<Integer, Double> weight_supplier) {
		for (int l=0; l < hidden_layers.length; l++) {
			double[][] weights = hidden_layers[l].weights;
			for (int i=0; i < weights.length; i++)
				for (int j=0; j < weights[i].length; j++)
					weights[i][j] = weight_supplier.apply(l);
		}
	}
	
	/**
	 * Initialized the weights of this neural network with random weights between the given minimum and maximum values.
	 * @param min the minimum weight to be generated (inclusive)
	 * @param max the maximum weight to be generated (exclusive)
	 * @param seed the seed of randomness for the weight generation
	 */
	public void initializeRandomWeights(double min, double max, long seed) {
		Random random = new Random(seed);
		initializeWeights(l -> random.nextDouble() * (max-min) + min);
	}
	
	/**
	 * Initialized the weights of this neural network with random weights between the given minimum and maximum values.
	 * @param min the minimum weight to be generated (inclusive)
	 * @param max the maximum weight to be generated (inclusive)
	 */
	public void initializeRandomWeights(double min, double max) {
		initializeRandomWeights(min, max, System.currentTimeMillis());
	}
	
	public void loadWeights(double[][][] weights) {
		boolean incorrect_size = false;
		if (weights.length != hidden_layers.length) incorrect_size = true;
		else
			for (int l=0; l < weights.length; l++)
				if (weights[l].length != hidden_layers[l].weights.length) incorrect_size = true;
				else if (weights[l][0].length != hidden_layers[l].weights[0].length) incorrect_size = true;
		if (incorrect_size) throw new IllegalArgumentException(
				"can't load in weights with non-matching layer structure (consider the existence or absence of bias columns!)");
		
		for (int l=0; l < hidden_layers.length; l++)
			for (int i=0; i < hidden_layers[l].weights.length; i++)
				for (int j=0; j < hidden_layers[l].weights[i].length; j++)
					hidden_layers[l].weights[i][j] = weights[l][i][j];
	}
	
	/**
	 * This method allows modification of the actual weights of this neural network and should thus be used carefully
	 * @return the weights of every layer in the network, in order of forward propagation
	 */
	public double[][][] getAllWeights() {
		double[][][] result = new double[hidden_layers.length][][];
		for (int l=0; l < result.length; l++)
			result[l] = hidden_layers[l].getWeights();
		return result;
	}
	
	public void setAllWeights(double[][][] weights) {
		for (int i=0; i < hidden_layers.length; i++) {
			for (int j=0; j < hidden_layers[i].weights.length; j++)
				for (int k=0; k < hidden_layers[i].weights[j].length; k++)
					hidden_layers[i].weights[j][k] = weights[i][j][k];
		}
	}
	
	public double[] forwardProp(double[] input) {
		int input_size = getInputSize();
		if (input.length != input_size) throw new IllegalArgumentException
			("input size of "+input.length+" does not match input size of layer ("+input_size+")");
		double[] current = input;
		for (int l=0; l < hidden_layers.length; l++)
			current = hidden_layers[l].forwardProp(current);
		return current;
	}
	

	public double[] computeOutput(double[] input) {
		return forwardProp(input);
	}
	
	/**
	 * Function assumes {@linkplain #forwardProp(double[])} has already been executed on the corresponding input vector, prior to this call.
	 * @param E the loss function to calculate the gradients of for this network's weights
	 * @param target the target value of the output vector given the input vector previously put into the model through forward propagation
	 * @return an ordered array of weight gradients per layer, ordered the same as {@linkplain Layer#weights}
	 */
	public double[][][] calculateLossGradients(LossFunction E, double[] target) {
		double[][][] gradients = new double[hidden_layers.length][][];
		double[][] deltas = new double[hidden_layers.length][];
		for (int l=0; l < gradients.length; l++)
			gradients[l] = new double[hidden_layers[l].weights.length][hidden_layers[l].weights[0].length];
		for (int l=0; l < deltas.length; l++)
			deltas[l] = new double[hidden_layers[l].getOutputSize()];
		
		// initialize gradients in output layer (start of back-propagation)
		// credit [https://en.wikipedia.org/wiki/Backpropagation] for help on formulas
		int out = hidden_layers.length - 1;
		Activation last = hidden_layers[out].activation;
		double[] y = last.activate(hidden_layers[out].stored_raw_outputs);
		for (int j=0; j < gradients[out].length; j++)
			for (int i=0; i < gradients[out][j].length; i++) {
				deltas[out][j] = E.derivate(y[j], j, target) * last.derivate(hidden_layers[out].stored_raw_outputs[j]);
				gradients[out][j][i] = deltas[out][j] * hidden_layers[out].stored_inputs[i];
			}
		
		// recursion of back propagation (but we can just as easily iterate it instead)
		// working on weight Wij from i to j
		for (int l=out-1; l >= 0; l--) {
			for (int j=0; j < gradients[l].length; j++) {
				
				double sum = 0;
				for (int k=0; k < hidden_layers[l+1].getOutputSize(); k++)
					sum += hidden_layers[l+1].getWeight(j, k) * deltas[l+1][k]; // backprop step
				deltas[l][j] = sum * hidden_layers[l].activation.derivate(hidden_layers[l].stored_raw_outputs[j]);
				
				for (int i=0; i < gradients[l][j].length; i++)
					gradients[l][j][i] = deltas[l][j] * hidden_layers[l].stored_inputs[i];
			}
		}
		return gradients;
	}
	
	public String toString(int precision) {
		StringWriter str_wr = new StringWriter();
		PrintWriter out = new PrintWriter(str_wr);
		out.println("{");
		// activations:
		out.println("  activations = ");
		boolean first = true;
		for (Activation act : this.getActivations()) {
			String act_string = act.name;
			if (first) {
				out.print("    "+act_string);
				first = false;
			} else out.print(",\n    "+act_string);
		}
		out.println(";");
		// bias:
		out.print("  bias = ");
		for (Boolean bool : this.getBiasInclusion())
			out.print(bool? "1":"0");
		out.println(";");
		// weights:
		out.println("  weights = ");
		String s = "    ";
		for (int l=0; l < this.hidden_layers.length; l++) {
			double[][] weights = this.hidden_layers[l].weights;
			out.print(s + Utils.matrixToString(weights, precision).replaceAll("\n", "\n"+s));
			if (l < this.hidden_layers.length - 1) out.println(",");
		}
		out.println(";");
		out.print("}");
		out.close();
		return str_wr.toString();
	}
	
	/**
	 * string representation of the neural network (same as when writing to a file)
	 */
	@Override
	public String toString() {
		return toString(20);
	}
	
	public int getInputSize() {
		return hidden_layers[0].getInputSize();
	}
	public int getOutputSize() {
		return hidden_layers[hidden_layers.length-1].getOutputSize();
	}
	public int getLayerCount() {
		return hidden_layers.length;
	}
	public int[] getLayerStructure() {
		int[] structure = new int[hidden_layers.length+1];
		structure[0] = getInputSize();
		for (int l=0; l < hidden_layers.length; l++)
			structure[l+1] = hidden_layers[l].getOutputSize();
		return structure;
	}
	public boolean[] getBiasInclusion() {
		boolean[] bias = new boolean[hidden_layers.length];
		for (int i=0; i < bias.length; i++)
			bias[i] = hidden_layers[i].hasBias();
		return bias;
	}
	public Activation[] getActivations() {
		Activation[] activations = new Activation[hidden_layers.length];
		for (int i=0; i < activations.length; i++)
			activations[i] = hidden_layers[i].getActivation();
		return activations;
	}
	
	static class Layer {
		
		public Layer(int input_size, int output_size, boolean include_bias, Activation activation_function) {
			bias = include_bias;
			activation = activation_function;
			
			int input = input_size + (bias? 1:0);
			weights = new double[output_size][input];
		}
		
		public Layer(int input_size, int output_size, Activation activation) {
			this(input_size, output_size, true, activation);
		}
		
		private boolean bias;
		
		public boolean hasBias() {
			return bias;
		}
		
		private double[][] weights;
		
		/** where these weights go from layer i to layer j, this vector is the previous output Oi */
		double[] stored_inputs;
		
		/** where these weights go from layer i to layer j, this vector is the raw output NETj */
		double[] stored_raw_outputs;
		
		private Activation activation;
		
		public Activation getActivation() {
			return activation;
		}
		
		public double[] forwardPropRaw(double[] input) {
			int input_size = getInputSize();
			if (input.length != input_size) throw new IllegalArgumentException
				("input size of "+input.length+" does not match input size of layer ("+input_size+")");
			
			double[] input_vector = new double[weights[0].length];
			for (int i=0; i < input_vector.length; i++)
				if (i==0 && bias) input_vector[i] = 1;
				else if (bias) input_vector[i] = input[i-1];
				else input_vector[i] = input[i];
			
			stored_inputs = input_vector;
			stored_raw_outputs = Utils.extractVector(Utils.matrixVectorMul(weights, input_vector));
			
			return stored_raw_outputs;
		}
		
		public double[] forwardProp(double[] input) {
			return activation.activate(forwardPropRaw(input));
		}
		
		public int getRawInputSize() {
			return weights[0].length;
		}
		public int getInputSize() {
			return weights[0].length - (bias? 1:0);
		}
		public int getOutputSize() {
			return weights.length;
		}
		
		/**
		 * @param from a neuron in the previous layer i
		 * @param to a neuron in the next layer j
		 * @return the weight Wij assigned to the connection between <b>from</b> and <b>to</b>
		 */
		public double getWeight(int from, int to) {
			return weights[to][from];
		}
		
		/**
		 * This method allows modification of the actual weights of this layer and should thus be used carefully
		 * @return the weights of this layer
		 */
		public double[][] getWeights() {
			return weights;
		}
		
	}
	
	public static class LossFunction {
		
		public final BiFunction<double[], double[], Double> loss;
		public final Utils.TriFunction<Double, Integer, double[], Double> derivative;
		
		/**
		 * @param loss The loss function with as <b>par1</b> the output from the model and as <b>par2</b> the target value for a specific training instance
		 * @param derivative The derivative of the loss function (with respect to output[j]) with <b>par1</b> as output[j], <b>par2</b> as j and <b>par3</b> as 
		 * 	the target value for a specific training instance
		 */
		public LossFunction(BiFunction<double[], double[], Double> loss, Utils.TriFunction<Double, Integer, double[], Double> derivative) {
			this.loss = loss;
			this.derivative = derivative;
		}
		
		/**
		 * @param output the output from the model for the training instance
		 * @param target the target value for the training instance
		 * @return the loss on the given parameters
		 */
		public double calculate(double[] output, double[] target) {
			return loss.apply(output, target);
		}
		/**
		 * @param Yj the value of the output from the model for the j-th element (for the training instance)
		 * @param j the index of the element in which respect the derivative is taken
		 * @param target the target value for the training instance
		 * @return the derivative of the loss function for the specific given output element
		 */
		public double derivate(double Yj, int j, double[] target) {
			return derivative.apply(Yj, j, target);
		}
	}
	
	public static final LossFunction HALF_SQUARE_ERROR = new LossFunction(
			(out, target) -> {
				double sum = 0;
				for (int i=0; i < out.length; i++)
					sum += Math.pow(out[i] - target[i], 2);
				return sum/2d;
			},
			(Yj, j, target) -> Yj - target[j]
		);
	
	/** should convert a number from <-inf, inf> to some value for activating a neuron (more positive is more activated) */
	public static class Activation {
		
		private static Map<String, Activation> activations = new HashMap<>();
		
		public final Function<Double, Double> activation;
		public final Function<Double, Double> derivative;
		public final String name;
		
		public Activation(String name, Function<Double, Double> activation, Function<Double, Double> derivative) {
			this.activation = activation;
			this.derivative = derivative;
			this.name = name;
			activations.put(name, this);
		}
		public double activate(double x) {
			return activation.apply(x);
		}
		public double derivate(double x) {
			return derivative.apply(x);
		}
		public double[] activate(double[] x) {
			double[] r = new double[x.length];
			for (int i=0; i < r.length; i++)
				r[i] = activate(x[i]);
			return r;
		}
		
		public static final Activation getFromName(String name) {
			return activations.get(name);
		}
	}
	
	public static final Activation LINEAR = new Activation("linear", x -> x, x -> 1d);
	
	public static final Activation SIGMOID = new Activation("sigmoid", x -> 1 / (1 + Math.exp(-x)), x -> {
		double f = 1 / (1 + Math.exp(-x));
		return f * (1 - f);
	});
	
	// linear rectifier (ramp-function)
	public static final Activation RELU = new Activation("relu", x -> Math.max(0, x), x -> (x < 0) ? 0d : 1d );
	
	// heaviside function
	public static final Activation STEP = new Activation("step", x -> (x > 0)? 1d : 0d, x -> 0d );
	
	// [credit to https://arxiv.org/pdf/1606.08415.pdf and https://arxiv.org/pdf/1702.03118.pdf]
	public static final Activation SILU = new Activation("silu", x -> x * SIGMOID.activate(x), x -> {
		double s = SIGMOID.activate(x);
		return s * (1 + x * (1 - s));
	});
	
	// derivative of SILU, https://arxiv.org/pdf/1702.03118.pdf found this activation function is really good
	public static final Activation dSILU = new Activation("dsilu", SILU.derivative, x -> {
		double s = SIGMOID.activate(x);
		// σ(x)(1 − σ(x))(2 + x(1 − σ(x)) − x*σ(x)) [credit to https://arxiv.org/pdf/1702.03118.pdf]
		return s * (1 - s) * (2 + x * (1 - s) - x * s);
	});
}
