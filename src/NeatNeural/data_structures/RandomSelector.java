package NeatNeural.data_structures;

import java.util.ArrayList;

public class RandomSelector<T> implements java.io.Serializable{

    private ArrayList<T> objects = new ArrayList<>();
    private ArrayList<Double> scores = new ArrayList<>();

    private double total_score = 0;

    
    /** 
     * @param element
     * @param score
     */
    public void add(T element, double score){
        objects.add(element);
        scores.add(score);
        total_score+=score;
    }

    
    /** 
     * @return T
     */
    public T random() {
        double v = Math.random() * total_score;
        double c = 0;
        for(int i = 0; i < objects.size(); i++){
            c += scores.get(i);
            if(c >= v){
                return objects.get(i);
            }
        }
        return null;
    }

    
    /** 
     * @return int
     */
    public int getsize(){
        return objects.size();
    }

    public void reset() {
        objects.clear();
        scores.clear();
        total_score = 0;
    }

}
