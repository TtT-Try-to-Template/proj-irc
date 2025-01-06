#pragma once
#include <boost/asio.hpp>
#include <boost/bind.hpp>
#include <iostream>
#include <string>
#include <map>
#include <memory>

using namespace std;

class IRCServer
{
public:
	IRCServer(boost::asio::io_context& ioContext, int port);
	~IRCServer();

private:
	void accept_clients();
	void handle_client(shared_ptr<boost::asio::ip::tcp::socket> clientSocket, const boost::system::error_code& error);
	void handle_read(shared_ptr<boost::asio::ip::tcp::socket> clientSocket, shared_ptr<string> buffer, const boost::system::error_code& error, size_t bytesTransferred);
	void broadcast_message(const string& message, shared_ptr<boost::asio::ip::tcp::socket> sender);
	
	boost::asio::ip::tcp::acceptor m_acceptor;
	map<shared_ptr<boost::asio::ip::tcp::socket>, string> m_clients;
};