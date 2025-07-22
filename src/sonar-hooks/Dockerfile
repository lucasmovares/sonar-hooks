FROM sonarqube:25.7.0.110598-community

# Copy the plugin source code
COPY . /tmp/plugin-src/

# Switch to root to install Maven, Git and JDK
USER root

# Install Maven, Git, and OpenJDK
RUN apt-get update && \
    apt-get install -y openjdk-11-jdk maven git unzip && \
    rm -rf /var/lib/apt/lists/*

# Set JAVA_HOME for Maven
ENV JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64

# Build the plugin
WORKDIR /tmp/plugin-src
RUN mvn clean package

# Copy the built plugin to the plugins directory
RUN cp target/sonar-hooks-plugin-1.0.0.jar /opt/sonarqube/extensions/plugins/

# Also download the community branch plugin
RUN wget -O /opt/sonarqube/extensions/plugins/sonarqube-community-branch-plugin-25.6.0.jar \
    https://github.com/mc1arke/sonarqube-community-branch-plugin/releases/download/25.6.0/sonarqube-community-branch-plugin-25.6.0.jar

# Download the webapp for the community branch plugin
RUN mkdir -p /tmp/webapp && \
    wget -O /tmp/sonarqube-webapp.zip https://github.com/mc1arke/sonarqube-community-branch-plugin/releases/download/25.6.0/sonarqube-webapp.zip && \
    unzip -o /tmp/sonarqube-webapp.zip -d /opt/sonarqube/web && \
    rm -f /tmp/sonarqube-webapp.zip

# Clean up only the source code, not the build tools
RUN rm -rf /tmp/plugin-src

# Restore working directory to SonarQube default
WORKDIR /opt/sonarqube

# Switch back to sonarqube user
USER sonarqube

# Set environment variables for the community branch plugin
ENV SONAR_WEB_JAVAOPTS="-javaagent:/opt/sonarqube/extensions/plugins/sonarqube-community-branch-plugin-25.6.0.jar=web"
ENV SONAR_CE_JAVAOPTS="-javaagent:/opt/sonarqube/extensions/plugins/sonarqube-community-branch-plugin-25.6.0.jar=ce" 