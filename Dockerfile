# Setting the Docker image
FROM openjdk:16

# Creating a new directory inside the app
RUN mkdir /app

# Copy app files from host machine to imagem filesystem
COPY target/cinephiles*.jar /app/cinephiles.jar

# Set the directory for future commands
WORKDIR /app

# Run CinephilesApplication as a defautl command/instruction for the container
CMD java -jar cinephiles.jar
