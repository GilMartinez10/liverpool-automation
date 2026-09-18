package com.liverpool.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;


public class InicioPage extends BasePage {

 
    private static final String CAMPO_BUSQUEDA = 
            "#mainSearchbar, input[id='mainSearchbar'], input[placeholder*='Buscar'], input[type='search']";
    

    private static final String BOTONES_CERRAR_MODALES = 
            "button.close, [aria-label='Close'], button:has-text('Aceptar'), button:has-text('Continuar'), .modal-header button";

    public InicioPage(Page pagina) {
        super(pagina);
    }

    public InicioPage abrir() {
        navegarA("https://www.liverpool.com.mx/tienda/home");
  
        pagina.waitForTimeout(2000);
        cerrarModalesSiExisten();
        return this;
    }

    private void cerrarModalesSiExisten() {
        try {
            Locator botonCerrar = pagina.locator(BOTONES_CERRAR_MODALES).first();
            if (botonCerrar.isVisible()) {
                System.out.println("Modal detectado, cerrándolo...");
                botonCerrar.click();
                pagina.waitForTimeout(1000);
            }
        } catch (Exception e) {
            
        }
    }

 
    public ResultadosBusquedaPage buscarProducto(String terminoBusqueda) {
        System.out.println("Esperando que el buscador esté listo...");
        
        
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