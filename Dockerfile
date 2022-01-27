FROM openjdk:8
COPY . /usr/src/LPAdesktop
RUN apt update
RUN apt install maven genisoimage -y

WORKDIR /usr/src/
RUN git clone https://github.com/Truphone/LPAd_SM-DPPlus_Connector.git
WORKDIR /usr/src/LPAd_SM-DPPlus_Connector
RUN mvn install
WORKDIR /usr/src/LPAdesktop
RUN mvn install
RUN chmod +x /usr/src/LPAdesktop/entrypoint.sh
CMD ["/usr/src/LPAdesktop/entrypoint.sh"]
