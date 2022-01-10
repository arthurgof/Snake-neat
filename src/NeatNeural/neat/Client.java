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

    public double[] calculate(double... input){
        return genome.calculate(input);
    }

    public double distance(Client other) {
        return this.getGenome().distance(other.getGenome());
    }

    public void mutate() {
        getGenome().mutate();
    }

    public Calculator getCalculator() {
        return genome.getCalculator();
    }

    public Genome getGenome() {
        return genome;
    }

    public void setGenome(Genome genome) {
        this.genome = genome;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public Species getSpecies() {
        return species;
    }

    public void setSpecies(Species species) {
        this.species = species;
    }
}
