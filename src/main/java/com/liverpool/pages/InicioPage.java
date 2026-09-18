package com.liverpool.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Representa la página principal (Home) de Liverpool.
 * Encapsula la interacción con la barra de búsqueda y modales iniciales.
 */
public class InicioPage extends BasePage {

    // Selector compuesto resiliente: busca por ID o por el texto del placeholder
    private static final String CAMPO_BUSQUEDA = 
            "#mainSearchbar, input[id='mainSearchbar'], input[placeholder*='Buscar'], input[type='search']";
    
    // Selectores para posibles modales (ubicación, código postal, cookies)
    private static final String BOTONES_CERRAR_MODALES = 
            "button.close, [aria-label='Close'], button:has-text('Aceptar'), button:has-text('Continuar'), .modal-header button";

    public InicioPage(Page pagina) {
        super(pagina);
    }

    /**
     * Navega a la página de inicio de Liverpool y espera a que el contenido esté listo.
     */
    public InicioPage abrir() {
        navegarA("https://www.liverpool.com.mx/tienda/home");
        
        // Damos una pequeña pausa para que los scripts de Liverpool monten el header y los modales
        pagina.waitForTimeout(2000);
        cerrarModalesSiExisten();
        return this;
    }

    /**
     * Cierra ventanas emergentes (código postal, cookies, banners) si aparecen.
     */
    private void cerrarModalesSiExisten() {
        try {
            Locator botonCerrar = pagina.locator(BOTONES_CERRAR_MODALES).first();
            if (botonCerrar.isVisible()) {
                System.out.println("Modal detectado, cerrándolo...");
                botonCerrar.click();
                pagina.waitForTimeout(1000);
            }
        } catch (Exception e) {
            // Si no hay modales, continuamos
        }
    }

    /**
     * Realiza la búsqueda de un producto.
     */
    public ResultadosBusquedaPage buscarProducto(String terminoBusqueda) {
        System.out.println("Esperando que el buscador esté listo...");
        
        // Esperamos hasta 25 segundos de forma explícita
        esperarVisibilidad(CAMPO_BUSQUEDA, 25);

        Locator inputBusqueda = pagina.locator(CAMPO_BUSQUEDA).first();
        inputBusqueda.click();
        inputBusqueda.fill(terminoBusqueda);
        inputBusqueda.press("Enter");

        System.out.println("Texto ingresado y Enter presionado. Esperando resultados...");
        esperarQueLaRedEsteInactiva();

        return new ResultadosBusquedaPage(pagina);
    }
}