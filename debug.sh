echo subindo em debug

JAVA_HOME="/usr/lib/jvm/java-1.11.0-openjdk-amd64"
echo $JAVA_HOME

./mvnw clean compile assembly:single

java -jar target/api-jpa-1.0-SNAPSHOT-jar-with-dependencies.jar