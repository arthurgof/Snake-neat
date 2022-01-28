package NeatNeural.genome;


import NeatNeural.neat.Neat;

public class ConnectionGene extends Gene implements java.io.Serializable{

    private NodeGene from;
    private NodeGene to;

    private double weight;
    private boolean enabled = true;

    private int replaceIndex;

    public ConnectionGene(NodeGene from, NodeGene to) {
        this.from = from;
        this.to = to;
    }

    
    /** 
     * @return NodeGene
     */
    public NodeGene getFrom() {
        return from;
    }

    
    /** 
     * @param from
     */
    public void setFrom(NodeGene from) {
        this.from = from;
    }

    
    /** 
     * @return NodeGene
     */
    public NodeGene getTo() {
        return to;
    }

    
    /** 
     * @param to
     */
    public void setTo(NodeGene to) {
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
    
    /** 
     * @param o
     * @return boolean
     */
    public boolean equals(Object o){
        if(!(o instanceof ConnectionGene)) return false;
        ConnectionGene c = (ConnectionGene) o;
        return (from.equals(c.from) && to.equals(c.to));
    }

    
    /** 
     * @return String
     */
    @Override
    public String toString() {
        return "ConnectionGene{" +
                "from=" + from.getInnovation_number() +
                ", to=" + to.getInnovation_number() +
                ", weight=" + weight +
                ", enabled=" + enabled +
                ", innovation_number=" + innovation_number +
                '}';
    }

    
    /** 
     * @return int
     */
    public int hashCode() {
        return from.getInnovation_number() * Neat.MAX_NODES + to.getInnovation_number();
    }

    
    /** 
     * @return int
     */
    public int getReplaceIndex() {
        return replaceIndex;
    }

    
    /** 
     * @param replaceIndex
     */
    public void setReplaceIndex(int replaceIndex) {
        this.replaceIndex = replaceIndex;
    }
}
