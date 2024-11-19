import java.util.*;

public class GraphNetwork {
    private int vCount = 0;
    private Map<User, HashMap<User, Edge>> adj_list = new HashMap<>();
    //private List edges = new ArrayList<Edge>();
    private Map<String, User> ids = new HashMap<>();

    public void removeUser(String uuid) {
        User userToRemove = this.ids.get(uuid);
        Set<User> list = this.adj_list.get(userToRemove).keySet(); //get all users that have a connection with target
        for (User user : list){
            this.adj_list.get(user).remove(userToRemove);
        }
        this.ids.remove(userToRemove);
        this.adj_list.remove(userToRemove);
        vCount--;
        System.out.println("User with ID " + uuid + " has been removed.");
    }

    public void addConnection(String srcs, String dsts, float weight) {
        User src = this.getUser(srcs);
        User dst = this.getUser(dsts);

        if (weight<0.00F||weight>1.00F){
            System.out.println("Invalid weight");
            return;
        }
        Edge temp = new Edge(src, dst, weight);
        this.adj_list.get(src).put(dst, temp);
        this.adj_list.get(dst).put(src, temp);
        System.out.println("User " + src.name + " and user " + dst.name + " are friends");


    }

    private User getUser(String uuid) {
        return this.ids.get(uuid);
    }

    public String addUser(String name) {
        vCount++;
        User temp = new User(name);

        this.adj_list.put(temp, new HashMap<>());
        this.ids.put(temp.uniqueID, temp);
        System.out.println("User " + name + " added!");
        return temp.uniqueID;

    }

    public void printAdjList() {
        System.out.println("Adjacency List:");
        for (Map.Entry<User, HashMap<User, Edge>> entry : adj_list.entrySet()) {
            User srcUser = entry.getKey();  // The source user
            HashMap<User, Edge> connections = entry.getValue();  // The map of connected users and edges

            System.out.print("User " + srcUser.name + ": ");

            if (connections.isEmpty()) {
                System.out.println("No connections.");
            } else {
                for (Map.Entry<User, Edge> connectionEntry : connections.entrySet()) {
                    User dstUser = connectionEntry.getKey();  // The destination user
                    Edge edge = connectionEntry.getValue();  // The edge between srcUser and dstUser
                    System.out.print(dstUser.name + " (Weight: " + edge.weight + "), ");
                }
                System.out.println();  // Move to the next line after printing all connections
            }
        }
    }

    public String[][] getFriends(String x){ //returns a list of lists
        User user = this.getUser(x);
        int friendCount = this.adj_list.get(user).size();
        String[][] temp = new String[friendCount][2];
        Set keys = this.adj_list.get(user).keySet();
        for (int i = 0; i < friendCount; i++) {
            temp[i][0] = user.name;
            temp[i][1] = user.uniqueID;
        }
        return temp;
    }



    public int getUserCount(){
        return this.vCount;
    }

    public void adjustRelationship(String a, String b, float value) {
        if (value<0.00F||value>1.00F){
            System.out.println("Invalid weight");
            return;
        }

        User user1 = this.ids.get(a);
        User user2 = this.ids.get(b);

        // Check if either user is null, or if user1 doesn't have an adjacency list (no connections), or if there's no connection between them
        if (user1 == null || user2 == null ||
                !this.adj_list.containsKey(user1) ||
                !this.adj_list.get(user1).containsKey(user2)) {
            System.out.println("User not found or no connection between users.");
            return; // Early return if the check fails
        }


        this.adj_list.get(user1).get(user2).changeWeightValue(value);
        this.adj_list.get(user2).get(user1).changeWeightValue(value);

    }

    private class User {
        String name;
        String uniqueID = UUID.randomUUID().toString();
        String[] interests;

        private User(String name) {
            this.name = name;
        }
    }

    private class Edge {
        Set<User> connection = new HashSet<>();
        float weight;

        private Edge(User src, User dst, float weight) {
            connection.add(src);
            connection.add(dst);
            this.weight = weight;
        }
        private void changeWeightValue(float x){
            this.weight = x;
        }
    }


}
