package com.liverpool.configuracion;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import java.util.Arrays;

/**
 * Clase encargada de inicializar y cerrar el navegador Playwright.
 * Cumple con el requerimiento de ejecutarse en modo 'headless' por defecto
 * y permitir modo 'headed' mediante la propiedad del sistema -Dheaded=true.
 */
public class FabricaNavegador {

    // ThreadLocal garantiza que si corremos pruebas en paralelo, cada hilo tenga su propio navegador
    private static final ThreadLocal<Playwright> hiloPlaywright = new ThreadLocal<>();
    private static final ThreadLocal<Browser> hiloNavegador = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> hiloContexto = new ThreadLocal<>();
    private static final ThreadLocal<Page> hiloPagina = new ThreadLocal<>();

    /**
     * Inicializa Playwright, el Navegador, el Contexto y la Página.
     * Retorna la instancia de Page lista para interactuar.
     */
    public static Page iniciarNavegador() {
        // Leemos si el usuario pasó el parámetro -Dheaded=true por consola. Por defecto es "false" (headless)
        String parametroHeaded = System.getProperty("headed", "false");
        boolean esModoVisible = Boolean.parseBoolean(parametroHeaded);

        // 1. Iniciar Playwright
        Playwright playwright = Playwright.create();
        hiloPlaywright.set(playwright);

        // 2. Opciones de lanzamiento
        BrowserType.LaunchOptions opcionesLanzamiento = new BrowserType.LaunchOptions()
                .setHeadless(!esModoVisible) // Si esModoVisible es false, setHeadless será true
                .setArgs(Arrays.asList(
                        "--start-maximized",
                        "--disable-blink-features=AutomationControlled",
                        "--no-sandbox"
                ));

        // 3. Lanzar navegador Chromium
        Browser navegador = playwright.chromium().launch(opcionesLanzamiento);
        hiloNavegador.set(navegador);

        // 4. Crear un contexto con resolución estándar y configuración de idioma de México
        Browser.NewContextOptions opcionesContexto = new Browser.NewContextOptions()
                .setViewportSize(1366, 768)
                .setLocale("es-MX")
                .setTimezoneId("America/Mexico_City")
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36");

        BrowserContext contexto = navegador.newContext(opcionesContexto);
        
        // Evitamos que el sitio detecte que es un bot automatizado
        contexto.addInitScript("Object.defineProperty(navigator, 'webdriver', {get: () => undefined});");
        hiloContexto.set(contexto);

        // 5. Crear la nueva página
        Page pagina = contexto.newPage();
        hiloPagina.set(pagina);

        return pagina;
    }

    /**
     * Obtiene la página activa del hilo actual.
     */
    public static Page obtenerPagina() {
        return hiloPagina.get();
    }

    /**
     * Cierra de forma segura todos los recursos de Playwright del hilo actual.
     */
    public static void cerrarNavegador() {
        try {
            if (hiloPagina.get() != null) {
                hiloPagina.get().close();
                hiloPagina.remove();
            }
            if (hiloContexto.get() != null) {
                hiloContexto.get().close();
                hiloContexto.remove();
            }
            if (hiloNavegador.get() != null) {
                hiloNavegador.get().close();
                hiloNavegador.remove();
            }
            if (hiloPlaywright.get() != null) {
                hiloPlaywright.get().close();
                hiloPlaywright.remove();
            }
        } catch (Exception e) {
            System.err.println("Error al cerrar el navegador: " + e.getMessage());
        }
    }
}