package NeatNeural.neat;
import NeatNeural.data_structures.RandomHashSet;
import NeatNeural.data_structures.RandomSelector;
import NeatNeural.genome.ConnectionGene;
import NeatNeural.genome.Genome;
import NeatNeural.genome.NodeGene;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

public class Neat implements java.io.Serializable{

    public static final int MAX_NODES = (int)Math.pow(2,20);

    private double C1 = 1, C2 = 1, C3 = 1;
    private double CP = 1;

    private double WEIGHT_SHIFT_STRENGTH = 0.3;
    private double WEIGHT_RANDOM_STRENGTH = 1;

    private double SURVIVORS = 0.8;

    private double PROBABILITY_MUTATE_LINK = 0.4;

    public void setSURVIVORS(double SURVIVORS) {
        this.SURVIVORS = SURVIVORS;
    }

    private double PROBABILITY_MUTATE_NODE = 0.2;
    private double PROBABILITY_MUTATE_WEIGHT_SHIFT = 0.5;
    private double PROBABILITY_MUTATE_WEIGHT_RANDOM= 0.2;
    private double PROBABILITY_MUTATE_TOGGLE_LINK = 0.0001;

    private final HashMap<ConnectionGene, ConnectionGene> all_connections = new HashMap<>();
    private final RandomHashSet<NodeGene> all_nodes = new RandomHashSet<>();

    private final RandomHashSet<Client> clients = new RandomHashSet<>();
    private final RandomHashSet<Species> species = new RandomHashSet<>();

    private int max_clients;
    private int output_size;
    private int input_size;
    private int extra_size;

    public Neat(int input_size, int output_size, int clients){
        this.reset(input_size, output_size, clients);
    }

    public Neat(int [] structures, boolean fullyConnect, int clients){
        this.max_clients = clients;
        this.CP = 1;
        this.setSize(structures,fullyConnect);
    }

    public Genome empty_genome(){
        Genome g = new Genome(this);
        for(int i = 0; i < input_size + output_size + extra_size; i++){
            g.getNodes().add(getNode(i + 1));
        }
        return g;
    }

    private Genome fullyConnect(NodeGene[] ann, int [] structures){
        this.PROBABILITY_MUTATE_LINK = 0;
        Genome g = new Genome(this);
        for(int i = 0; i < input_size + output_size + extra_size; i++){
            g.getNodes().add(getNode(i + 1));
        }
        int size1 = 0;
        int size2 = 0;
        for(int i = 0;  i < structures.length - 1; i++){
            int n0 = structures[i];
            int n1 = structures[i + 1];
            size1 += n0;
            for(int x = 0; x < n0; x++){
                for(int y = 0; y < n1; y++){
                    ConnectionGene con = new ConnectionGene(ann[size2 + x], ann[size1+y]);
                    con.setWeight(Math.random() - 0.5);
                    if(Math.random() < .1) con.setEnabled(false);
                    g.getConnections().add(con);
                }
            }
            size2 += n0;
//            all_connections.put(new ConnectionGene(ann[index + n],ann[index]),new ConnectionGene(ann[index],ann[index + n]));
        }
        return g;
    }

    public void reset(int input_size, int output_size, int clients){
        this.extra_size = 0;
        this.input_size = input_size;
        this.output_size = output_size;
        this.max_clients = clients;

        all_connections.clear();
        all_nodes.clear();
        this.clients.clear();

        for(int i = 0;i < input_size; i++){
            NodeGene n = getNode();
            n.setX(0.1);
            n.setY((i + 1) / (double)(input_size + 1));
        }

        for(int i = 0; i < output_size; i++){
            NodeGene n = getNode();
            n.setX(0.9);
            n.setY((i + 1) / (double)(output_size + 1));
        }

        for(int i = 0; i < max_clients; i++){
            Client c = new Client();
            c.setGenome(empty_genome());
            c.generate_calculator();
            this.clients.add(c);
        }
    }

    public void setSize(int [] structures, boolean fullyConnect){
        if(structures.length < 2) return;
        if(structures.length == 2){
            reset(structures[0], structures[1], this.max_clients);
            return;
        }
        int number = 0;
        for (int n : structures){
            number += n;
        }
        NodeGene [] ann = new NodeGene[number];
        this.PROBABILITY_MUTATE_NODE = 0;
        this.input_size = structures[0];
        this.output_size = structures[structures.length - 1];

        all_connections.clear();
        all_nodes.clear();
        this.clients.clear();
        int index  = 0;
        for(int i = 0;i < input_size; i++){
            NodeGene n = getNode();
            n.setX(0.1);
            n.setY((i + 1) / (double)(input_size + 1));
            ann[index++] = n;
        }

        double size = 1./(structures.length - 1);
        for (int i = 1; i < structures.length - 1; i++) {
            extra_size += structures[i];
            for (int y = 0; y < structures[i] ; y++){
                NodeGene n = getNode();
                n.setX(size * i);
                n.setY((y + 1) / (double)(structures[i] + 1));
                ann[index++] = n;
            }
        }

        for(int i = 0; i < output_size; i++){
            NodeGene n = getNode();
            n.setX(0.9);
            n.setY((i + 1) / (double)(output_size + 1));
            ann[index++] = n;
        }

        for(int i = 0; i < max_clients; i++){
            if(fullyConnect){
                Client c = new Client();
                c.setGenome(fullyConnect(ann, structures));
                c.generate_calculator();
                this.clients.add(c);
            }
            else{
                Client c = new Client();
                c.setGenome(empty_genome());
                c.generate_calculator();
                this.clients.add(c);
            }
        }
    }

    public Client getClient(int index) {
        return clients.get(index);
    }

    public static ConnectionGene getConnection(ConnectionGene con){
        ConnectionGene c = new ConnectionGene(con.getFrom(), con.getTo());
        c.setInnovation_number(con.getInnovation_number());
        c.setWeight(con.getWeight());
        c.setEnabled(con.isEnabled());
        return c;
    }
    public ConnectionGene getConnection(NodeGene node1, NodeGene node2){
        ConnectionGene connectionGene = new ConnectionGene(node1, node2);

        if(all_connections.containsKey(connectionGene)){
            connectionGene.setInnovation_number(all_connections.get(connectionGene).getInnovation_number());
        }else{
            connectionGene.setInnovation_number(all_connections.size() + 1);
            all_connections.put(connectionGene, connectionGene);
        }

        return connectionGene;
    }
    public void setReplaceIndex(NodeGene node1, NodeGene node2, int index){
        all_connections.get(new ConnectionGene(node1, node2)).setReplaceIndex(index);
    }
    public int getReplaceIndex(NodeGene node1, NodeGene node2){
        ConnectionGene con = new ConnectionGene(node1, node2);
        ConnectionGene data = all_connections.get(con);
        if(data == null) return 0;
        return data.getReplaceIndex();
    }

    public NodeGene getNode() {
        NodeGene n = new NodeGene(all_nodes.size() + 1);
        all_nodes.add(n);
        return n;
    }
    public NodeGene getNode(int id){
        if(id <= all_nodes.size()) {
            return all_nodes.get(id - 1);
        }
        return getNode();
    }

    public void evolve() {
        gen_species();
        kill();
        remove_extinct_species();
        reproduce();
        mutate();
        for(Client c:clients.getData()){
            c.generate_calculator();
        }
    }

    public void printSpecies() {
        double max = -1;
        double min = Integer.MAX_VALUE;
        double avg = 0;
        System.out.println("##########################################");
        for(Species s:this.species.getData()){
            if(s.getScore() > max)
                max = s.getScore();
            if(s.getScore() < min)
                min = s.getScore();
            avg += s.getScore() * s.size();
        }
        System.out.println("Min : "+ min);
        System.out.println("Max : "+ max);
        System.out.println("Avg : "+ avg/max_clients);
    }

    private void reproduce() {
        RandomSelector<Species> selector = new RandomSelector<>();
        for(Species s:species.getData()){
            selector.add(s, s.getScore());
        }

        for(Client c:clients.getData()){
            if(c.getSpecies() == null){
                Species s = selector.random();
                c.setGenome(s.breed());
                s.force_put(c);
            }
        }
    }

    public void mutate() {
        for(Client c:clients.getData()){
            c.mutate();
        }
    }

    private void remove_extinct_species() {
        for(int i = species.size()-1; i>= 0; i--){
            if(species.get(i).size() <= 1){
                species.get(i).goExtinct();
                species.remove(i);
            }
        }
    }

    private void gen_species() {
        for(Species s:species.getData()){
            s.reset();
        }

        for(Client c:clients.getData()){
            if(c.getSpecies() != null) continue;
            boolean found = false;
            for(Species s:species.getData()){
                if(s.put(c)){
                    found = true;
                    break;
                }
            }
            if(!found){
                species.add(new Species(c));
            }
        }

        for(Species s:species.getData()){
            s.evaluate_score();
        }
    }

    private void kill() {
        for(Species s:species.getData()){
            s.kill(1 - SURVIVORS);
        }
    }

    public Client getBest(){
        double sm = -1;
        Client best = null;
        for(Client c :clients.getData()){
            if(c.getScore() > sm){
                sm = c.getScore();
                best = c;
            }
        }
        return best;
    }

    public void save(String path) throws IOException{
        System.out.println("Begin to save");
        FileOutputStream fileOut = new FileOutputStream(path);
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(this);
        out.close();
        fileOut.close();
        System.out.println("File as been Save");
    }

    public static Neat load(String path) throws IOException, ClassNotFoundException{
        System.out.println("Begin to load");
        FileInputStream fileIn = new FileInputStream(path);
        ObjectInputStream in = new ObjectInputStream(fileIn);
        Neat savefile;
        savefile = (Neat) in.readObject();
        in.close();
        fileIn.close();
        System.out.println("Neat as been loaded");
        return savefile;
    }

    public static void main(String[] args) {
        Neat neat = new Neat(10,1,2);
        neat.evolve();
    }

    public RandomHashSet<Client> getClients() {
        return clients;
    }

    public double getCP() {
        return CP;
    }

    public void setCP(double CP) {
        this.CP = CP;
    }

    public double getC1() {
        return C1;
    }

    public double getC2() {
        return C2;
    }

    public double getC3() {
        return C3;
    }


    public double getWEIGHT_SHIFT_STRENGTH() {
        return WEIGHT_SHIFT_STRENGTH;
    }

    public double getWEIGHT_RANDOM_STRENGTH() {
        return WEIGHT_RANDOM_STRENGTH;
    }

    public double getPROBABILITY_MUTATE_LINK() {
        return PROBABILITY_MUTATE_LINK;
    }

    public double getPROBABILITY_MUTATE_NODE() {
        return PROBABILITY_MUTATE_NODE;
    }

    public double getPROBABILITY_MUTATE_WEIGHT_SHIFT() {
        return PROBABILITY_MUTATE_WEIGHT_SHIFT;
    }

    public double getPROBABILITY_MUTATE_WEIGHT_RANDOM() {
        return PROBABILITY_MUTATE_WEIGHT_RANDOM;
    }

    public double getPROBABILITY_MUTATE_TOGGLE_LINK() {
        return PROBABILITY_MUTATE_TOGGLE_LINK;
    }

    public int getOutput_size() {
        return output_size;
    }

    public int getInput_size() {
        return input_size;
    }



    public void setPROBABILITY_MUTATE_LINK(double PROBABILITY_MUTATE_LINK) {
        this.PROBABILITY_MUTATE_LINK = PROBABILITY_MUTATE_LINK;
    }

    public void setPROBABILITY_MUTATE_NODE(double PROBABILITY_MUTATE_NODE) {
        this.PROBABILITY_MUTATE_NODE = PROBABILITY_MUTATE_NODE;
    }

    public void setPROBABILITY_MUTATE_WEIGHT_SHIFT(double PROBABILITY_MUTATE_WEIGHT_SHIFT) {
        this.PROBABILITY_MUTATE_WEIGHT_SHIFT = PROBABILITY_MUTATE_WEIGHT_SHIFT;
    }

    public void setPROBABILITY_MUTATE_WEIGHT_RANDOM(double PROBABILITY_MUTATE_WEIGHT_RANDOM) {
        this.PROBABILITY_MUTATE_WEIGHT_RANDOM = PROBABILITY_MUTATE_WEIGHT_RANDOM;
    }

    public void setPROBABILITY_MUTATE_TOGGLE_LINK(double PROBABILITY_MUTATE_TOGGLE_LINK) {
        this.PROBABILITY_MUTATE_TOGGLE_LINK = PROBABILITY_MUTATE_TOGGLE_LINK;
    }
}

class EofIndicatorClass implements java.io.Serializable{}