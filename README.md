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
