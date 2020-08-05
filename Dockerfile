FROM  touchvie/tomcat8-jdk8
RUN rm -rf /usr/local/tomcat/webapps/*
copy  target/ROOT.war  /usr/local/tomcat/webapps
RUN unzip /usr/local/tomcat/webapps/ROOT.war -d /usr/local/tomcat/webapps/ROOT
RUN rm /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
CMD ["/usr/local/tomcat/bin/catalina.sh","run"]