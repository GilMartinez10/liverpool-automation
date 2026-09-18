package com.liverpool.pruebas;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

public class PrimerPasoPlaywright {

    public static void main(String[] args) {
        System.out.println("Iniciando Playwright...");

        // 1. Iniciamos el motor de Playwright
        try (Playwright playwright = Playwright.create()) {

            // 2. Abrimos el navegador Chromium
            // setHeadless(false) le dice a Playwright: "Abre la ventana visible para que yo la vea"
            // Por defecto viene en true (invisible/headless)
            Browser navegador = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(false)
            );

            // 3. Abrimos una nueva pestaña/página
            Page pagina = navegador.newPage();

            System.out.println("Navegando hacia Liverpool...");
            // 4. Navegamos a la URL de Liverpool
            pagina.navigate("https://www.liverpool.com.mx/tienda/home");

            // 5. Obtenemos e imprimimos el título de la página
            String titulo = pagina.title();
            System.out.println("¡Conexión exitosa! El título de la página es: " + titulo);

            // Esperamos 3 segundos solo para que alcances a ver la ventana antes de cerrarse
            pagina.waitForTimeout(3000);

            // 6. Cerramos el navegador
            navegador.close();
            System.out.println("Navegador cerrado correctamente.");
        }
    }
}