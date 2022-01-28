package NeatNeural.genome;

public class NodeGene extends Gene implements java.io.Serializable{


    private double x,y;

    public NodeGene(int innovation_number) {
        super(innovation_number);
    }

    
    /** 
     * @return double
     */
    public double getX() {
        return x;
    }

    
    /** 
     * @param x
     */
    public void setX(double x) {
        this.x = x;
    }

    
    /** 
     * @return double
     */
    public double getY() {
        return y;
    }

    
    /** 
     * @param y
     */
    public void setY(double y) {
        this.y = y;
    }

    
    /** 
     * @param o
     * @return boolean
     */
    public boolean equals(Object o){
        if(!(o instanceof NodeGene)) return false;
        return innovation_number == ((NodeGene) o).getInnovation_number();
    }

    
    /** 
     * @return String
     */
    @Override
    public String toString() {
        return "NodeGene{" +
                "innovation_number=" + innovation_number +
                '}';
    }

    
    /** 
     * @return int
     */
    public int hashCode(){
        return innovation_number;
    }
}
