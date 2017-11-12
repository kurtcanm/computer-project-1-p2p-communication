import select, socket, sys, Queue, time, datetime, ast

# Create a TCP/IP socket
server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server.setblocking(0)

# Bind the socket to the port
server_address = ('', 12000)
print >>sys.stderr, 'starting up on %s port %s' % server_address
server.bind(server_address)

# Listen for max 20 incoming connections
server.listen(20)
# Sockets from which we expect to read
inputs = [ server ]

# Sockets to which we expect to write
outputs = [ ]

# Outgoing message queues (socket:Queue)
message_queues = {}

# Do not block forever (milliseconds)
TIMEOUT = 1000

# Commonly used flag sets definition
READ_ONLY = select.POLLIN | select.POLLPRI | select.POLLHUP | select.POLLERR
READ_WRITE = READ_ONLY | select.POLLOUT


# Set up the poller
poller = select.poll()
poller.register(server, READ_ONLY)

# Map file descriptors to socket objects
fd_to_socket = { server.fileno(): server,}

# Map file descriptors to user names
username_to_fd = {'p2p_server': server.fileno()}
while True:

    events = poller.poll(TIMEOUT)
    client_address = None
    for fd, flag in events:

        # Retrieve the actual socket from its file descriptor
        s = fd_to_socket[fd]

        # Handle inputs
        if flag & (select.POLLIN | select.POLLPRI):

            if s is server:
                # A "readable" server socket is ready to accept a connection
                connection, client_address = s.accept()
                print >> sys.stderr, 'new connection from', client_address, datetime.datetime.now()
                connection.setblocking(0)
                fd_to_socket[connection.fileno()] = connection
                poller.register(connection, READ_ONLY)

                # Give the connection a queue for data that it wants to send
                message_queues[connection] = Queue.Queue()
            else:
                try:
                    socket_data = s.recv(1024)
                    if socket_data:
                        # Convert string to array
                        data = ast.literal_eval(socket_data)
                        if data['request'] == 'login':
                            print data
                            username = data['request_args']
                            print username
                            username_to_fd[username] = fd
                            message = {'from':'p2p_server','to': username, 'request': 'login_result', 'request_args': '',
                                       'status': 'ok'}
                            print events, username_to_fd
                            message_queues[s].put(message)

                        elif data['request'] == 'get_users':
                            username = None
                            connected_users = []
                            for k in username_to_fd:
                                if username_to_fd[k] != fd:
                                    connected_users.append(k)
                                else:
                                    username = k

                            message = {'from': 'p2p_server', 'to': username, 'request': 'users_result',
                                       'request_args': connected_users, 'status': 'ok'}

                            print >> sys.stderr, 'received "%s" from %s' % (data, s.getpeername())
                            message_queues[s].put(message)

                        elif data['request'] == 'send_packet':
                            username = None
                            for k in username_to_fd:
                                if username_to_fd[k] == fd:
                                    username = k
                            if username != 'p2p_server':
                                message = {'request': 'ask_send','from': username,'to': data['request_args'], 'request_args': '', 'status':'ask'}

                                print >> sys.stderr, 'received "%s" from %s' % (data, s.getpeername())
                                message_queues[s].put(message)
                            else:
                                print 'bana geldi aq'

                        elif data['request'] == 'get_packet':
                            #paket sunucu araciligiyle user2 ye gider kabul ederse asagidakini gonderir
                            message = {'request': data['request'], 'from': data['from'], 'to': data['to'],
                                       'status': data['status'], 'request_args': data['request_args'],}

                            print >> sys.stderr, 'received "%s" from %s' % (data, s.getpeername())
                            message_queues[s].put(message)

                        elif data['request'] == 'send_file':
                            username = None
                            for k in username_to_fd:
                                if username_to_fd[k] == fd:
                                    username = k
                            # user2 yukaridaki paketi alir ve  user 1 file'i gonderir
                            message = {'request': data['request'], 'from': username, 'to': data['request_args'],
                                       'status': '', 'request_args': data['file'] }

                            print >> sys.stderr, 'received "%s" from %s' % (data, s.getpeername())
                            message_queues[s].put(message)

                        elif data['request'] == 'get_file':
                            username = None
                            for k in username_to_fd:
                                if username_to_fd[k] == fd:
                                    username = k
                            # sunucu yukaridaki paketi user2 ye gonderir ve user2 alinca cevap doner sunucuya cevap doner
                            message = {'request': data['request'], 'from': username, 'to': data['request_args'],
                                       'status':  data['status'], 'request_args': ''}
                            # A readable client socket has data
                            print >> sys.stderr, 'received "%s" from %s' % (data, s.getpeername())
                            message_queues[s].put(message)

                        else:
                            print 'yattara'
                            # A readable client socket has data
                            print >> sys.stderr, 'received "%s" from %s' % (data, s.getpeername())

                        # Add output channel for response
                        poller.modify(s, READ_WRITE)

                except Exception, e:
                    print e
                else:
                    # there is no data as input
                    pass

        elif flag & select.POLLHUP: # channel is closed, client hung up
            print >> sys.stderr, 'closing', client_address, 'after receiving HUP'
            print events, username_to_fd
            username = None
            for k in username_to_fd:
                if username_to_fd[k] == fd:
                    del username_to_fd[k]

            print events, username_to_fd
            # Stop listening the connection
            poller.unregister(s)
            s.close()

        elif flag & select.POLLOUT: # Socket is ready to send data
            try:
                next_msg = message_queues[s].get_nowait()
            except Queue.Empty:
                # If ther is any messages waiting, then stop checking for writability.
                print >> sys.stderr, 'output queue for', s.getpeername(), 'is empty'
                poller.modify(s, READ_ONLY)
            else:
                print >> sys.stderr, 'sending "%s" to %s' % (next_msg, s.getpeername())
                username = None
                for k in username_to_fd:
                    if username_to_fd[k] == fd:
                        username = k

                try:
                    to_user = username_to_fd[next_msg['to']]
                    if to_user:
                        socsoc = fd_to_socket[to_user]
                        if socsoc:
                            packet = {'from': next_msg['from'], 'request': next_msg['request'],
                                       'request_args': next_msg['request_args'], 'status': next_msg['status']}
                            #next_msg[1] = next_msg[1] + ' ' + str(datetime.datetime.now())
                            print str(packet) + ' gonderilen packet'
                            socsoc.send(str(packet))
                        else:
                            print 'socket yok'
                    else:
                        print 'user yok'
                except Exception,e:
                    print str(e) + str('exception')

        elif flag & select.POLLERR:
            print >> sys.stderr, 'handling exceptional condition for', s.getpeername()
            # Stop listening the connection
            poller.unregister(s)
            s.close()

            # Remove message queue
            del message_queues[s]

    #time.sleep(0.01)