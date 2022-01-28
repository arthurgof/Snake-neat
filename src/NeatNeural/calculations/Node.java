package NeatNeural.calculations;

import java.util.ArrayList;

public class Node implements Comparable<Node>, java.io.Serializable{

    private double x;
    private double output;
    private ArrayList<Connection> connections = new ArrayList<>();

    public Node(double x) {
        this.x = x;
    }

    public void calculate() {
        double s = 0;
        for(Connection c:connections){
            if(c.isEnabled()){
                s += c.getWeight() * c.getFrom().getOutput();
            }
        }
        output = activation_function(s);
    }

    
    /** 
     * @param x
     * @return double
     */
    private double activation_function(double x){
        return 1d / (1 + Math.exp(-x));
    }

    
    /** 
     * @param x
     */
    public void setX(double x) {
        this.x = x;
    }

    
    /** 
     * @param output
     */
    public void setOutput(double output) {
        this.output = output;
    }

    
    /** 
     * @param connections
     */
    public void setConnections(ArrayList<Connection> connections) {
        this.connections = connections;
    }

    
    /** 
     * @return double
     */
    public double getX() {
        return x;
    }

    
    /** 
     * @return double
     */
    public double getOutput() {
        return output;
    }

    
    /** 
     * @return ArrayList<Connection>
     */
    public ArrayList<Connection> getConnections() {
        return connections;
    }


    
    /** 
     * @param o
     * @return int
     */
    @Override
    public int compareTo(Node o) {
        if(this.x > o.x) return -1;
        if(this.x < o.x) return 1;
        return 0;
    }
}
