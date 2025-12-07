# Guía de Despliegue en Render

Esta guía te ayudará a desplegar tu aplicación Spring Boot de certificados en Render usando Docker.

## 📋 Prerrequisitos

- [ ] Cuenta en [Render](https://render.com)
- [ ] Repositorio en GitHub o GitLab con tu código
- [ ] Los archivos `Dockerfile` y `application.yaml` actualizados (ya están listos en tu proyecto)

## 🗄️ Paso 1: Crear Base de Datos PostgreSQL

1. Inicia sesión en [Render Dashboard](https://dashboard.render.com)
2. Haz clic en **New +** → **PostgreSQL**
3. Configura la base de datos:
   - **Name**: `certificados-db` (o el nombre que prefieras)
   - **Database**: `certificados` (nombre de la base de datos)
   - **User**: Se genera automáticamente
   - **Region**: Elige la más cercana (ej. Ohio)
   - **Instance Type**: Selecciona **Free** (válido por 90 días)
4. Haz clic en **Create Database**
5. **Importante**: Guarda la **Internal Database URL** que aparece en la página de detalles. Tiene este formato:
   ```
   postgresql://usuario:contraseña@hostname/database
   ```

> [!NOTE]
> La base de datos gratuita de Render expira después de 90 días. Para persistencia permanente gratuita, considera usar [Neon](https://neon.tech) o [Supabase](https://supabase.com) y conectar la URL externa.

## 🚀 Paso 2: Crear Web Service

1. En el Dashboard de Render, haz clic en **New +** → **Web Service**
2. Conecta tu repositorio:
   - Autoriza a Render para acceder a tu GitHub/GitLab
   - Selecciona el repositorio `CERTIFICADOS_AUTOMATICOS`
3. Configura el servicio:
   - **Name**: `certificados-app` (o el nombre que prefieras)
   - **Region**: Elige la misma región que tu base de datos
   - **Branch**: `main` (o la rama que uses)
   - **Root Directory**: Déjalo vacío si el `Dockerfile` está en la raíz del proyecto
   - **Runtime**: Selecciona **Docker**
   - **Instance Type**: Selecciona **Free**

## 🔐 Paso 3: Configurar Variables de Entorno

En la sección **Environment Variables** del servicio, añade las siguientes variables:

| Key | Value | Descripción |
|-----|-------|-------------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://hostname:5432/database` | Convierte la Internal DB URL a formato JDBC |
| `SPRING_DATASOURCE_USERNAME` | `tu_usuario` | Usuario de la base de datos |
| `SPRING_DATASOURCE_PASSWORD` | `tu_contraseña` | Contraseña de la base de datos |
| `PORT` | `8080` | Puerto del servidor (opcional, Render lo inyecta automáticamente) |

### 📝 Cómo convertir la URL de PostgreSQL a JDBC

Si tu **Internal Database URL** de Render es:
```
postgresql://user:pass@dpg-xxxxx.oregon-postgres.render.com/dbname
```

Conviértela a formato JDBC así:
```
jdbc:postgresql://dpg-xxxxx.oregon-postgres.render.com/dbname
```

**Ejemplo completo**:
- **SPRING_DATASOURCE_URL**: `jdbc:postgresql://dpg-abc123.oregon-postgres.render.com/certificados`
- **SPRING_DATASOURCE_USERNAME**: `certificados_user`
- **SPRING_DATASOURCE_PASSWORD**: `xYz789AbC...`

## 🎯 Paso 4: Desplegar

1. Haz clic en **Create Web Service**
2. Render comenzará a:
   - Clonar tu repositorio
   - Construir la imagen Docker (esto puede tardar 3-5 minutos)
   - Iniciar tu aplicación
3. Monitorea los logs en tiempo real para ver el progreso

## ✅ Paso 5: Verificar el Despliegue

1. Una vez que el deploy esté completo, Render te dará una URL como:
   ```
   https://certificados-app.onrender.com
   ```
2. Accede a tu aplicación en esa URL
3. Verifica que la aplicación responde correctamente

## 🔍 Solución de Problemas

### La aplicación no inicia

**Revisa los logs** en el Dashboard de Render. Busca errores comunes:

- **Error de conexión a base de datos**: Verifica que las variables de entorno estén correctas
- **Puerto incorrecto**: Asegúrate de que `application.yaml` usa `${PORT:8080}`
- **Falta de dependencias**: Verifica que el `Dockerfile` copie correctamente los archivos

### Cold Starts (Arranques en Frío)

> [!WARNING]
> En el plan gratuito, Render "duerme" tu servicio después de **15 minutos de inactividad**. La primera petición después de dormir tardará **30-50 segundos** en responder mientras Spring Boot se inicia.

### Logs útiles

Para ver los logs en tiempo real:
1. Ve a tu servicio en el Dashboard
2. Haz clic en la pestaña **Logs**
3. Busca mensajes como:
   ```
   Started DemoApplication in X.XXX seconds
   Tomcat started on port(s): 8080
   ```

## 📚 Recursos Adicionales

- [Documentación oficial de Render - Docker](https://render.com/docs/docker)
- [Render Free Tier Limits](https://render.com/docs/free)
- [Spring Boot Deployment Best Practices](https://docs.spring.io/spring-boot/docs/current/reference/html/deployment.html)

## 🎉 ¡Listo!

Tu aplicación de certificados ahora está desplegada en Render. Puedes acceder a ella desde cualquier lugar usando la URL proporcionada por Render.
