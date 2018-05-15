.DEFAULT_GOAL := all
compile_server:
	@echo 'Compiling Server'
	@find ./server -name '*.java' | xargs javac -d bin

jar_server:
	@echo 'Creating server.jar'
	@jar cvfe server.jar main.Main -C bin .

server: compile_server jar_server


compile_client:
	@echo 'Compiling Client'
	@javac client/Client.java

jar_client:
	@echo 'Creating client.jar'
	@jar cvfe client.jar client.Client client

client: compile_client jar_client

all: client server

