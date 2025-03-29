import networkx as nx
import csv

G = nx.Graph()

DATA_DIR = "C:\\Users\\soula\\OneDrive\\Desktop\\Programming\\CS525\\360Torrent-PeerSim\\bitpeer\\data"
FILE_NAME = "fake_regional.csv"
FILE = DATA_DIR+"\\"+FILE_NAME

region1_clients = [1,2,3]
region1_cdn_server = [4]

region2_clients = [5,6,7]
region2_cdn_server = [8]
# Note, node id's have to be integers

for cl in region1_clients:
    arr = (region1_cdn_server + region1_clients)
    arr.remove(cl)
    for s in arr:
        G.add_edge(cl, s, weight = 20)

    for s in region2_cdn_server + region2_clients:
        G.add_edge(cl, s, weight = 50)

for cl in region2_clients:
    arr = (region2_cdn_server + region2_clients)
    arr.remove(cl)
    # Note, A node having an edge back to itself blows up peersim

    for s in arr:
        G.add_edge(cl, s , weight = 20)

    for s in region1_cdn_server + region1_clients:
        G.add_edge(cl, s, weight = 50)


# Write edges with weights to a CSV file
with open(FILE, 'w', newline='') as csvfile:
    fieldnames = ['src', 'dst', 'rtt']
    writer = csv.DictWriter(csvfile, fieldnames=fieldnames)

    writer.writeheader()
    for u, v, data in G.edges(data=True):
        writer.writerow({'src': u, 'dst': v, 'rtt': data['weight']})