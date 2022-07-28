## How to run rhosak (with examples)
```shell script
jbang ./Rhosak.java --help
```
---
## Kafka Login
```shell script
jbang ./Rhosak.java login
```
---
## Kafka management
```shell script
jbang ./Rhosak.java kafka
```
```shell script
jbang ./Rhosak.java kafka list
```
```shell script
jbang ./Rhosak.java kafka create --name [name]
```
```shell script
jbang ./Rhosak.java kafka delete --id [id]
```
```shell script
jbang ./Rhosak.java kafka config
```
---
## Kafka topics management
```shell script
jbang ./Rhosak.java kafka topic create --name [name]
jbang ./Rhosak.java kafka topic list
jbang ./Rhosak.java kafka topic delete --name [name]
```
---
## Service account management
```shell script
jbang ./Rhosak.java service-account create --file-format [json]
jbang ./Rhosak.java service-account list
jbang ./Rhosak.java service-account reset-credentials
jbang ./Rhosak.java service-account describe --id [id]
jbang ./Rhosak.java service-account delete --id [id]
```
---
## Kafka ACL management
In order to deal with ACLs you need to grant admin's privileges to the service account with 'rhoas'  first
```
Ex.: rhoas kafka acl grant-admin --all-accounts
```
Create ACL for topic create operation
```shell script
jbang ./Rhosak.java kafka acl create --permission allow --operation create --topic all --service-account bc949c05-defe-4a86-97b1-4658f41c75e6
```
If you don't specify the service account - the last created/reset account will be loaded from file:
```shell script
jbang ./Rhosak.java kafka acl create --permission allow --operation create --topic topic-test-2801
>>> No principal specified. Trying to load from file ...
```
Create ACL for topic delete operation
```shell script
jbang ./Rhosak.java kafka acl create --permission allow --operation delete --topic topic-test-2801
```
Get ACL list
```shell script
jbang ./Rhosak.java kafka acl list
```
Delete ACLs for topic create/delete operation
```shell script
jbang ./Rhosak.java kafka acl delete --permission allow --operation create --topic topic-test-2801  
jbang ./Rhosak.java kafka acl delete --permission allow --operation delete --topic topic-test-2801  
```
---
### Service registry
```shell
jbang ./Rhosak.java service-registry create --name [name]
```
```shell
jbang ./Rhosak.java service-registry describe --id [id]
```
```shell
jbang ./Rhosak.java service-registry config
```
```shell
jbang ./Rhosak.java service-registry list
```
```shell
jbang ./Rhosak.java service-registry delete --id [id]
```
---
### Service registry artifacts
```shell
jbang ./Rhosak.java service-registry artifact create --file <file> --type [JSON|XML|AVRO|...]
```
```shell
jbang ./Rhosak.java service-registry artifact get --artifact-id 94bfe8da-cc43-44c9-9daf-c9e01beedd9f 
```
```shell
jbang ./Rhosak.java service-registry artifact delete --artifact-id 94bfe8da-cc43-44c9-9daf-c9e01beedd9f
```
```shell
jbang ./Rhosak.java service-registry artifact list
```
```shell
jbang ./Rhosak.java service-registry artifact update --artifact-id 94bfe8da-cc43-44c9-9daf-c9e01beedd9f --file <file>
```
```shell
jbang ./Rhosak.java service-registry artifact download --content-id
jbang ./Rhosak.java service-registry artifact download --global-id
```
NB: `--content-id` and `--global-id` can be obtained from the metadata-get command output (See below).
```shell
jbang ./Rhosak.java service-registry artifact metadata-get --artifact-id 94bfe8da-cc43-44c9-9daf-c9e01beedd9f
```
```shell
jbang ./Rhosak.java service-registry artifact metadata-set --artifact-id 94bfe8da-cc43-44c9-9daf-c9e01beedd9f --name [new name] --description [new description]
```
---