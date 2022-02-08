package Game;
import NeatNeural.genome.Genome;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class Board {
    private final int [][] boardgames;
    private final UI graphics;
    private final LinkedList<int []> snakes;
    private String direction;
    private boolean endGame;
    private int [] food;
    private boolean bots;
    private final Genome ann;
    private Random rand;


    public Board() {
        this(null, System.currentTimeMillis());

    }

    public Board(Genome ann, long seed) {
        this(40,40, ann, seed);
    }

    public Board(int a, int b, Genome ann){
        this(a, b, ann, System.currentTimeMillis());
    }

    Board(int a, int b, long seed){
        rand = new Random(seed);
        boardgames = new int[a][b];
        graphics = new UI(a,b,20);
        food = new int[]{rand.nextInt(a), rand.nextInt(b)};
        snakes = new LinkedList<>();
        snakes.add(new int[]{rand.nextInt(a), rand.nextInt(b)});
        direction = "R";
        endGame = true;
        bots = false;
        this.ann = null;
    }

    public Board(int a, int b, Genome ann,long seed){
        rand = new Random(seed);
        boardgames = new int[a][b];
        graphics = new UI(a,b,20);
        food = new int[]{rand.nextInt(a), rand.nextInt(b)};
        snakes = new LinkedList<>();
        direction = "R";
        endGame = true;
        bots = true;
        if(ann == null) bots = false;
        this.ann = ann;
        snakes.add(new int[]{rand.nextInt(a), rand.nextInt(b)});
    }

    private void move(){
        int [] head = snakes.getLast();
        switch (direction) {
            case "R" -> snakes.add(new int[]{head[0] + 1, head[1]});
            case "L" -> snakes.add(new int[]{head[0] - 1, head[1]});
            case "D" -> snakes.add(new int[]{head[0], head[1] + 1});
            case "U" -> snakes.add(new int[]{head[0], head[1] - 1});
        }
        snakes.remove(0);
        validMove();
    }

    private void validMove(){
        int [] head = snakes.getLast();
        if(head[0] >= boardgames.length) endGame = false;
        else if(head[1] >= boardgames[0].length) endGame = false;
        else if(head[0] < 0) endGame = false;
        else if(head[1] < 0) endGame = false;
        else if(boardgames[head[0]][head[1]] == 1) endGame = false;
    }

    private void foodSpawn(){
        food = new int[]{rand.nextInt(boardgames.length), rand.nextInt(boardgames.length)};
        if(boardgames[food[0]][food[1]] == 1) foodSpawn();
    }

    
    /** 
     * @return boolean
     */
    private boolean eat() {
        int [] head = snakes.getLast();
        int [] position = switch (direction) {
            case "R" -> (new int[]{head[0] + 1, head[1]});
            case "L" -> (new int[]{head[0] - 1, head[1]});
            case "D" -> (new int[]{head[0], head[1] + 1});
            case "U" -> (new int[]{head[0], head[1] - 1});
            default -> throw new IllegalStateException("Unexpected value: " + direction);
        };
        if(position[0] == food[0] && position[1] == food[1]){
            snakes.add(position);
            foodSpawn();
            return true;
        }
        return false;
    }

    private void actualize() {
        for(int i=0; i<boardgames.length; i++) {
            for (int j = 0; j <boardgames[0].length;j++){
                boardgames[i][j] = 0;
            }
        }
        boardgames[food[0]][food[1]] = 2;
        for(int [] part: snakes){
            boardgames[part[0]][part[1]] = 1;
        }
    }

    
    /** 
     * @return int
     */
    public int gameLoop(){
        int size = 1;
        int score = 0;
        while (endGame && (score < boardgames.length*boardgames.length || snakes.size() > 15)) {
            if(snakes.size() != size){
                size = snakes.size();
            }
            this.obtainInput();
            score++;
            if(bots){
                double [] result = ann.calculate(this.obtainInput());
                double max = Integer.MIN_VALUE;
                int index = -1;
                for(int i = 0; i < result.length; i++){
                    double r = result[i];
                    if(r > max){
                        max = r;
                        index = i;
                    }
                }
                switch (index) {
                    case 0:
                        if(direction != "D")
                            direction = "U";
                        break;
                    case 1:
                        if(direction != "U")
                            direction = "D";
                        break;
                    case 2:
                        if(direction != "L")
                            direction = "R";
                        break;
                    case 3:
                        if(direction != "R")
                            direction = "L";
                        break;
                }
            }
            try{
                if(bots) Thread.sleep(50);
                else Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            KeyListener listener = new KeyListener(){
                @Override
                public void keyTyped(KeyEvent e) {

                }
                @Override
                public void keyPressed(KeyEvent e) {
                    int keyCode = e.getKeyCode();
                    switch (keyCode) {
                        case KeyEvent.VK_UP :
                            if(direction != "D")
                                direction = "U";
                            break;
                        case KeyEvent.VK_DOWN :
                            if(direction != "U")
                                direction = "D";
                            break;
                        case KeyEvent.VK_LEFT :
                            if(direction != "R")
                                direction = "L";
                            break;
                        case KeyEvent.VK_RIGHT :
                            if(direction != "L")
                                direction = "R";
                            break;
                        case KeyEvent.VK_B :
                            bots = true;
                    }
                }
                @Override
                public void keyReleased(KeyEvent e) {

                }
            };
            if(!eat())move();
            if(endGame){
                actualize();
            }
            graphics.setState(boardgames);
            graphics.getWindow().addKeyListener(listener);
        }
        graphics.getWindow().setVisible(false);
        return snakes.size();
    }

    
    /** 
     * @return double[]
     */
    private double [] obtainInput(){
        double [] input = new double[9];
        int headx = snakes.getLast()[0];
        int heady = snakes.getLast()[1];
        if (headx > food[0])
            input[0] = 1;
        if(headx < food[0])
            input[1] = 1;
        if(heady > food[1])
            input[2] = 1;
        if(heady < food[1])
            input[3] = 1;
        int size = boardgames.length;
        for(int distance = size; distance > 0; distance--) {
            int[] tr1 = new int[]{headx + distance, heady};
            int[] tr2 = new int[]{headx, heady + distance};
            int[] tr3 = new int[]{headx - distance, heady};
            int[] tr4 = new int[]{headx, heady - distance};
            for (int[] b : snakes) {
                if ((tr1[0] == b[0] && tr1[1] == b[1]) || tr1[0] >= size) {
                    input[4] = 1. / distance;
                    break;
                }
            }
            for (int[] b : snakes) {
                if ((tr2[0] == b[0] && tr2[1] == b[1]) || tr2[1] >= size) {
                    input[5] = 1. / distance;
                    break;
                }
            }
            for (int[] b : snakes) {
                if ((tr3[0] == b[0] && tr3[1] == b[1]) || tr3[0] < 0) {
                    input[6] = 1. / distance;
                    break;
                }
            }
            for (int[] b : snakes) {
                if ((tr4[0] == b[0] && tr4[1] == b[1]) || tr4[1] < 0) {
                    input[7] = 1. / distance;
                    break;
                }
            }
            
        }
        input[8] = 1;
        return input;
    }

    
    /** 
     * @param target
     * @return double[]
     */
    private double [] reachable(int [] target){
        double [] score = new double[]{0, 0, 0, 0};
        int [] headd = snakes.getLast();
        ArrayList<int []> heads = generatedirection(headd);
        int index = 0;
        for(int [] head : heads){
            if(!(head[0] >= 0 && head[0] < boardgames.length && 
            head[1] >= 0 && head[1] < boardgames.length))
                continue;
            HashMap<Integer, ArrayList<Integer>> explore = new HashMap<>();
            ArrayList<Integer> y  = new ArrayList<>();
            y.add(head[1]);
            explore.put(head[0], y);
            for(int [] poin : snakes){
                if(explore.containsKey(poin[0])){
                    if(!explore.get(poin[0]).contains(poin[1])){
                        explore.get(poin[0]).add(poin[1]);
                    }
                }
                else{
                    ArrayList<Integer> y1  = new ArrayList<>();
                    y1.add(poin[1]);
                    explore.put(poin[0], y1);
                }
            
            }
            LinkedList<int []> next = new LinkedList<>();
            next.add(head);
            while(!next.isEmpty()){
                score[index]++;
                int [] pointer = next.removeLast();
                ArrayList<int []> alld = generatedirection(pointer);
                for(int [] poin : alld){
                    if(poin[0] >= 0 && poin[0] < boardgames.length && 
                    poin[1] >= 0 && poin[1] < boardgames.length){
                        if(explore.containsKey(poin[0])){
                            if(!explore.get(poin[0]).contains(poin[1])){
                                explore.get(poin[0]).add(poin[1]);
                                next.add(poin);
                            }
                        }
                        else{
                            ArrayList<Integer> y1  = new ArrayList<>();
                            y1.add(poin[1]);
                            explore.put(poin[0], y1);
                            next.add(poin);
                        }
                    }
                }
            }
            if(score[0] == (boardgames.length*boardgames.length) - snakes.size()){
                System.out.println("tee");
                return new double[]{score[0],score[0],score[0],score[0]};
            }
            index ++;
        }
        return score;
    }

    
    /** 
     * @param head
     * @return ArrayList<int[]>
     */
    private ArrayList<int []> generatedirection(int [] headd){
        ArrayList<int []> heads = new ArrayList<>();
        heads.add(new int []{headd[0]+1,headd[1]});
        heads.add(new int []{headd[0]-1,headd[1]});
        heads.add(new int []{headd[0],headd[1]+1});
        heads.add(new int []{headd[0],headd[1]-1});
        return heads;
    }
}
