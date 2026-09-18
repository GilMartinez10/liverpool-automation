# Estrategia de Pruebas: Liverpool E-Commerce


### 1. ¿Qué NO automatizaría en este flujo y por qué?
* **Pagos reales con tarjeta:** Usar dinero real o esperar códigos SMS del banco en pruebas automáticas es peligroso e inestable. Para eso se usan tarjetas y cuentas de prueba en ambientes de prueba (QA).
* **Banners de ofertas o publicidad:** La publicidad cambia todos los días y haría que la prueba falle por cambios visuales y no por fallos reales en la tienda.
* **Fotos de los productos:** Las fotos y tamaños cambian seguido. Es mejor validar que el nombre y el precio estén correctos a revisar la imagen al píxel.

---

### 2. Si Liverpool pusiera un CAPTCHA en la búsqueda, ¿cómo lo manejaría?
* **No intentaría romper el CAPTCHA:** Intentar burlar un captcha con robots es lento y suele fallar.
* **Pedir apoyo al equipo de desarrollo:** Lo correcto es pedir que en el ambiente de pruebas (QA) desactiven el captcha para el robot, o que nos den un permiso especial (por dirección IP o clave de prueba).
* **Usar cookies:** Otra opción práctica es entrar una vez manualmente, guardar la sesión (cookies) e inyectarla en Playwright para entrar directo sin que pida el captcha.

---

### 3. ¿Qué riesgos de que la prueba falle (flakiness) existen y cómo los evité?
* **La página tarda en actualizar los productos al filtrar:** Cuando eliges "Blanco" o "Menor precio", la lista tarda unos segundos en cambiar. Lo evité usando las esperas de Playwright para esperar a que las tarjetas aparezcan en pantalla antes de leerlas.
* **Precios raros o con descuento:** A veces en pantalla vienen dos precios pegados (el tachado y el de oferta). Lo evité limpiando el texto con código para tomar siempre el precio real de oferta.
* **Nombres cortados con puntos suspensivos:** En la pantalla el nombre a veces viene resumido y en la API viene completo. Lo evité comparando palabras clave para que no marque error si el nombre está un poco más corto en la pantalla.

---

### 4. ¿Qué cambiaría si tuviera que correr 50 suites de prueba en el pipeline?
* **Correr pruebas en paralelo:** Si 50 pruebas corren una por una tardarían horas. Las configuraría para que corran varias al mismo tiempo.
* **Separar las pruebas:** Correr solo las pruebas más críticas (las más rápidas) en cada cambio que hagan los desarrolladores, y correr las 50 pruebas completas por la noche.
* **Correr en modo invisible (Headless):** En los servidores siempre se debe correr sin abrir ventanas gráficas para que sea mucho más rápido y use menos memoria
