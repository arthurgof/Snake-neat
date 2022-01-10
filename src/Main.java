import Game.Board;
import Game.BoardNotUI;
import NeatNeural.neat.Neat;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Neat now = Neat.load("newTry.network");
        System.out.println(now.getBest().getScore());
        //while(true)
        System.out.println(new Board(30,30,now.getBest().getGenome(),5).setfoodOnly(false).setdiagonality(true).gameLoop());
    }
}