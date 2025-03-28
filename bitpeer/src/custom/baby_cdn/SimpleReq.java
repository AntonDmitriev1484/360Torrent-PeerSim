package custom.baby_cdn;

import peersim.config.Configuration;
import peersim.core.*;
import peersim.config.FastConfig;
import peersim.transport.E2ETransport;
import peersim.transport.Transport;
import peersim.vector.SingleValueHolder;
import peersim.cdsim.CDProtocol;

import peersim.edsim.EDProtocol;

import java.util.ArrayList;


public class SimpleReq extends SingleValueHolder
implements  CDProtocol, EDProtocol {

    private int transport_pid;

    public ArrayList<Long> time_sent;
    public ArrayList<Long> time_received;

    public SimpleReq(String prefix) {

        super(prefix);
        this.transport_pid = Configuration.getPid(prefix + ".transport");
        this.time_sent = new ArrayList<Long>();
        this.time_received = new ArrayList<Long>();
        // When you want one protocol to USE another, you need to acquire its PID directly from the config file like so
    }

    public void nextCycle(Node node, int protocolID) {
        // Simple test:
        // Once every cycle, Node 1 sends requests to 4 and 8, and a controller measures the latency for return

//        this.time_sent = new ArrayList<Long>();
//        this.time_received = new ArrayList<Long>();
        // Empty arrays are likely something to do with the clears not lining up with the send timing
        // mess around with STEP parameter


            if (node.getID() == 1) {

                Node local_CDN = Network.get(4-1);
                Node nonlocal_CDN = Network.get(8-1);

                if(!local_CDN.isUp() || !nonlocal_CDN.isUp()) return;

                Transport transport = (Transport) node.getProtocol(this.transport_pid);

                // Get a handle to the E2ETransport protocol instance assigned to this node

                long time = CommonState.getTime();
                this.time_sent.add(time);
                transport.send(node, local_CDN, new SimpleReqMessage("Req content "+this.value+" from region at time "+time, (int)this.value, node), protocolID);
                this.time_sent.add(time);
                transport.send(node, nonlocal_CDN, new SimpleReqMessage("Req content "+this.value+" from outside of region at time "+time, (int)this.value, node), protocolID);
                System.out.println(" I am node: "+node.getID()+" I just sent messages at time " +time);
                // Send messages using the E2ETransport object,
                // But, in order to route messages to the DST node's SimpleReq protocol
                // MAKE SURE YOU set send( ... , pid == SimpleReq ID == protocolID)
                // this makes sure that your message event will arrive at the SimpleReq PID that can handle it using its processEvent that it inherits from EDProtocol interface

            }
    }

    public void processEvent( Node node, int pid, Object event ) {
        // Whenever an event is issued to a certain protocol
        // It can respond through processEvent
        // pid is the id of where the event was sent
        // i.e. if nextCycle sent an event to SimpleReq pid, then processEvent will respond, and the pid == SimpleReq pid

        SimpleReqMessage req = (SimpleReqMessage)event;

        long time = CommonState.getTime();

        // Write our response
        if( req.sender!=null ) {

            System.out.println(" I am node: "+node.getID()+" I just received a message at time " +time);

            if (node.getID() == 3 || node.getID() == 7) {
                SimpleReqMessage msg = new SimpleReqMessage(
                        "Returning content " + req.content + " to requesting node " + req.sender.getID(),
                        req.content,
                        null); // set sender to null to designate that this is a response

                ((E2ETransport) node.getProtocol(transport_pid)).
                        send(
                                node,
                                req.sender,
                                msg,
                                pid);
            }
        }
        else { // Null req.sender means its a response to node 1
            this.time_received.add(time);
            System.out.println(" I am node: "+node.getID()+" I just received a message at time "+time);
        }
    }


}
