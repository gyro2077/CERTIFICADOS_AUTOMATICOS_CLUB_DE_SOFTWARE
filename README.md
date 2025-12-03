# 🎓 Generador de Certificados Automáticos (Spring Boot + JasperReports)

Este proyecto es una API REST construida con **Spring Boot** que genera certificados en formato PDF automáticamente utilizando **JasperReports**. Los datos de los participantes se extraen directamente de una base de datos **PostgreSQL**.

## 🚀 Características

*   **Generación PDF**: Crea certificados de alta calidad listos para imprimir o enviar.
*   **Datos Dinámicos**: Extrae nombre, carrera, correo, etc., desde la base de datos.
*   **Fuentes Nativas**: Utiliza fuentes estándar de PDF (Helvetica) para evitar problemas de instalación de fuentes en servidores Linux.
*   **Imágenes Incrustadas**: Soporta imágenes de fondo y logotipos mediante rutas absolutas.

---

## 🛠️ Requisitos Previos

*   **Java 17** (JDK)
*   **Maven** (para gestión de dependencias)
*   **PostgreSQL** (Base de datos con la tabla `profiles`)

---

## 📦 Estructura del Proyecto y Dependencias (`pom.xml`)

El archivo `pom.xml` gestiona las librerías necesarias:

1.  **`spring-boot-starter-webmvc`**: Permite crear la API REST y el controlador web.
2.  **`spring-boot-starter-jdbc`**: Proporciona la conexión a la base de datos (DataSource).
3.  **`postgresql`**: Driver JDBC para conectar con tu base de datos PostgreSQL.
4.  **`jasperreports` (v6.21.0)**: El motor de reportes que compila el XML (`.jrxml`) y genera el PDF.

---

## ⚙️ Configuración

### 1. Base de Datos (`application.properties`)
Configura tus credenciales en `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://tu-host:5432/tu-base-de-datos
spring.datasource.username=tu-usuario
spring.datasource.password=tu-contraseña
spring.datasource.driver-class-name=org.postgresql.Driver
```

### 2. Plantilla del Certificado (`.jrxml`)
El diseño está en `src/main/resources/Blank_A4_Landscape.jrxml`.

**⚠️ IMPORTANTE: Rutas de Imágenes**
JasperReports requiere **rutas absolutas** para las imágenes cuando se ejecuta en este entorno. Asegúrate de que la etiqueta `<imageExpression>` apunte al archivo correcto en tu disco:

```xml
<imageExpression><![CDATA["/home/gyro/Documents/CLUB/CERTIFICADOS_AUTOMATICOS/src/main/resources/Blue and White Completion Certificate.png"]]></imageExpression>
```

### 3. El "Truco" de las Fuentes (Sin instalar nada)
Para evitar errores de "Font not found" en Linux sin instalar fuentes `.ttf` extra, usamos las fuentes internas del estándar PDF:

```xml
<font fontName="SansSerif" 
      pdfFontName="Helvetica-BoldOblique" 
      isPdfEmbedded="false"/>
```
*   `pdfFontName`: Le dice al PDF que use su fuente interna (ej. Helvetica, Times-Roman).
*   `isPdfEmbedded="false"`: No intenta incrustar un archivo de fuente, ahorrando peso y errores.

---

## 🏃‍♂️ Cómo Ejecutar

1.  Abre una terminal en la raíz del proyecto.
2.  Ejecuta el comando:

```bash
mvn spring-boot:run
```

El servidor iniciará en el puerto **8080**.

---

## 🔌 Uso de la API

### Generar Certificados
Para descargar el PDF con todos los certificados generados:

*   **URL**: `http://localhost:8080/generar-certificados`
*   **Método**: `GET`
*   **Respuesta**: Archivo `certificados.pdf`

**Ejemplo con cURL:**
```bash
curl -o mis_certificados.pdf http://localhost:8080/generar-certificados
```

---

## 📂 Explicación del Código

### `ReportController.java`
Es el cerebro de la operación.
1.  **`@RestController`**: Lo marca como un controlador web.
2.  **`DataSource`**: Inyecta la conexión a la base de datos automáticamente.
3.  **`generarReporte()`**:
    *   Carga el archivo `.jrxml` desde `resources`.
    *   Compila el reporte en tiempo de ejecución (`JasperCompileManager`).
    *   Llena el reporte (`JasperFillManager`) pasando la conexión a la DB.
    *   Exporta el resultado a bytes PDF (`JasperExportManager`).
    *   Devuelve un `ResponseEntity<byte[]>` para que el navegador descargue el archivo.

### `Blank_A4_Landscape.jrxml`
Es el archivo XML que define el diseño visual. Contiene:
*   **Query SQL**: `SELECT ... FROM profiles ...`
*   **Bandas**: `Detail` (se repite por cada usuario).
*   **Elementos**: `<image>` (fondo), `<textField>` (texto dinámico).

---

## 🆘 Solución de Problemas Comunes

1.  **Error `Invalid UUID`**: Asegúrate de que todos los elementos en el XML tengan un UUID válido (formato `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx`).
2.  **Imagen no aparece**: Verifica que la ruta en `<imageExpression>` sea absoluta y el archivo exista.
3.  **Puerto ocupado**: Si falla al iniciar, asegúrate de que nada esté usando el puerto 8080 o mata el proceso anterior (`lsof -ti:8080 | xargs kill -9`).
