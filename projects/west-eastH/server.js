const net = require('net');

if (process.argv.length !== 3) {
    console.error("Wrong number of arguments");
    process.exit(1);
}

const port = parseInt(process.argv[2]);
if (isNaN(port)) {
    console.error("Wrong number of arguments");
    process.exit(1);
}

const server = net.createServer();
const clients = new Map();
let clientIdCounter = 0;

function broadcastMessage(message, excludeSocket = null) {
    for (const [id, socket] of clients.entries()) {
        if (socket !== excludeSocket) {
            try {
                socket.write(message);
            } catch (err) {
                console.error("Failed to send message to client:", id);
            }
        }
    }
}

server.on('connection', (socket) => {
    const clientId = clientIdCounter++;
    clients.set(clientId, socket);

    const arrivalMessage = `server: client ${clientId} just arrived\n`;
    broadcastMessage(arrivalMessage);

    console.log(`Client ${clientId} connected`);

    let buffer = "";

    socket.on('data', (data) => {
        buffer += data.toString();

        const messages = buffer.split('\n');
        buffer = messages.pop();

        for (const msg of messages) {
            if (msg.trim()) {
                const fullMessage = `client ${clientId}: ${msg}\n`;
                broadcastMessage(fullMessage, socket);
            }
        }
    });

    socket.on('end', () => {
        clients.delete(clientId);

        const departureMessage = `server: client ${clientId} just left\n`;
        broadcastMessage(departureMessage);

        console.log(`Client ${clientId} disconnected`);
    });

    socket.on('error', (err) => {
        console.error(`Error with client ${clientId}:`, err.message);
    });
});

server.on('error', (err) => {
    console.error("Fatal error");
    process.exit(1);
});

server.listen(port, '0.0.0.0', () => {
    console.log(`Server listening on 0.0.0.0:${port}`);
});
