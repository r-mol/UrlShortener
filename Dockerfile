# Use a base image with Scala and sbt pre-installed
FROM hseeberger/scala-sbt:8u302_1.5.5_2.13.6

# Set the working directory inside the container
WORKDIR /app

# Copy the project files to the container
COPY . .

# Build the project
RUN sbt compile

# Expose the server's port
EXPOSE 8080

# Run the project
CMD sbt run