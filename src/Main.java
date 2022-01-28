import Game.Board;
import NeatNeural.neat.Neat;
import java.io.IOException;

public class Main {
    
    /** 
     * @param args
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        //new Board().gameLoop();
        Neat now = Neat.load("night_test.network");
        System.out.println(now.getBest().getScore());
        System.out.println(new Board(40,40,now.getBest().getGenome()).gameLoop());
        System.exit(10);
    }
}