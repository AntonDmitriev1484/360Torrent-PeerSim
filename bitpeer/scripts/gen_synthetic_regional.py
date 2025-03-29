import networkx as nx
import csv
import numpy as np
import math


DATA_DIR = "C:\\Users\\soula\\OneDrive\\Desktop\\Programming\\CS525\\360Torrent-PeerSim\\bitpeer\\data"
DELAY_FILE_NAME = DATA_DIR + "\\synthetic_regional_delay.csv"
NODE_FILE_NAME = DATA_DIR + "\\node_info.csv"

# Define regions and their share of the userbase < 1
REGIONS = [["W", 0.3, []], ["N", 0.4, []], ["C", 0.2, []], ["F", 0.1, []]] # This should probably be a map...

# Define delays between regions
net = nx.Graph(data=True)
net.add_edge("W","N", weight=100)
net.add_edge("W","C", weight=35)
net.add_edge("W", "F", weight=120)
net.add_edge("N", "C", weight=120)
net.add_edge("N", "F", weight = 35)
net.add_edge("C", "F", weight=100)

# Map userids to regions
# for simplicity, contiguous id users will be assigned the same region
N_CLIENTS = 20

lower = 0
for i in range(len(REGIONS)):
    region, percent, _ = REGIONS[i]
    upper = lower + percent
    REGIONS[i][2] = list(range(math.floor(lower*N_CLIENTS), math.ceil(upper*N_CLIENTS), 1))
    lower = upper

print(REGIONS)

def get_regionusers_by_string(region_str): return [r[2] for r in REGIONS if r[0]==region_str][0]

with open(DELAY_FILE_NAME, 'w', newline='') as csvfile:
    fieldnames = ['src', 'dst', 'rtt']
    writer = csv.DictWriter(csvfile, fieldnames=fieldnames)

    for src, dst, delay in net.edges(data=True):

        src_users = get_regionusers_by_string(src)
        dst_users = get_regionusers_by_string(dst)

        print(f"{src_users} for {src}")

        for srcu in src_users:
            for dstu in dst_users:
                writer.writerow({'src': srcu, 'dst': dstu, 'rtt': delay["weight"]})

with open(NODE_FILE_NAME, 'w', newline='') as csvfile:
    fieldnames = ['node', 'region']
    writer = csv.DictWriter(csvfile, fieldnames=fieldnames)

    for region, _, _ in REGIONS:
        users = get_regionusers_by_string(region)
        for user in users:
            writer.writerow({'node': user, 'region': region})
