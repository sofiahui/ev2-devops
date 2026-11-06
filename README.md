# EP2 DevOps - Pipeline CI/CD

Integrantes: Sofía Huichulef - Bastián Millañir
Docente: [Nombre]

## El proyecto

Tenemos dos microservicios. ms-productos está hecho en Java con Spring Boot y maneja los productos y el stock. ms-pedidos está hecho en Python con FastAPI y crea los pedidos, para eso le pregunta a ms-productos si hay stock y lo descuenta.

Delante de los dos hay un nginx que hace de gateway, así que todo entra por el puerto 8080.

## Contenedores

Cada microservicio tiene su Dockerfile. El de Java compila en una etapa y después copia solo el jar a una imagen más liviana.

## Pipeline

El pipeline está en .github/workflows/ci-cd.yml y corre cada vez que hacemos push o un pull request a main.

Si todo pasa, se construyen las imágenes Docker y se suben a GitHub Container Registry.

Al final se hace el despliegue con Docker Compose dentro de GitHub Actions y se prueban los endpoints con curl para ver que todo responde.

## Calidad

Pusimos como regla que la cobertura de los tests no puede bajar de 80%. Si baja, el pipeline falla. En Java eso está en el pom.xml y en Python en el pytest.ini.

## Seguridad

Dependabot revisa una vez por semana si hay dependencias nuevas y abre pull requests solo.

Snyk revisa las dependencias y el código. Si encuentra algo alto o crítico, el pipeline se bloquea, no se construye ni se despliega nada y se crea un issue avisando.

Nos pasó en la primera ejecución, Snyk encontró vulnerabilidades en Spring Boot. Actualizamos la versión, después apareció otra en jackson-databind, la corregimos y recién ahí el pipeline pasó completo.

El token de Snyk está guardado en los secrets de GitHub.

## Trazabilidad

Cada cambio queda en un commit y cada commit dispara el pipeline, que guarda sus logs y reportes. Las imágenes se etiquetan con el SHA del commit, así sabemos de qué código salió cada una. Cuando la seguridad falla queda un issue, y lo cerramos indicando el commit que lo arregló.

## Docker Compose

En el docker-compose.yml está el gateway, ms-productos y dos réplicas de ms-pedidos. Los servicios esperan a que el otro esté sano antes de partir, tienen límite de CPU y memoria y se reinician solos si se caen.

Los datos se guardan en memoria, así que cada réplica de ms-pedidos tiene sus propios pedidos.

## Cómo ejecutarlo

```bash
docker compose up -d --build
curl http://localhost:8080/api/productos
docker compose down
```

## Uso de IA

Usamos Claude como apoyo para armar el código y entender algunos errores del pipeline.

## Reflexiones

Bastián Millañir: Anteriormente ya había creado CI/CD para despliegue automatizado, pero nunca me ha servido por completo el proceso al primer intento. Siempre me he encontrado con errores y esta vez no fue la excepción. Aprendí a resolver problemas de seguridad y a configurar correctamente el pipeline para que funcione de manera eficiente.

Sofía Huichulef: En mi caso, nunca había trabajado con CI/CD y fue un desafío aprender a configurar el pipeline y entender cómo funcionan los microservicios. Aprendí sobre la importancia de la cobertura de los archivos de tests y cómo automatizar el despliegue con actions de github. Además no conocía la herramienta Snyk y me pareció útil para mantener la seguridad de las dependencias y del código. Dependabot también me pareció una herramienta útil para mantener las dependencias actualizadas, lo había visto antes en códigos y repositorios pero nunca había trabajado con esa herramienta.