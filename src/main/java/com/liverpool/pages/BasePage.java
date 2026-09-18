package com.liverpool.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;

/**
 * Clase base para todas las páginas (Page Objects).
 * Contiene métodos utilitarios con ESPERAS EXPLÍCITAS para garantizar
 * que los elementos estén listos antes de interactuar con ellos.
 */
public abstract class BasePage {

    protected Page pagina;

    public BasePage(Page pagina) {
        this.pagina = pagina;
    }

    /**
     * Espera explícita para navegar a una URL y asegurar que el DOM cargó.
     */
    public void navegarA(String url) {
        pagina.navigate(url);
        // Espera explícita: hasta que el evento DOMContentLoaded se haya emitido
        pagina.waitForLoadState(LoadState.DOMCONTENTLOADED);
    }

    /**
     * ESPERA EXPLÍCITA: Espera a que un elemento sea visible en pantalla.
     * @param selector El selector CSS o XPath del elemento
     * @param tiempoSegundos El tiempo máximo explícito que esperaremos
     */
    public void esperarVisibilidad(String selector, int tiempoSegundos) {
        pagina.locator(selector).first().waitFor(
                new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE) // Condición explícita: Visible
                        .setTimeout(tiempoSegundos * 1000)
        );
    }

    /**
     * ESPERA EXPLÍCITA: Espera a que un elemento sea interactuable y le hace clic.
     */
    public void hacerClic(String selector) {
        // Espera explícita de visibilidad antes de hacer clic
        esperarVisibilidad(selector, 10);
        pagina.locator(selector).first().click();
    }

    /**
     * ESPERA EXPLÍCITA: Espera a que el campo sea visible antes de escribir.
     */
    public void escribirTexto(String selector, String texto) {
        // Espera explícita de visibilidad antes de escribir
        esperarVisibilidad(selector, 10);
        pagina.locator(selector).first().fill(texto);
    }

    /**
     * Espera a que el contenido HTML y DOM se hayan cargado y estabilizado.
     */
    public void esperarQueLaRedEsteInactiva() {
        try {
            // Esperamos que el DOM esté listo y damos una pausa pequeña de 1.5s para que los componentes rendericen
            pagina.waitForLoadState(LoadState.DOMCONTENTLOADED);
            pagina.waitForTimeout(1500);
        } catch (Exception e) {
            // Continuar si ya cargó
        }
    }
    /**
     * Retorna el título de la página actual.
     */
    public String obtenerTitulo() {
        return pagina.title();
    }
}