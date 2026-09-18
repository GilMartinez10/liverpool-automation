package com.liverpool.pruebas;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

public class PrimerPasoPlaywright {

    public static void main(String[] args) {
        System.out.println("Iniciando Playwright...");

       
        try (Playwright playwright = Playwright.create()) {

            
            Browser navegador = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(false)
            );

            
            Page pagina = navegador.newPage();

            System.out.println("Navegando hacia Liverpool...");
           
            pagina.navigate("https://www.liverpool.com.mx/tienda/home");

            
            String titulo = pagina.title();
            System.out.println("¡Conexión exitosa! El título de la página es: " + titulo);

            
            pagina.waitForTimeout(3000);

          
            navegador.close();
            System.out.println("Navegador cerrado correctamente.");
        }
    }
}