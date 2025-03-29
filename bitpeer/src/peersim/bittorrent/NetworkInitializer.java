/*
 * Copyright (c) 2007-2008 Fabrizio Frioli, Michele Pedrolli
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
 * --
 *
 * Please send your questions/suggestions to:
 * {fabrizio.frioli, michele.pedrolli} at studenti dot unitn dot it
 *
 */

package peersim.bittorrent;

import peersim.config.IllegalParameterException;
import peersim.core.*;
import peersim.config.Configuration;
import peersim.edsim.EDSimulator;
import peersim.transport.E2ENetwork;
import peersim.transport.E2ETransport;
import peersim.transport.Transport;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;
import java.util.StringTokenizer;

/**
 * This {@link Control} ...
 */
public class NetworkInitializer implements Control {
	/**
	* The protocol to operate on.
	*
	* @config
	*/
	private static final String PAR_PROT="protocol";
		
	private static final String PAR_TRANSPORT="transport";
	
	private static final int TRACKER = 11;
	
	private static final int CHOKE_TIME = 13;
	
	private static final int OPTUNCHK_TIME = 14;
	
	private static final int ANTISNUB_TIME = 15;
	
	private static final int CHECKALIVE_TIME = 16;
	
	private static final int TRACKERALIVE_TIME = 17;
	
	/** Protocol identifier, obtained from config property */
	private final int pid;
	private final int tid;
	private NodeInitializer init;
	
	private Random rnd;

	private String filename;
	private String PAR_FILE = "filename";
	private String PAR_DELAY_TID = "netdelay";
	private final int delay_tid;

	private String prefix;
	public NetworkInitializer(String prefix) {
		this.prefix = prefix;
		pid = Configuration.getPid(prefix+"."+PAR_PROT);
		tid = Configuration.getPid(prefix+"."+PAR_TRANSPORT);

		delay_tid = Configuration.getPid(prefix+"."+PAR_DELAY_TID);
		filename = Configuration.getString(prefix+"."+PAR_FILE);

		init = new NodeInitializer(prefix);
	}
	
	public boolean execute() {
		int completed;
		Node tracker = Network.get(0);
		
		// manca l'inizializzazione del tracker;

		init_users_from_user_file(); // Assigns all instances of Bittoreent protocol a "region" field
		init_netdelay_fromnetdelay_file(); // Sets up an e2etransfer netdelay

		((BitTorrent)Network.get(0).getProtocol(pid)).initializeTracker();
		
		for(int i=1; i<Network.size(); i++){
			System.err.println("chiamate ad addNeighbor " + i);
			((BitTorrent)Network.get(0).getProtocol(pid)).addNeighbor(Network.get(i));
			init.initialize(Network.get(i));
		}
		for(int i=1; i< Network.size(); i++){
			Node n = Network.get(i);
			long latency = ((Transport)n.getProtocol(tid)).getLatency(n,tracker);
			Object ev = new SimpleMsg(TRACKER, n);
			EDSimulator.add(latency,ev,tracker,pid);
			ev = new SimpleEvent(CHOKE_TIME);
			EDSimulator.add(10000,ev,n,pid);
			ev = new SimpleEvent(OPTUNCHK_TIME);
			EDSimulator.add(30000,ev,n,pid);
			ev = new SimpleEvent(ANTISNUB_TIME);
			EDSimulator.add(60000,ev,n,pid);
			ev = new SimpleEvent(CHECKALIVE_TIME);
			EDSimulator.add(120000,ev,n,pid);
			ev = new SimpleEvent(TRACKERALIVE_TIME);
			EDSimulator.add(1800000,ev,n,pid);
		}
		return true;
	}


	private void init_users_from_user_file() {
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

				((BitTorrent)Network.get(node_id).getProtocol(this.pid)).region = region;

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
	}

	private void init_netdelay_fromnetdelay_file() {
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
				int latency = (int) (rtt);
				E2ENetwork.setLatency(srcid, dstid, latency);

				lc++;
			}

			// Set each node's router to be its pid
			// This is key to actually enable the delay when we send messages!
			for (int i = 0; i < Network.size(); i++) {
				E2ETransport protocol = (E2ETransport) Network.get(i).getProtocol(delay_tid);
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
	}

	}
