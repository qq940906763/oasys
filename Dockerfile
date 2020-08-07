FROM primetoninc/jdk:1.8
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
RUN echo 'Asia/Shanghai' >/etc/timezone
EXPOSE 8080
VOLUME /tmp
ADD target/oasys-0.0.1-SNAPSHOT.jar oasys.jar
CMD ["java", "-Xmx666m", "-jar", "oasys.jar"]