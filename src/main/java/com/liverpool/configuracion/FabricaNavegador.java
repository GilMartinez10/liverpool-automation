package com.liverpool.configuracion;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import java.util.Arrays;

/**
 * Clase encargada de inicializar y cerrar el navegador Playwright.
 * ejecucion en modo 'headless' por defecto
 * y permitir modo 'headed' mediante la propiedad del sistema -Dheaded=true.
 */
public class FabricaNavegador {

  
    private static final ThreadLocal<Playwright> hiloPlaywright = new ThreadLocal<>();
    private static final ThreadLocal<Browser> hiloNavegador = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> hiloContexto = new ThreadLocal<>();
    private static final ThreadLocal<Page> hiloPagina = new ThreadLocal<>();

  
    public static Page iniciarNavegador() {
      
        String parametroHeaded = System.getProperty("headed", "false");
        boolean esModoVisible = Boolean.parseBoolean(parametroHeaded);

        Playwright playwright = Playwright.create();
        hiloPlaywright.set(playwright);

        BrowserType.LaunchOptions opcionesLanzamiento = new BrowserType.LaunchOptions()
                .setHeadless(!esModoVisible) 
                .setArgs(Arrays.asList(
                        "--start-maximized",
                        "--disable-blink-features=AutomationControlled",
                        "--no-sandbox"
                ));

        
        Browser navegador = playwright.chromium().launch(opcionesLanzamiento);
        hiloNavegador.set(navegador);

    
        Browser.NewContextOptions opcionesContexto = new Browser.NewContextOptions()
                .setViewportSize(1366, 768)
                .setLocale("es-MX")
                .setTimezoneId("America/Mexico_City")
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36");

        BrowserContext contexto = navegador.newContext(opcionesContexto);
        

        contexto.addInitScript("Object.defineProperty(navigator, 'webdriver', {get: () => undefined});");
        hiloContexto.set(contexto);

        
        Page pagina = contexto.newPage();
        hiloPagina.set(pagina);

        return pagina;
    }

   
    public static Page obtenerPagina() {
        return hiloPagina.get();
    }

   
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