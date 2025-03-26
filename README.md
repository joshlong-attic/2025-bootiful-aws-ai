# Bootiful AI on AWS

TODO:
- Put additive config for test schema gen in test application.properties

In `service`
```
./mvnw spring-boot:build-image
```

In `adoptions`
```
export SPRING_AI_BEDROCK_AWS_ACCESS_KEY=YOUR_ACCESS_KEY
export SPRING_AI_BEDROCK_AWS_SECRET_KEY=YOUR_SCREET_KEY

# Run Tests
./mvnw test

# Run Server
./mvnw spring-boot:test-run
```

