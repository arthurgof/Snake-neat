package NeatNeural.data_structures;


import NeatNeural.genome.Gene;
import java.util.ArrayList;
import java.util.HashSet;

public class RandomHashSet<T> implements java.io.Serializable{

    private HashSet<T> set;
    private ArrayList<T> data;

    public RandomHashSet(){
        set = new HashSet<>();
        data = new ArrayList<>();
    }

    
    /** 
     * @param object
     * @return boolean
     */
    public boolean contains(T object){
        return set.contains(object);
    }

    
    /** 
     * @return T
     */
    public T random_element(){
        if(set.size() > 0){
            return data.get((int)(Math.random() * size()));
        }
        return null;
    }

    
    /** 
     * @return int
     */
    public int size() {
        return data.size();
    }

    
    /** 
     * @param object
     */
    public void add(T object){
        if(!set.contains(object)){
            set.add(object);
            data.add(object);
        }
    }

    
    /** 
     * @param object
     */
    public void add_sorted(Gene object){
        for(int i = 0; i < this.size(); i++){
            int innovation = ((Gene)data.get(i)).getInnovation_number();
            if(object.getInnovation_number() < innovation){
                data.add(i, (T)object);
                set.add((T)object);
                return;
            }
        }
        data.add((T)object);
        set.add((T)object);
    }

    public void clear() {
        set.clear();
        data.clear();
    }

    
    /** 
     * @param index
     * @return T
     */
    public T get(int index){
        if(index < 0 || index >= size())return null;
        return data.get(index);
    }

    
    /** 
     * @param index
     */
    public void remove(int index){
        if(index < 0 || index >= size()) return;
        set.remove(data.get(index));
        data.remove(index);
    }

    
    /** 
     * @param object
     */
    public void remove(T object){
        set.remove(object);
        data.remove(object);
    }

    
    /** 
     * @return ArrayList<T>
     */
    public ArrayList<T> getData() {
        return data;
    }
}
