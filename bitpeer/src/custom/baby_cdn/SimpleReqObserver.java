/*
 * Copyright (c) 2003-2005 The BISON Project
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 2 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

package custom.baby_cdn;

import peersim.config.*;
import peersim.core.*;
import peersim.edsim.EDSimulator;
import peersim.transport.E2ETransport;
import peersim.vector.*;
import peersim.util.IncrementalStats;

/**
 * Print statistics for an average aggregation computation. Statistics printed
 * are defined by {@link IncrementalStats#toString}
 *
 * @author Alberto Montresor
 * @version $Revision: 1.17 $
 */
public class SimpleReqObserver implements Control {

    // /////////////////////////////////////////////////////////////////////
    // Constants
    // /////////////////////////////////////////////////////////////////////

    /**
     * Config parameter that determines the accuracy for standard deviation
     * before stopping the simulation. If not defined, a negative value is used
     * which makes sure the observer does not stop the simulation
     *
     * @config
     */
    private static final String PAR_ACCURACY = "accuracy";

    /**
     * The protocol to operate on.
     *
     * @config
     */
    private static final String PAR_PROT = "protocol";

    // /////////////////////////////////////////////////////////////////////
    // Fields
    // /////////////////////////////////////////////////////////////////////

    /**
     * The name of this observer in the configuration. Initialized by the
     * constructor parameter.
     */
    private final String name;

    /** Protocol identifier; obtained from config property {@link #PAR_PROT}. */
    private final int pid;
    private final int simplereq_pid;
    private final int tr_pid;

    // /////////////////////////////////////////////////////////////////////
    // Constructor
    // /////////////////////////////////////////////////////////////////////

    /**
     * Creates a new observer reading configuration parameters.
     */
    public SimpleReqObserver(String name) {
        this.name = name;
        pid = Configuration.getPid(name + "." + PAR_PROT);
        simplereq_pid = Configuration.getPid(name +".protocol");
        tr_pid = Configuration.getPid(name +".transport");
    }

    // /////////////////////////////////////////////////////////////////////
    // Methods
    // /////////////////////////////////////////////////////////////////////

    public boolean execute() {

        for (int i = 0; i < Network.size(); i++) {
            if (i == 1) {
                SimpleReq protocol = (SimpleReq) Network.get(i).getProtocol(simplereq_pid);
                System.out.println("[Observer "+this.name+"] Node 1 sent requests at times: "+protocol.time_sent.toString()
                        +" Node 1 received responses at times: "+protocol.time_received.toString());

                protocol.time_sent.clear();
                protocol.time_received.clear();

                // Having a small problem lining up this observer window with the data
                // currently set it up to where we look at request response / recv times over a 2 secvond window
                // an array isn't the best way to do this honestly, using unique requesat identifiers and a map would be better

                // Not going to worry too much about fixing it here, I get how the controller works and thats the important part.
            }
        }


        return false;
    }



}
