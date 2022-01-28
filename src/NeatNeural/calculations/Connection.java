package NeatNeural.calculations;


public class Connection implements java.io.Serializable{

    private Node from;
    private Node to;

    private double weight;
    private boolean enabled = true;

    public Connection(Node from, Node to) {
        this.from = from;
        this.to = to;
    }

    
    /** 
     * @return Node
     */
    public Node getFrom() {
        return from;
    }

    
    /** 
     * @param from
     */
    public void setFrom(Node from) {
        this.from = from;
    }

    
    /** 
     * @return Node
     */
    public Node getTo() {
        return to;
    }

    
    /** 
     * @param to
     */
    public void setTo(Node to) {
        this.to = to;
    }

    
    /** 
     * @return double
     */
    public double getWeight() {
        return weight;
    }

    
    /** 
     * @param weight
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }

    
    /** 
     * @return boolean
     */
    public boolean isEnabled() {
        return enabled;
    }

    
    /** 
     * @param enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
