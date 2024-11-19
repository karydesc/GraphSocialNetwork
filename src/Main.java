public class Main {
    public static void main(String[] args) {
        GraphNetwork model = new GraphNetwork();

        String eve = model.addUser("Eve");
        String chris = model.addUser("Chris");
        String jay = model.addUser("Jay");
        String dey = model.addUser("Day");
        model.addConnection(eve, chris, 0.05F);
        model.addConnection(jay, dey, 0.05F);
        model.addConnection(jay, eve, 0.05F);

        model.printAdjList();
        model.removeUser(chris);
        model.adjustRelationship(chris, jay, 1.0F);
        model.adjustRelationship(jay, dey, 1.1F);
        model.printAdjList();
    }
}





