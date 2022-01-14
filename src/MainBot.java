
import NeatNeural.neat.Client;
import NeatNeural.neat.Neat;
import Game.BoardNotUI;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;


public class MainBot {
    public boolean foodOnly = true;
    private long timeSave = 900000;
    private Semaphore sem2;
    private static String path = "night_test.network";

    public void main(Neat neat, int size){
        int numbretest = 10;
        long begin = System.currentTimeMillis();
        ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()-1);  
        while(true){
            Client best = neat.getBest();
            System.out.println(best.getScore());
            if(best.getScore() > 10){
                foodOnly = false;
            }
            else{
                foodOnly = true;
            }
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
        int size =  10000;
        Neat neat = new Neat(9,4,size);
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
                BoardNotUI b = new BoardNotUI(40,40,c.getGenome(),seed+i).setfoodOnly(foodOnly);
                score += b.gameLoop();
            }
            score /= numbretest;
            c.setScore(score);
            sem2.release(); 
        }
    }
}