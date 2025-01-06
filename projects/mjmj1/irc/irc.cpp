#include "IRCServer.h"

int main()
{
    try
    {
        boost::asio::io_context io_context;
        IRCServer server(io_context, 8080);
        io_context.run();
    }
    catch (const std::exception& e)
    {
        std::cerr << "Exception: " << e.what() << std::endl;
    }

    return 0;
}