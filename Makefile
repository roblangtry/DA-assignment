
compile_server:
	find ./src -name '*.java' | xargs javac -d bin

jar_server:
	jar cvfe server.jar main.Main -C bin .

server: compile_server jar_server


compile_client:
	javac client/Client.java

jar_client:
	jar cvfe client.jar client.Client client

client: compile_client jar_client

default: client