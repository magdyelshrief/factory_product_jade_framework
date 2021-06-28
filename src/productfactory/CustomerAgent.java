package productfactory;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import java.util.Scanner;
import javax.swing.JOptionPane;

public class CustomerAgent extends Agent {

    private static final long serialVersionUID = 1L;
    BuyerGui bgui;
    FactorySellerGui fgui;
    FactorySellerAgent f = new FactorySellerAgent();
    private String targetProductname;
    private AID[] sellerAgents;
    public int price;
    public int ca;

    @Override
    protected void setup() {
        //bgui = new BuyerGui(this);
        //bgui.AgentshowGui();
        System.out.println("Hello! Customer-agent " + getAID().getName() + " is ready.");
        Scanner s = new Scanner(System.in);
        System.out.println("Enter your credit");
        int ac = s.nextInt();
        JOptionPane.showMessageDialog(null, "yor credit has." + ac + "$");
        ca = ac;
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            targetProductname = (String) args[0];
            System.out.println("Target product is " + targetProductname);
            addBehaviour(new TickerBehaviour(this, 10000) {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onTick() {
                    System.out.println("Trying to buy " + targetProductname);
                    DFAgentDescription template = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("product-selling");
                    template.addServices(sd);
                    try {
                        DFAgentDescription[] result = DFService.search(myAgent, template);
                        System.out.println("Found the following seller agents:");
                        sellerAgents = new AID[result.length];
                        for (int i = 0; i < result.length; ++i) {
                            sellerAgents[i] = result[i].getName();
                            System.out.println(sellerAgents[i].getName());
                        }
                    } catch (FIPAException fe) {
                        fe.printStackTrace();
                    }

                    myAgent.addBehaviour(new RequestPerformer());
                }

            });
        } else {
            System.out.println("No target product name specified");
            doDelete();
        }

    }

    @Override
    protected void takeDown() {
        System.out.println("Buyer-agent " + getAID().getName() + " terminating.");
    }

    public int updateOrder(int credit) {
        addBehaviour(new OneShotBehaviour() {

            @Override
            public void action() {
                System.out.println("credit stored is" + credit);
            }
        });
        return credit;
    }
//////////////////////////////////////////////

    private class RequestPerformer extends Behaviour {

        BuyerGui g;
        FactorySellerGui g2;
        private static final long serialVersionUID = 1L;
        private AID bestSeller;
        private int bestPrice;
        private int repliesCnt = 0;
        private MessageTemplate mt;
        private int step = 0;

        @Override
        public void action() {
            switch (step) {
                case 0:
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    for (int i = 0; i < sellerAgents.length; ++i) {
                        cfp.addReceiver(sellerAgents[i]);
                    }
                    cfp.setContent(targetProductname);
                    cfp.setConversationId("product-trade");
                    cfp.setReplyWith("cfp" + System.currentTimeMillis());
                    myAgent.send(cfp);
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("product-trade"),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    step = 1;
                    break;
                case 1:
                    ACLMessage reply = myAgent.receive(mt);
                    if (reply != null) {
                        if (reply.getPerformative() == ACLMessage.PROPOSE) {
                            price = Integer.parseInt(reply.getContent());
                            if (ca < price) {
                                System.out.println("price exceeds the credit"); 
                                JOptionPane.showMessageDialog(null, "yor credit has is less than the product price.");
                                step = 5;
                                block();
                                break;
                            } else {
                                if (bestSeller == null || price < bestPrice) {
                                    bestPrice = price;
                                    bestSeller = reply.getSender();
                                }
                            }
                        }
                        repliesCnt++;
                        if (repliesCnt >= sellerAgents.length) {
                            step = 2;
                        }
                    } else {
                        block();
                    }
                    break;
                case 2:
                    ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                    order.addReceiver(bestSeller);
                    order.setContent(targetProductname);
                    order.setConversationId("product-trade");
                    order.setReplyWith("order" + System.currentTimeMillis());
                    myAgent.send(order);

                    mt = MessageTemplate.and(
                            MessageTemplate.MatchConversationId("product-trade"),
                            MessageTemplate.MatchInReplyTo(order.getReplyWith()));
                    step = 3;
                    break;
                case 3:
                    // Receive the purchase order reply
                    reply = myAgent.receive(mt);
                    if (reply != null) {
                        // Purchase order reply received
                        if (reply.getPerformative() == ACLMessage.INFORM) {
                            // Purchase successful. We can terminate
                            System.out.println(targetProductname
                                    + " successfully purchased from agent "
                                    + reply.getSender().getName());
                            System.out.println("Price = " + bestPrice);
                            myAgent.doDelete();
                        } else {
                            System.out.println("Attempt failed: requested product out of stock.");
                        }

                        step = 4;
                    } else {
                        block();
                    }
                    break;
            }
        }

        @Override
        public boolean done() {
            if (step == 2 && bestSeller == null) {
                System.out.println("Attempt failed: " + targetProductname + " not available for sale");
            }
            return ((step == 2 && bestSeller == null) || step == 4);
        }

        private FactorySellerGui FactorySellerGui() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////

}
