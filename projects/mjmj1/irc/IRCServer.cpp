#include "IRCServer.h"

IRCServer::IRCServer(boost::asio::io_context& ioContext, int port)
	: m_acceptor(ioContext, boost::asio::ip::tcp::endpoint(boost::asio::ip::tcp::v4(), port))
{
    accept_clients();
}

IRCServer::~IRCServer()
{
}

void IRCServer::accept_clients()
{
	auto clientSocket = make_shared<boost::asio::ip::tcp::socket>(m_acceptor.get_executor());

	m_acceptor.async_accept(*clientSocket, boost::bind(
		&IRCServer::handle_client, this, clientSocket, boost::asio::placeholders::error));
}

void IRCServer::handle_client(shared_ptr<boost::asio::ip::tcp::socket> clientSocket, const boost::system::error_code& error)
{
    if (error)
    {
        printf("Accept failed : %s\n", error.message().c_str());
        return;
    }

    string clientID = "Client" + to_string(reinterpret_cast<uintptr_t>(clientSocket.get()));
    m_clients[clientSocket] = clientID;

    printf("%s connected\n", clientID.c_str());

    auto welcomeMessage = std::make_shared<std::string>("Welcome to the IRC Server, " + clientID + "!\n");
    boost::asio::async_write(*clientSocket, boost::asio::buffer(*welcomeMessage),
        [this, clientSocket, welcomeMessage](const boost::system::error_code&, size_t) {});

    auto buffer = make_shared<std::string>(1024, '\0');
    clientSocket->async_read_some(boost::asio::buffer(*buffer),
        boost::bind(&IRCServer::handle_read, this, clientSocket, buffer,
            boost::asio::placeholders::error, boost::asio::placeholders::bytes_transferred));
        
    accept_clients();
}

void IRCServer::handle_read(shared_ptr<boost::asio::ip::tcp::socket> clientSocket, shared_ptr<string> buffer, const boost::system::error_code& error, size_t bytesTransferred)
{
    if (error)
    {
        if (error != boost::asio::error::eof)
        {
            printf("read error: %s\n", error.message().c_str());
        }

        printf("%s disconnected\n", m_clients[clientSocket].c_str());

        m_clients.erase(clientSocket);

        return;
    }

    buffer->resize(bytesTransferred);
    string message = m_clients[clientSocket] + ": " + *buffer + "\r\n";
    cout << message;

    broadcast_message(message, clientSocket);

    buffer->resize(1024, '\0');
    clientSocket->async_read_some(boost::asio::buffer(*buffer),
        boost::bind(&IRCServer::handle_read, this, clientSocket, buffer,
            boost::asio::placeholders::error, boost::asio::placeholders::bytes_transferred));
}

void IRCServer::broadcast_message(const string& message, shared_ptr<boost::asio::ip::tcp::socket> sender)
{
    for (const auto& client : m_clients)
    {
        if (client.first != sender)
        {
            auto messageCopy = make_shared<string>(message);
            boost::asio::async_write(*client.first, boost::asio::buffer(*messageCopy),
                [messageCopy](const boost::system::error_code&, size_t) {});
        }
    }
}
