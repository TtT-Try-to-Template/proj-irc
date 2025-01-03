#include "IRCServer.h"

IRCServer::IRCServer(boost::asio::io_context& ioContext, int port)
	: m_acceptor(ioContext, boost::asio::ip::tcp::endpoint(boost::asio::ip::tcp::v4(), port))
{

}

IRCServer::~IRCServer()
{
}

void IRCServer::start()
{
}

void IRCServer::accept_clients()
{
}

void IRCServer::handle_client(shared_ptr<boost::asio::ip::tcp::socket> clientSocket)
{
}

void IRCServer::broadcast_message(const string& message, shared_ptr<boost::asio::ip::tcp::socket> sender)
{
}
