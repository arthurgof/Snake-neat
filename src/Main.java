import Game.Board;
import NeatNeural.neat.Neat;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        //new Board().gameLoop();
        Neat now = Neat.load("night_test.network");
        System.out.println(now.getClients().getData().get(0).getScore());
        System.out.println(new Board(30,30,now.getBest().getGenome()).setfoodOnly(false).gameLoop());
        System.exit(10);
    }
}