# Bootiful AI on AWS

Hi, Spring and AWS fans! in this installment industry legend James Ward and his trusty sidekick Josh look at the amazing opportunities marrying Spring AI and AWS Bedrock!

## prompts to `/jlong/inquire`

* ` http POST :8080/42/inquire question=="do you have any neurotic dogs?" `
* ` http POST :8080/42/inquire question=="fantastic. when could i schedule an appointment to adopt Prancer, whose ID is 45, from the London location?" `

## dependencies for `adoptions` 
- `web`
- `pgvector`
- `bedrock-converse`
- `bedrock`
- `devtools`
- `data-jdbc`
- `mcp-client`

## `service`

### properties
- `server.port=8081`
 
### dependencies
- `mcp-server`
- `web`


## properties 

```
spring.ai.bedrock.converse.chat.options.model=amazon.nova-pro-v1:0
spring.ai.bedrock.converse.chat.enabled=true
spring.ai.bedrock.cohere.embedding.enabled=true
spring.ai.vectorstore.pgvector.dimensions=1024
spring.ai.vectorstore.pgvector.initialize-schema=true 
```

## script 

- java is hard! 
- lets use bedrock and spring ai to build something
- james: aws console 
- prancer
- dog adoption agency assistant
- dog, repository
- start.spring.io 
- chatclient 
- no memory? add advisor to enable chat memory 
- system prompt 
- no data, add RAG
- tools
- nice, but lets centralize it with: MCP

