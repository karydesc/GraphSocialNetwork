import javax.naming.ldap.spi.LdapDnsProvider;

public class Main {
    public static void main(String[] args) {
        GraphNetwork model = new GraphNetwork();

        String eve = model.addUser("Eve");
        String chris = model.addUser("Chris");
        String jay = model.addUser("Jay");
        String dey = model.addUser("Day");
        String david = model.addUser("David");
        model.addConnection(dey, david, 0.5F);
        model.addConnection(eve, chris, 0.05F);
        model.addConnection(jay, dey, 0.05F);
        model.addConnection(jay, eve, 0.05F);

//        model.printAdjList();
//        model.removeUser(chris);
//        model.adjustRelationship(chris, jay, 1.0F);
//        model.adjustRelationship(jay, dey, 1.1F);
//        model.printAdjList();
        System.out.println(model.getFriends(jay)[1][0]);
    }
}





