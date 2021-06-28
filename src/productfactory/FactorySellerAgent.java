package productfactory;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Hashtable;
import javax.swing.JOptionPane;

public class FactorySellerAgent extends Agent {

    private static final long serialVersionUID = 1L;

    private Hashtable<String, Integer> catalogue;
    private FactorySellerGui myGui;
    public int c;

    protected void setup() {
        catalogue = new Hashtable<String, Integer>();

        myGui = new FactorySellerGui(this);
        myGui.showGui();

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("product-selling");
        sd.setName("JADE-product-trading");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        
         
        addBehaviour(new OfferRequestsServer());

        addBehaviour(new PurchaseOrdersServer());
    }

    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        myGui.dispose();

        System.out.println("Seller-agent " + getAID().getName() + " terminating.");
    }

    public void updateCatalogue(final String name, final int price) {
        addBehaviour(new OneShotBehaviour() {

            private static final long serialVersionUID = 1L;

            @Override
            public void action() {
                catalogue.put(name, new Integer(price));
                FactorySellerAgent f = new FactorySellerAgent();
                f.c=price;
                JOptionPane.showMessageDialog(null,name + " inserted into catalogue. Price = " + price);
                System.out.println(name + " inserted into catalogue. Price = " + price);
                
            }
        });
    }
    private class OfferRequestsServer extends CyclicBehaviour {

        private static final long serialVersionUID = 1L;

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                String name = msg.getContent();
                ACLMessage reply = msg.createReply();

                Integer price = (Integer) catalogue.get(name);
                if (price != null) {
                    reply.setPerformative(ACLMessage.PROPOSE);
                    reply.setContent(String.valueOf(price.intValue()));
                } else {
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("not-available");
                }
                myAgent.send(reply);
            } else {
                block();
            }
        }
    }

    private class PurchaseOrdersServer extends CyclicBehaviour {

        private static final long serialVersionUID = 1L;

        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                String name = msg.getContent();
                ACLMessage reply = msg.createReply();

                Integer price = (Integer) catalogue.remove(name);
                if (price != null) {
                    reply.setPerformative(ACLMessage.INFORM);
                    System.out.println(name + " sold to agent " + msg.getSender().getName());
                } else {
                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent("not-available");
                }
                myAgent.send(reply);
            } else {
                block();
            }
        }
    }
}
