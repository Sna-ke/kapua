###############################################################################
# Copyright (c) 2016, 2017 Red Hat Inc and others
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
###############################################################################

#!/usr/bin/env bash

set -e

# print error and exit when necessary
die() { printf "$@" "\n" 1>&2 ; exit 1; }

#minishift start
#eval $(minishift docker-env)

# Setup Parameters
OPENSHIFT_PROJECT_NAME="eclipse-kapua"
DOCKER_ACCOUNT=hekonsek
DOCKER_IMAGE_KAPUA_API=${DOCKER_ACCOUNT}/kapua-api:latest
DOCKER_IMAGE_KAPUA_SQL=${DOCKER_ACCOUNT}/kapua-sql
DOCKER_IMAGE_KAPUA_BROKER=${DOCKER_ACCOUNT}/kapua-broker:latest
#DOCKER_IMAGE_KAPUA_BROKER=redhatiot/kapua-broker:latest
#DOCKER_IMAGE_KAPUA_BROKER=bcgovbrian/kapua-broker
DOCKER_IMAGE_KAPUA_CONSOLE=${DOCKER_ACCOUNT}/kapua-console:latest
DOCKER_IMAGE_KAPUA_LIQUBASE=${DOCKER_ACCOUNT}/kapua-liquibase:latest

# test if the project is already created ... fail otherwise 

oc describe "project/$OPENSHIFT_PROJECT_NAME" &>/dev/null || oc new-project "$OPENSHIFT_PROJECT_NAME" --description="Open source IoT Platform" --display-name="Eclipse Kapua (Dev)"

#oc login
#oc new-project "$OPENSHIFT_PROJECT_NAME" --description="Open source IoT Platform" --display-name="Eclipse Kapua"

#if [ -z "${DOCKER_ACCOUNT}" ]; then
#  DOCKER_ACCOUNT=kapua
#fi

echo Creating ElasticSearch server...

if [ -z "${ELASTIC_SEARCH_MEMORY}" ]; then
  ELASTIC_SEARCH_MEMORY=512M
fi

oc new-app -e ES_JAVA_OPTS="-Xms${ELASTIC_SEARCH_MEMORY} -Xmx${ELASTIC_SEARCH_MEMORY}" elasticsearch:2.4 -n "$OPENSHIFT_PROJECT_NAME"

echo ElasticSearch server created

### SQL database

echo Creating SQL database

oc new-app ${DOCKER_IMAGE_KAPUA_SQL} --name=sql -n "$OPENSHIFT_PROJECT_NAME"
oc set probe dc/sql --readiness --open-tcp=3306

echo SQL database created

### Broker

echo Creating broker

oc new-app ${DOCKER_IMAGE_KAPUA_BROKER} -name=kapua-broker -n "$OPENSHIFT_PROJECT_NAME" \
   '-eACTIVEMQ_OPTS=-Dcommons.db.connection.host=$SQL_SERVICE_HOST -Dcommons.db.connection.port=$SQL_SERVICE_PORT_3306_TCP -Dcommons.db.schema='
oc set probe dc/kapua-broker --readiness --initial-delay-seconds=15 --open-tcp=1883

echo Broker created

## Build assembly module with
## mvn -Pdocker

echo Creating web console

oc new-app ${DOCKER_IMAGE_KAPUA_CONSOLE} -n "$OPENSHIFT_PROJECT_NAME" \
   '-eCATALINA_OPTS=-Dcommons.db.connection.host=$SQL_SERVICE_HOST -Dcommons.db.connection.port=$SQL_SERVICE_PORT_3306_TCP -Dcommons.db.schema= -Dbroker.host=$KAPUA_BROKER_SERVICE_HOST'
oc set probe dc/kapua-console --readiness --liveness --initial-delay-seconds=30 --timeout-seconds=10 --get-url=http://:8080/console

echo Web console created

### REST API

echo 'Creating Rest API'

oc new-app ${DOCKER_IMAGE_KAPUA_API} -n "$OPENSHIFT_PROJECT_NAME" \
   '-eCATALINA_OPTS=-Dcommons.db.connection.host=$SQL_SERVICE_HOST -Dcommons.db.connection.port=$SQL_SERVICE_PORT_3306_TCP -Dcommons.db.schema= -Dbroker.host=$KAPUA_BROKER_SERVICE_HOST'
oc set probe dc/kapua-api --readiness --liveness --initial-delay-seconds=30 --timeout-seconds=10 --get-url=http://:8080/api

echo 'Rest API created'

## Applying DB schema

# Create batch job for liquibase
oc set image -f liquibase_job.yml "liquibase=$DOCKER_IMAGE_KAPUA_LIQUBASE" --local --source=docker -o yaml | oc create -f -

## Start router

#oc adm policy add-scc-to-user hostnetwork -z router
#oc adm router --create

## Expose web console

oc expose svc/kapua-console
oc expose svc/kapua-api
oc expose svc/kapua-broker --port 61614

