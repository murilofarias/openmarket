### Inicializar postgres

```
docker compose up -d
```

### Compilar aplicação

```
./mvnw clean package
```

### Executar aplicação

```
./mvnw spring-boot:run -Dspring.profiles.active=dev
```

```
java -jar app.jar --spring.profiles.active=dev
```