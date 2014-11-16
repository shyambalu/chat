######Java chat application

Clone repo then run 

```
./gradlew build
```

Run server
```
./gradlew :chat_server:run
```

Run client, multiple clients can be spawned
```
./gradlew :chat_client:run
```

Alternatively you can run the below command and find the binaries in the build/install/ folder of chat_server and chat_client
```
./gradlew installApp
```
