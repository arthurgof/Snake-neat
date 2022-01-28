package NeatNeural.neat;



import NeatNeural.data_structures.RandomHashSet;
import NeatNeural.genome.Genome;
import java.util.Comparator;

public class Species implements java.io.Serializable{

    private RandomHashSet<Client> clients = new RandomHashSet<>();
    private Client representative;
    private double score;

    public Species(Client representative){
        this.representative = representative;
        this.representative.setSpecies(this);
        clients.add(representative);
    }

    
    /** 
     * @param client
     * @return boolean
     */
    public boolean put(Client client){
        if(client.distance(representative) < representative.getGenome().getNeat().getCP()){
            client.setSpecies(this);
            clients.add(client);
            return true;
        }
        return false;
    }

    
    /** 
     * @param client
     */
    public void force_put(Client client) {
        client.setSpecies(this);
        clients.add(client);
    }

    public void goExtinct() {
        for(Client c:clients.getData()){
            c.setSpecies(null);
        }
    }

    public void evaluate_score() {
        double v = 0;
        for(Client c:clients.getData()){
            v += c.getScore();
        }
        score = v / clients.size();
    }

    public void reset() {
        representative = clients.random_element();
        for(Client c:clients.getData()){
            c.setSpecies(null);
        }
        clients.clear();

        clients.add(representative);
        representative.setSpecies(this);
        score = 0;
    }

    
    /** 
     * @param percentage
     */
    public void kill(double percentage) {
        if(clients.size() < 2) return;
        clients.getData().sort(
                new Comparator<Client>() {
                    @Override
                    public int compare(Client o1, Client o2) {
                        return Double.compare(o1.getScore(), o2.getScore());
                    }
                }
        );
        double cc = (percentage * clients.size());
        int amount = Math.toIntExact(Math.round(cc));
        for(int i = 0;i < amount; i++){
            clients.get(0).setSpecies(null);
            clients.remove(0);
        }
    }

    
    /** 
     * @return Genome
     */
    public Genome breed() {
        Client c1 = clients.random_element();
        Client c2 = clients.random_element();

        if(c1.getScore() > c2.getScore()) return Genome.crossOver(c1.getGenome(), c2.getGenome());
        return Genome.crossOver(c2.getGenome(), c1.getGenome());
    }

    
    /** 
     * @return int
     */
    public int size() {
        return clients.size();
    }

    
    /** 
     * @return RandomHashSet<Client>
     */
    public RandomHashSet<Client> getClients() {
        return clients;
    }

    
    /** 
     * @return Client
     */
    public Client getRepresentative() {
        return representative;
    }

    
    /** 
     * @return double
     */
    public double getScore() {
        return score;
    }
}
