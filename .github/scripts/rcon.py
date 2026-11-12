"""Tiny RCON client: python3 rcon.py <password> <command>... (one command per argument)."""
import socket, struct, sys, time

def packet(rid, kind, body):
    data = struct.pack("<ii", rid, kind) + body.encode() + b"\x00\x00"
    return struct.pack("<i", len(data)) + data

def read(sock):
    size = struct.unpack("<i", sock.recv(4))[0]
    data = b""
    while len(data) < size:
        data += sock.recv(size - len(data))
    rid, kind = struct.unpack("<ii", data[:8])
    return rid, data[8:-2].decode(errors="replace")

s = socket.create_connection(("127.0.0.1", 25575), timeout=300)
s.sendall(packet(1, 3, sys.argv[1]))
read(s)
for i, cmd in enumerate(sys.argv[2:]):
    s.sendall(packet(10 + i, 2, cmd))
    _, out = read(s)
    print(f"> {cmd}\n{out}", flush=True)
    time.sleep(0.5)
