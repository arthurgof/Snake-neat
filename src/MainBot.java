
import NeatNeural.neat.Client;
import NeatNeural.neat.Neat;
import Game.BoardNotUI;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;


public class MainBot {
    public boolean foodOnly = false;
    public boolean diagonl = false;
    private long timeSave = 100000;
    private Semaphore sem2;
    private static String path = "Try.network";

    public void main(Neat neat, int size){
        int numbretest = 10;
        long begin = System.currentTimeMillis();
        ExecutorService pool = Executors.newFixedThreadPool(100);  
        while(true){
            Client best = neat.getBest();
            System.out.println(best.getScore());
            sem2 = new Semaphore(-size);
            long l1 = System.currentTimeMillis();
            for(Client c:neat.getClients().getData()){
                Runnable si = new Simulation(c, numbretest, l1);
                pool.execute(si);              
            }
            sem2.release();
            try {
                sem2.acquire();
            } catch (InterruptedException e) {
            }
            if(System.currentTimeMillis() - begin > timeSave){
                try {
                    neat.save(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.gc();
                begin = System.currentTimeMillis();
            }
            neat.evolve();
        }
    }
    
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        System.gc();
        int size =  5000;
        Neat neat = new Neat(9,4,size);/*
        neat.setCP(1);
        neat.setPROBABILITY_MUTATE_WEIGHT_RANDOM(0.12);
        neat.setPROBABILITY_MUTATE_WEIGHT_SHIFT(0.4);
        neat.setPROBABILITY_MUTATE_LINK(0.5);
        neat.setPROBABILITY_MUTATE_NODE(0.1);
        neat.setPROBABILITY_MUTATE_TOGGLE_LINK(.1);
        neat.setSURVIVORS(.3);*/
        neat = Neat.load(path   );
        new MainBot().main(neat, size);
    }

    class Simulation implements Runnable{
        private Client c;
        private int numbretest;
        private long seed;

        public Simulation(Client c, int numbretest, long seed){
            this.c = c;
            this.numbretest = numbretest;
            this.seed = seed;
        }

        @Override
        public void run() {
            double score = 0;
            for(int i = 0; i < numbretest; i++){
                BoardNotUI b = new BoardNotUI(20,20,c.getGenome(),seed+i).setfoodOnly(foodOnly).setdiagonality(diagonl);
                score += b.gameLoop();
            }
            score /= numbretest;
            c.setScore(score);
            sem2.release(); 
        }
    }
}