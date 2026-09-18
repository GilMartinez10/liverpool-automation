package com.liverpool.pages;

import com.liverpool.modelos.Producto;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.ArrayList;
import java.util.List;

public class ResultadosBusquedaPage extends BasePage {

   
    private static final String ENCABEZADO_FILTRO_COLOR = "button:has-text('Color'), div:has-text('Color'), h3:has-text('Color')";
    private static final String OPCION_COLOR_BLANCO = 
            "input[id*='variants.normalizedColor-Blanco'], " +
            "label:has-text('Blanco'), " +
            "label:has-text('White'), " +
            "span:has-text('Blanco')";

   
    private static final String BOTON_ORDENAR_POR = 
            "button:has-text('Ordenar por'), " +
            ".m-dropdown__trigger, " +
            "button:has-text('Relevancia'), " +
            "div.m-dropdown";
    private static final String OPCION_MENOR_PRECIO = 
            "button:has-text('Menor precio'), " +
            "a:has-text('Menor precio'), " +
            "li:has-text('Menor precio'), " +
            "span:has-text('Menor precio')";

 
    private static final String TARJETAS_PRODUCTOS = 
            "a[href*='/pdp/'], " +
            "a[href*='/tienda/pdp/'], " +
            "li.m-product__card, " +
            "div.m-product__card, " +
            "div[class*='product-cell'], " +
            "div[class*='card-masonry'], " +
            "[class*='m-figureCard']";


    private static final String TITULO_PRODUCTO = 
            "h3, h5, " +
            "[class*='card-title'], " +
            "[class*='description'], " +
            "p.a-card-description";


    private static final String PRECIO_PRODUCTO = 
            "[class*='discount'], " +
            "[class*='salePrice'], " +
            "[class*='price'], " +
            "p:has-text('$'), " +
            "span:has-text('$')";
    public ResultadosBusquedaPage(Page pagina) {
        super(pagina);
    }

 
    public ResultadosBusquedaPage filtrarPorColorBlanco() {
        System.out.println("Aplicando filtro de color: Blanco...");
        try {
            
            Locator encabezadoColor = pagina.locator(ENCABEZADO_FILTRO_COLOR).first();
            if (encabezadoColor.isVisible()) {
                encabezadoColor.click();
            }
        } catch (Exception e) {
           
        }

 
        esperarVisibilidad(OPCION_COLOR_BLANCO, 10);
        Locator opcionBlanco = pagina.locator(OPCION_COLOR_BLANCO).first();
        
    
        opcionBlanco.scrollIntoViewIfNeeded();
        opcionBlanco.click();

   
        esperarQueLaRedEsteInactiva();
        System.out.println("Filtro de color Blanco aplicado exitosamente.");
        return this;
    }


    public ResultadosBusquedaPage ordenarPorMenorPrecio() {
        System.out.println("Aplicando ordenamiento por Menor precio...");

     
        esperarVisibilidad(BOTON_ORDENAR_POR, 10);
        pagina.locator(BOTON_ORDENAR_POR).first().click();

      
        esperarVisibilidad(OPCION_MENOR_PRECIO, 10);
        pagina.locator(OPCION_MENOR_PRECIO).first().click();

      
        esperarQueLaRedEsteInactiva();
        System.out.println("Ordenamiento por Menor precio aplicado exitosamente.");
        return this;
    }


    public List<Producto> extraerPrimerosProductos(int cantidadDeseada) {
        System.out.println("Extrayendo los primeros " + cantidadDeseada + " productos de la pantalla...");
        List<Producto> listaProductos = new ArrayList<>();

      
        pagina.waitForTimeout(2000);

       
        esperarVisibilidad(TARJETAS_PRODUCTOS, 20);

        Locator tarjetas = pagina.locator(TARJETAS_PRODUCTOS);
        int totalDisponibles = tarjetas.count();
        System.out.println("Total de tarjetas de producto detectadas en la página: " + totalDisponibles);

        int limite = Math.min(cantidadDeseada, totalDisponibles);

        for (int i = 0; i < limite; i++) {
            Locator tarjetaActual = tarjetas.nth(i);

       
            String nombre = "";
            Locator locatorNombre = tarjetaActual.locator(TITULO_PRODUCTO);
            if (locatorNombre.count() > 0) {
                nombre = locatorNombre.first().innerText().trim();
            }

            String textoPrecio = "";
            Locator locatorPrecio = tarjetaActual.locator(PRECIO_PRODUCTO);
            if (locatorPrecio.count() > 0) {
                textoPrecio = locatorPrecio.first().innerText().trim();
            }

            double precioNumerico = Producto.convertirPrecioADouble(textoPrecio);

            if (nombre.isEmpty()) {
                String todoElTexto = tarjetaActual.innerText();
                String[] lineas = todoElTexto.split("\n");
                if (lineas.length > 0) {
                    nombre = lineas[0].trim();
                }
            }

            listaProductos.add(new Producto(nombre, precioNumerico, textoPrecio));
        }

        return listaProductos;
    }

  
    public void imprimirResultadosEnConsola(List<Producto> productos) {
        System.out.println("\n=======================================================");
        System.out.println("   RESULTADOS EXTRAÍDOS DE LA PANTALLA (UI LIVERPOOL)  ");
        System.out.println("=======================================================");
        for (int i = 0; i < productos.size(); i++) {
            Producto p = productos.get(i);
            System.out.printf("[%d] Nombre : %s%n", (i + 1), p.getNombre());
            System.out.printf("    Precio : $%.2f (Texto original: '%s')%n", p.getPrecio(), p.getPrecioTextoOriginal());
            System.out.println("-------------------------------------------------------");
        }
        System.out.println("=======================================================\n");
    }
}