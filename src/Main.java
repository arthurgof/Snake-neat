import Game.Board;
import NeatNeural.neat.Neat;
import NeatNeural.visual.Frame;

import java.io.IOException;

public class Main {
    
    /** 
     * @param args
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException{
        Neat now = Neat.load("libraries.network");
        System.out.println(now.getBest().getScore());
        System.out.println(new Board(40,40,now.getBest().getGenome()).gameLoop());
        new Frame(now.getBest().getGenome());
    }
}