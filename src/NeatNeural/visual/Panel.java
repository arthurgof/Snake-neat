package NeatNeural.visual;

import NeatNeural.genome.ConnectionGene;
import NeatNeural.genome.Genome;
import NeatNeural.genome.NodeGene;

import javax.swing.*;
import java.awt.*;

public class Panel extends JPanel {

    private Genome genome;

    public Panel() {
    }

    
    /** 
     * @return Genome
     */
    public Genome getGenome() {
        return genome;
    }

    
    /** 
     * @param genome
     */
    public void setGenome(Genome genome) {
        this.genome = genome;
    }

    
    /** 
     * @param g
     */
    @Override
    protected void paintComponent(Graphics g) {
        g.clearRect(0,0,10000,10000);
        g.setColor(Color.black);
        g.fillRect(0,0,10000,10000);

        for(ConnectionGene c:genome.getConnections().getData()){
            paintConnection(c, (Graphics2D) g);
        }


        for(NodeGene n:genome.getNodes().getData()) {
            paintNode(n, (Graphics2D) g);
        }

    }

    
    /** 
     * @param n
     * @param g
     */
    private void paintNode(NodeGene n, Graphics2D g){
        g.setColor(Color.gray);
        g.setStroke(new BasicStroke(3));
        g.drawOval((int)(this.getWidth() * n.getX()) - 10,
                (int)(this.getHeight() * n.getY()) - 10,20,20);
    }

    
    /** 
     * @param c
     * @param g
     */
    private void paintConnection(ConnectionGene c, Graphics2D g){
        g.setColor(c.isEnabled() ? Color.green:Color.red);
        g.setStroke(new BasicStroke(3));
        g.drawString(new String(c.getWeight() + "       ").substring(0,7),
                (int)((c.getTo().getX() + c.getFrom().getX())* 0.5 * this.getWidth()),
                (int)((c.getTo().getY() + c.getFrom().getY())* 0.5 * this.getHeight()) +15);
        g.drawLine(
                (int)(this.getWidth() * c.getFrom().getX()),
                (int)(this.getHeight() * c.getFrom().getY()),
                (int)(this.getWidth() * c.getTo().getX()),
                (int)(this.getHeight() * c.getTo().getY()));
    }

}
