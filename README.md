# Automatización E2E Liverpool - Playwright con Java

Proyecto de automatización de pruebas para el flujo de búsqueda, filtrado y validación de red en **Liverpool México**, desarrollado con **Java 17**, **Playwright**, **TestNG** y el patrón de diseño **Page Object Model (POM)**.

---

## Resumen del Flujo Automatizado

1. **Parte 1 (Automatización UI E2E):**
   - Navega a la tienda en línea de Liverpool.
   - Realiza la búsqueda del producto `"playstation 5"`.
   - Aplica el filtro de color `"Blanco"`.
   - Ordena los resultados por `"Menor precio"`.
   - Extrae el nombre y precio de los primeros 5 productos mostrados en la pantalla y los imprime en la consola.

2. **Parte 2 (Intercepción de Red y Validación Cruzada):**
   - Intercepta la respuesta HTTP/JSON que consume el frontend desde la API del catálogo (`/api/plp/search`).
   - Parsea el contenido JSON y extrae la lista de productos devuelta por el servidor.
   - Realiza una validación cruzada comparando los productos visibles en pantalla contra los devueltos por la API.
   - Comprueba mediante aserción que al menos 3 de los 5 productos de la UI aparezcan en la respuesta del backend (obteniendo 5 de 5 coincidencias exitosas).
   - Registra cualquier discrepancia de precio o ajuste en títulos.

3. **Parte 3 (Reportes y CI/CD):**
   - Captura automática de pantalla a pantalla completa ante cualquier fallo mediante un Listener de TestNG (`OyentePruebas.java`), sin código manual dentro de los tests.
   - Pipeline de Integración Continua configurado en GitHub Actions (`.github/workflows/test.yml`).

4. **Parte 4 (Documento de Estrategia):**
   - Documento (TEST_STRATEGY.md) con el análisis de qué no automatizar, mitigación de CAPTCHAs, manejo de inestabilidad (*flakiness*) y escalamiento a 50+ suites de prueba.

---

## Tecnologías y Dependencias

- **Lenguaje:** Java 17
- **Framework de Automatización:** Microsoft Playwright for Java (1.49.0)
- **Test Runner:** TestNG (7.10.2)
- **Procesamiento JSON:** Jackson Databind (2.18.2)
- **Aserciones:** AssertJ Core (3.27.0)
- **Gestor de Construcción:** Apache Maven

---

## Instalación y Ejecución Local

### 1. Prerrequisitos
- Tener instalado **Java 17** o superior.
- Tener instalado **Maven** y **Git** (o Eclipse IDE).

### 2. Clonar el repositorio
```bash
git clone https://github.com/GilMartinez10/liverpool-automation.git
cd liverpool-automation
```

### 3. Instalar navegadores de Playwright (solo la primera vez)
```bash
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install chromium"
```

---

## Modos de Ejecución

### Opción A: Modo HEADLESS (Por defecto para servidores y CI/CD)
Ejecuta las pruebas en segundo plano sin abrir ventana gráfica:
```bash
mvn clean test
```

### Opción B: Modo HEADED (Ventana visible de Chrome)
Ejecuta las pruebas abriendo la ventana del navegador para observar la navegación:
```bash
mvn test -Dheaded=true
```

### Opción C: Ejecución desde Eclipse IDE
1. Importa el proyecto: `File > Import > Existing Maven Projects`.
2. Ubica la clase `src/test/java/com/liverpool/pruebas/LiverpoolE2ETest.java`.
3. Haz clic derecho sobre el archivo > `Run As` > `Java Application` (o `TestNG Test`).

---

## Reportes y Capturas de Evidencias

- **Reportes HTML:** Se generan automáticamente en la carpeta `target/surefire-reports/index.html`.
- **Capturas automáticas en fallos:** Si una prueba falla, el framework captura la pantalla y la almacena en: `target/screenshots/`.

---

# Integración Continua (GitHub Actions)

El archivo `.github/workflows/test.yml` ejecuta automáticamente las pruebas en servidores Ubuntu en cada *push*:
- Instala Java 17 y las dependencias de Linux para Chromium.
- Ejecuta toda la suite en modo headless.
- Sube los reportes y capturas de pantalla como artefactos descargables de la ejecución.



## Entrega
- **Repositorio:** `https://github.com/GilMartinez10/liverpool-automation.git`
- **Documento de Estrategia:** (TEST_STRATEGY.md)
