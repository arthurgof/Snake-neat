package NeatNeural.neat;


import NeatNeural.calculations.Calculator;
import NeatNeural.genome.Genome;

public class Client implements java.io.Serializable{

    private Genome genome;
    private double score;
    private Species species;

    public void generate_calculator(){
        genome.generate_calculator();
    }

    
    /** 
     * @param input
     * @return double[]
     */
    public double[] calculate(double... input){
        return genome.calculate(input);
    }

    
    /** 
     * @param other
     * @return double
     */
    public double distance(Client other) {
        return this.getGenome().distance(other.getGenome());
    }

    public void mutate() {
        getGenome().mutate();
    }

    
    /** 
     * @return Calculator
     */
    public Calculator getCalculator() {
        return genome.getCalculator();
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
     * @return double
     */
    public double getScore() {
        return score;
    }

    
    /** 
     * @param score
     */
    public void setScore(double score) {
        this.score = score;
    }

    
    /** 
     * @return Species
     */
    public Species getSpecies() {
        return species;
    }

    
    /** 
     * @param species
     */
    public void setSpecies(Species species) {
        this.species = species;
    }
}
