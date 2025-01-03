#pragma once
#include <boost/asio.hpp>
#include <iostream>
#include <string>
#include <vector>
#include <set>
#include <memory>
#include <mutex>

using namespace std;

class IRCServer
{
public:
	IRCServer(boost::asio::io_context& ioContext, int port);
	~IRCServer();

	void start();

private:
	void accept_clients();
	void handle_client(shared_ptr<boost::asio::ip::tcp::socket> clientSocket);
	void broadcast_message(const string& message, shared_ptr<boost::asio::ip::tcp::socket> sender);
	
	boost::asio::ip::tcp::acceptor m_acceptor;
	set<shared_ptr< boost::asio::ip::tcp::socket>> m_clients;
	mutex m_clientMutex;
};