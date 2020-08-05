FROM  www.cserver.com.cn:5000/admin_sys/tomcat8.5.32:v2
WORKDIR /usr/local/tomcat/webapps/
MAINTAINER yarn
COPY  target/ROOT.war /usr/local/tomcat/webapps/ROOT
RUN  /bin/rm -rf  ROOT.war u/
EXPOSE 8080
CMD ["/usr/local/tomcat/bin/catalina.sh","run"]