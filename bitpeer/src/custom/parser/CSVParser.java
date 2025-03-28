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

package custom.parser;

import java.io.*;
import java.util.*;

import custom.baby_cdn.SimpleReq;
import peersim.config.*;
import peersim.core.Control;
import peersim.core.Network;
import peersim.transport.E2ENetwork;
import peersim.transport.E2ETransport;

/**
 * Initializes static singleton {@link E2ENetwork} by reading a king data set.
 *
 * @author Alberto Montresor
 * @version $Revision: 1.9 $
 */
public class CSVParser implements Control
{

// ---------------------------------------------------------------------
// Parameters
// ---------------------------------------------------------------------

/**
 * The file containing the King measurements.
 * @config
 */
private static final String PAR_FILE = "file";

/**
 * The ratio between the time units used in the configuration file and the
 * time units used in the Peersim simulator.
 * @config
 */
private static final String PAR_RATIO = "ratio";

// ---------------------------------------------------------------------
// Fields
// ---------------------------------------------------------------------

/** Name of the file containing the King measurements. */
private String filename;

/**
 * Ratio between the time units used in the configuration file and the time
 * units used in the Peersim simulator.
 */
private double ratio;

/** Prefix for reading parameters */
private String prefix;

private int net_size;
private int tr_pid;

// ---------------------------------------------------------------------
// Initialization
// ---------------------------------------------------------------------

/**
 * Read the configuration parameters.
 */
public CSVParser(String prefix) {
	this.prefix = prefix;
	net_size = Configuration.getInt("network.size");
	ratio = Configuration.getDouble(prefix + "." + PAR_RATIO, 1);
	filename = Configuration.getString(prefix + "." + PAR_FILE, null);
	tr_pid = Configuration.getPid(prefix+".protocol");
}

// ---------------------------------------------------------------------
// Methods
// ---------------------------------------------------------------------

/**
 * Initializes static singleton {@link E2ENetwork} by reading a king data set.
* @return  always false
*/
public boolean execute() {
	BufferedReader in = null;
	if (filename != null) {
		try {
			in = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException e) {
			throw new IllegalParameterException(prefix + "." + PAR_FILE, filename
					+ " does not exist");
		}
	} else {
		System.err.println("No static data file declared, exiting.");
//		in = new BufferedReader(new InputStreamReader(
//				ClassLoader.getSystemResourceAsStream("latency_data.csv")));
	}

	String line;
	// Skip header line
	int size = 0;
	int lc = 1;

	try {
		// Skip the header
		line = in.readLine();
		while ((line = in.readLine()) != null) {
			size++;
		}
	} catch (IOException e) {
		System.err.println("CSVParser: " + filename + ", line " + lc + ":");
		e.printStackTrace();
		try {
			in.close();
		} catch (IOException e1) {
		}
		System.exit(1);
	}

	E2ENetwork.reset(size, true);

	try {
		// Reopen the file and process the data
		in = new BufferedReader(new FileReader(filename));
		line = in.readLine();  // Skip header
		lc++;

		while ((line = in.readLine()) != null) {
			StringTokenizer tok = new StringTokenizer(line, ",");
			if (tok.countTokens() != 3) {
				System.err.println("CSVParser: " + filename + ", line " + lc + ":");
				System.err.println("Invalid line format: <src, dst, rtt>");
				try {
					in.close();
				} catch (IOException e1) {
				}
				System.exit(1);
			}

			// Parse source, destination, and RTT values
			String src = tok.nextToken().trim();
			String dst = tok.nextToken().trim();
			double rtt = Double.parseDouble(tok.nextToken().trim());

			int srcid = Integer.valueOf(src);
			int dstid = Integer.valueOf(dst);

			// Set latency between nodes
			int latency = (int) (rtt * ratio);
			E2ENetwork.setLatency(srcid, dstid, latency);

			lc++;
		}

		// Set each node's router to be its pid
		// This is key to actually enable the delay when we send messages!
		for (int i = 0; i < Network.size(); i++) {
			E2ETransport protocol = (E2ETransport) Network.get(i).getProtocol(tr_pid);
			protocol.setRouter(i);
		}

		in.close();
	} catch (IOException e) {
		System.err.println("CSVParser: " + filename + ", line " + lc + ":");
		e.printStackTrace();
		try {
			in.close();
		} catch (IOException e1) {
		}
		System.exit(1);
	}

	return false;
}

}
