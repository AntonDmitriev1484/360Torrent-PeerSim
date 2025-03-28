package custom.baby_cdn;

import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.E2ETransport;
import peersim.transport.Transport;

/**
 * Wrapper around E2ETransport to make it compatible with EDSimulator.
 */
public class E2ETransportED extends E2ETransport implements EDProtocol {

    /**
     * Constructor to initialize the E2ETransport wrapper.
     */
    public E2ETransportED(String prefix) {
        super(prefix); // Initialize the underlying E2ETransport logic
    }

    /**
     * Processes an incoming event (required by EDProtocol).
     * @param node the node receiving the event
     * @param pid the protocol identifier
     * @param event the incoming event/message
     */

    public void processEvent(Node node, int pid, Object event) {

        SimpleReqMessage req = (SimpleReqMessage)event;

        // We NEVER enter processEvent

        // Write our response
        if( req.sender!=null ) {

            System.out.println(" I am node: "+node.getID()+" I just received a message: "+ req.getValue());


            if (node.getID() == 4-1 || node.getID() == 8-1) {

//                int tr_pid = FastConfig.getTransport((pid));
//                Transport transport_protocol = (Transport) node.getProtocol(tr_pid);
                this.
                        send(
                                node,
                                req.getSender(),
                                new SimpleReqMessage("Returning content " + req.content + " to requesting node " + req.sender.getID(), req.content, null),
                                pid
                        );
            }

        }
        else {
            System.out.println(" I am node: "+node.getID()+" I just received a message: "+ req.getValue());
        }
    }

    /**
     * Clones this protocol instance to support multiple nodes.
     */

//    public Object clone() {
//        return new E2ETransportED(Configuration.getName(null));
//    }
}