import NeatNeural.neat.Client;
import NeatNeural.neat.Neat;
import Game.BoardNotUI;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;


public class MainBot {
    private long timeSave = 900000;
    private Semaphore sem2;
    private static String path = "libraries.network";

    
    /** 
     * @param neat
     * @param size
     */
    public void main(Neat neat, int size){
        int numbretest = 5;
        long begin = System.currentTimeMillis();
        ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()-1);  
        while(true){
            neat.printSpecies();
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
    
    
    /** 
     * @param args
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        int size =  5000;
        Neat neat = new Neat(8, 4, size);
        neat = Neat.load(path);
        new MainBot().main(neat, size);
    }

    class Simulation implements Runnable{
        private final Client c;
        private final int numbretest;
        private final long seed;

        public Simulation(Client c, int numbretest, long seed){
            this.c = c;
            this.numbretest = numbretest;
            this.seed = seed;
        }

        @Override
        public void run() {
            double score = 0;
            for(int i = 0; i < numbretest; i++){
                BoardNotUI b = new BoardNotUI(40,40,c.getGenome(),seed+i);
                score += b.gameLoop();
            }
            score /= numbretest;
            c.setScore(score);
            sem2.release(); 
        }
    }
}