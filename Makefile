compile:
	find ./src -name '*.java' | xargs echo javac -d bin
	jar cvfe YourJar.jar main.Main -C bin .

default:
	compile