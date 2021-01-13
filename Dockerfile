FROM vertx/vertx4
ENV VERTX_VERSION 4.0.0

ENV VERTX_EFFECT_VERSION 1.0.0-RC5
#dependency of vertx-effect
ENV JSON_VALUES_VERSION 9.0.0-RC1
#depenencies of json-values.
ENV VAVR_VERSION 0.10.3
ENV DSL_JSON_VERSION 1.9.8

ENV VERTX_EFFECT_URL https://repo1.maven.org/maven2/com/github/imrafaelmerino/vertx-effect/${VERTX_EFFECT_VERSION}/vertx-effect-${VERTX_EFFECT_VERSION}.jar
ENV JSON_VALUES_URL https://repo1.maven.org/maven2/com/github/imrafaelmerino/json-values/${JSON_VALUES_VERSION}/json-values-${JSON_VALUES_VERSION}.jar
ENV VAVR_URL https://repo1.maven.org/maven2/io/vavr/vavr/${VAVR_VERSION}/vavr-${VAVR_VERSION}.jar
ENV DSL_JSON_URL https://repo1.maven.org/maven2/com/dslplatform/dsl-json/${DSL_JSON_VERSION}/dsl-json-${DSL_JSON_VERSION}.jar

RUN curl -L ${VERTX_EFFECT_URL} > /usr/local/vertx/lib/vertx-effect-${VERTX_EFFECT_VERSION}.jar
RUN curl -L ${JSON_VALUES_URL} > /usr/local/vertx/lib/json-values-${JSON_VALUES_VERSION}.jar
RUN curl -L ${DSL_JSON_URL} > /usr/local/vertx/lib/dsl-json-${DSL_JSON_VERSION}.jar
RUN curl -L ${VAVR_URL} > /usr/local/vertx/lib/vavr-${VAVR_VERSION}.jar

#vertx image uses java 8 and vertx-effec uses java 11.
FROM openjdk:11
COPY --from=0 /usr/local/vertx/ /usr/local/vertx
COPY startup.jsh .
ENV PATH $PATH:/usr/local/vertx/bin

CMD ["jshell","-v","--startup","startup.jsh","--class-path","/usr/local/vertx/lib/*"]



