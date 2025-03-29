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

package peersim.custom.parser;

import peersim.bittorrent.BitTorrent;
import peersim.config.Configuration;
import peersim.config.IllegalParameterException;
import peersim.core.Control;
import peersim.core.Network;
import peersim.transport.E2ENetwork;
import peersim.transport.E2ETransport;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Initializes static singleton {@link E2ENetwork} by reading a king data set.
 *
 * @author Alberto Montresor
 * @version $Revision: 1.9 $
 */
public class UserParser implements Control
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
private int protocol_pid;

// ---------------------------------------------------------------------
// Initialization
// ---------------------------------------------------------------------

/**
 * Read the configuration parameters.
 */
public UserParser(String prefix) {
	this.prefix = prefix;
	net_size = Configuration.getInt("network.size");
	ratio = Configuration.getDouble(prefix + "." + PAR_RATIO, 1);
	filename = Configuration.getString(prefix + "." + PAR_FILE, null);
	protocol_pid = Configuration.getPid(prefix+".protocol");
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

		int N_VAR = 2;

		while ((line = in.readLine()) != null) {
			StringTokenizer tok = new StringTokenizer(line, ",");
			if (tok.countTokens() != N_VAR) {
				System.err.println("CSVParser: " + filename + ", line " + lc + ":");
				System.err.println("Invalid line format");
				try {
					in.close();
				} catch (IOException e1) {
				}
				System.exit(1);
			}

			// Parse source, destination, and RTT values
			int node_id = Integer.valueOf(tok.nextToken().trim());
			String region = tok.nextToken().trim();

			((BitTorrent)Network.get(node_id).getProtocol(protocol_pid)).region = region;

			lc++;
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
