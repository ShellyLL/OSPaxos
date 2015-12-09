# Example Demo:
Type 'help' for a list of commands
help
List of valid commands:
	help - displays this list
	mode <num> - choose mode 1 or mode 2 for different implementation
	init <num> - creates <num> nodes
	read - the current leader accept the client's read request and expect to return to the client
	write <value> - the current leader accept the client's write request and expect to return to the client
	leaderFail - tests leader fail
	voterFail - tests voter fail
	randomFail - Random set a node fail
	leaderRecover - Recover leader node
	voterRecover - Recover leader node
	failRecoverDemo - automatically demo node fail and recover
	leaderStateFail - tests leader fail during progress
	voterStateFail - tests voter fail during progress
mode 2
mode 2 is chosen
init 3
0: I'm Leader
3 nodes created
write j
Proposing for writing: j
0: Send Prepare Request to 1: (1)
0: Send Prepare Request to 2: (1)
1: Received Prepare Request from 0: (1)
2: Received Prepare Request from 0: (1)
0: Received Promise from 1: (1, {0, g})
0: Received Accept Request from 0: {1, j}
0: Accepted from 0: {1, j}
1: Received Accept Request from 0: {1, j}
0: Received Promise from 2: (1, {0, g})
2: Received Accept Request from 0: {1, j}
0: Accepted from 2: {1, j}
Value j is written to datumbase
Time used: 2 msec
1: Received Decision from 0: {1, j}
2: Received Decision from 0: {1, j}
0: Accepted from 1: {1, j}
