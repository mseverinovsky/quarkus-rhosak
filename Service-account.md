## Service Account (SA)

SA will be used to create Kafka topic

- Create SA
- Get existing SA (by `client_id` from the `.rhosak-sa.json` file)
    - if the file doesn't exist - create a new SA
    - if SA doesn't exist - create a new SA
- Validate SA. Perform read-only operation with old credentials
- Reset SA credentials in order to get new access topic
