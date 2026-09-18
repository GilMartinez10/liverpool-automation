package com.liverpool.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;

/**
 * Clase base para todas las páginas.
 */
public abstract class BasePage {

    protected Page pagina;

    public BasePage(Page pagina) {
        this.pagina = pagina;
    }


    public void navegarA(String url) {
        pagina.navigate(url);
        pagina.waitForLoadState(LoadState.DOMCONTENTLOADED);
    }

    
    public void esperarVisibilidad(String selector, int tiempoSegundos) {
        pagina.locator(selector).first().waitFor(
                new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE) 
                        .setTimeout(tiempoSegundos * 1000)
        );
    }

   
    public void hacerClic(String selector) {
      
        esperarVisibilidad(selector, 10);
        pagina.locator(selector).first().click();
    }

 
    public void escribirTexto(String selector, String texto) {
  
        esperarVisibilidad(selector, 10);
        pagina.locator(selector).first().fill(texto);
    }

 
    public void esperarQueLaRedEsteInactiva() {
        try {
          
            pagina.waitForLoadState(LoadState.DOMCONTENTLOADED);
            pagina.waitForTimeout(1500);
        } catch (Exception e) {
            
        }
    }
  
    public String obtenerTitulo() {
        return pagina.title();
    }
}