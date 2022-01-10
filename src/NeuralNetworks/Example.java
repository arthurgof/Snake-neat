package NeuralNetworks;

import static NeuralNetworks.NeuralNetwork.*;

import java.util.Random;

public class Example{

    public static void main(String[] args) {
        xor();
    }

    @SuppressWarnings("unused")
    public static void xor() {
        Long seed = null;
        GDseed = null;
        if (GDseed == null) {
            GDseed = System.currentTimeMillis();
            System.out.println("GDseed = "+GDseed+"l");
        }
        if (seed != null) {
            xor(seed);
        } else {
            seed = System.currentTimeMillis();
            System.out.println("seed = "+seed+"l");
            xor(seed);
        }
    }
    private static Long GDseed;
    public static void xor(long seed) {
        Random random = new Random(seed);
        int[] structure = {2, 4, 1};
        Activation[] act = {SILU, SILU};
        NeuralNetwork net = new NeuralNetwork(structure, act);
        net.initializeRandomWeights(-1, 1, seed);
        GradientDescent GD = new GradientDescent(net, HALF_SQUARE_ERROR, 0.4, 1000);
        GD.setDynamicLR(true, 1.25, 5);
        GD.setExploration(true, 0.5, 5, 0.1, GDseed);
        GD.start(() -> {
            boolean x1 = random.nextBoolean();
            boolean x2 = random.nextBoolean();
            boolean y = (x1 && !x2) || (!x1 && x2);
            return new double[][] {{ x1 ? 1:0, x2 ? 1:0 }, {y ? 1:0}};
        });
        try {
            Thread.sleep(10);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        while (GD.isBusy()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("done "+GD.getIterations()+" iterations");
            System.out.format("Loss = %.3e (+/- %.3e)\n",GD.getCurrentLoss(), GD.getCurrentLossSD());
        }
        double[][] inputs = {
                {1, 1}, {1, 0}, {0, 1}, {0, 0}
        };
        for (int i=0; i < inputs.length; i++) {
            System.out.format("in: [%.0f, %.0f]; out: [%.15f]\n", inputs[i][0], inputs[i][1], net.forwardProp(inputs[i])[0]);
        }
    }

}