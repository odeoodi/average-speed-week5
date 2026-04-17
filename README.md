This project is a Java‑based Average Speed Calculator implemented as a lightweight HTTP server.
The application is:

- tested and analyzed using SonarQube
- packaged as a Docker container
- deployed and run in Kubernetes (Minikube)
- accessed locally using port‑forwarding

📌## Features

- Simple HTTP server using HttpServer
- Average speed calculation (distance / time)
- Input validation and error handling
- Unit tests with JUnit 5 and Mockito
- Code coverage with JaCoCo
- Static code analysis with SonarQube
- Containerized with Docker
- Deployed in Kubernetes (Minikube)

🔍## SonarQube Configuration
Create sonar-project.properties in the project root:
````xml
sonar.projectKey=avg_consol
sonar.projectName=Average Speed App
sonar.projectVersion=1.0

sonar.sources=src/main/java
sonar.tests=src/test/java
sonar.java.binaries=target/classes

sonar.junit.reportPaths=target/surefire-reports
sonar.jacoco.reportPaths=target/jacoco.exec
sonar.sourceEncoding=UTF-8
````
## Run analysis
```
mvn clean test
mvn sonar:sonar
```
Ensures >80% test coverage and clean‑code compliance.

🐳## Docker
### Dockerfile (multi‑stage)

```xml
# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/avgspd1_pod.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```
## Build Docker image (Minikube)
```
minikube docker-env | Invoke-Expression
docker build --no-cache -t avgspeed-pod-app:1.0 .
```
☸ ## Kubernetes (Minikube)
### Deployment YAML (interactive pod)
Create avgspeed1-app.yaml:
```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: avgspeed1-app
spec:
  replicas: 1
  selector:
    matchLabels:
      app: avgspeed1-app
  template:
    metadata:
      labels:
        app: avgspeed1-app
    spec:
      containers:
        - name: avgspeed1-app
          image: avgspeed-pod-app:1.0
          imagePullPolicy: Never
          ports:
            - containerPort: 8081
          env:
            - name: JAVA_TOOL_OPTIONS
              value: "-Djava.awt.headless=true -Dprism.order=sw"

```
### Deploy to Minikube

```
kubectl apply -f avgspeed1-app.yaml
kubectl get pods
```
✅ ### Pod status should be:
```
avgspeed1-app-xxxxx   1/1   Running
```
RUN the pod
```
 kubectl port-forward pod/avgspeed1-app-7784fd9f6c-kj9b2 8081:8081
```
open browser
```
http://localhost:8081
```
📜 ### Logs & Maintenance
View logs
```
kubectl logs -l app=avgspeed1-app
```
Restart application
```
kubectl rollout restart deployment avgspeed1-app
```
Clean up
```
kubectl delete deployment avgspeed1-app
```
-------------------------------------
⚠️## Important Notes

- One Docker image per application
- Do not reuse images for JavaFX/FXML apps
- Avoid latest tags during development
- GUI (FXML/JavaFX) applications should not run in Kubernetes
- Always launch Java apps with java -jar
----------------------------------------------

📚## Technologies Used

- Java 21
- Maven
- JUnit 5 & Mockito
- JaCoCo
- SonarQube
- Docker
- Kubernetes (Minikube)

