FROM actor/base-java:latest
MAINTAINER Steve Kite <steve@actor.im>

ADD build/docker/bin/* /opt/actor-keygen/bin/
ADD build/docker/lib/* /opt/actor-keygen/lib/

WORKDIR "/keygen"

CMD ["/opt/actor-keygen/bin/actor-keygen"]