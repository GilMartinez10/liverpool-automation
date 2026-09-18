package com.liverpool.pages;

import com.liverpool.modelos.Producto;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa la página de resultados de búsqueda (PLP - Product Listing Page) de Liverpool.
 * Encapsula el filtrado por color, el ordenamiento por menor precio
 * y la extracción de los productos resultantes.
 */
public class ResultadosBusquedaPage extends BasePage {

    // Selectores para el filtro de color
    private static final String ENCABEZADO_FILTRO_COLOR = "button:has-text('Color'), div:has-text('Color'), h3:has-text('Color')";
    private static final String OPCION_COLOR_BLANCO = 
            "input[id*='variants.normalizedColor-Blanco'], " +
            "label:has-text('Blanco'), " +
            "label:has-text('White'), " +
            "span:has-text('Blanco')";

    // Selectores para el menú desplegable de ordenamiento
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

    // Selectores universales para las tarjetas de productos de Liverpool
    private static final String TARJETAS_PRODUCTOS = 
            "a[href*='/pdp/'], " +
            "a[href*='/tienda/pdp/'], " +
            "li.m-product__card, " +
            "div.m-product__card, " +
            "div[class*='product-cell'], " +
            "div[class*='card-masonry'], " +
            "[class*='m-figureCard']";

    // Selectores para el nombre del producto dentro de la tarjeta
    private static final String TITULO_PRODUCTO = 
            "h3, h5, " +
            "[class*='card-title'], " +
            "[class*='description'], " +
            "p.a-card-description";

    // Selectores para el precio del producto dentro de la tarjeta
    private static final String PRECIO_PRODUCTO = 
            "[class*='discount'], " +
            "[class*='salePrice'], " +
            "[class*='price'], " +
            "p:has-text('$'), " +
            "span:has-text('$')";
    public ResultadosBusquedaPage(Page pagina) {
        super(pagina);
    }

    /**
     * Aplica el filtro de color 'Blanco' en la barra lateral.
     */
    public ResultadosBusquedaPage filtrarPorColorBlanco() {
        System.out.println("Aplicando filtro de color: Blanco...");
        try {
            // Si la sección de color está colapsada como acordeón, le damos clic para abrirla
            Locator encabezadoColor = pagina.locator(ENCABEZADO_FILTRO_COLOR).first();
            if (encabezadoColor.isVisible()) {
                encabezadoColor.click();
            }
        } catch (Exception e) {
            // Si ya estaba desplegado, continúa sin error
        }

        // Espera explícita a que la opción de color Blanco sea visible en pantalla
        esperarVisibilidad(OPCION_COLOR_BLANCO, 10);
        Locator opcionBlanco = pagina.locator(OPCION_COLOR_BLANCO).first();
        
        // Hacemos scroll hacia el elemento para asegurarnos que esté a la vista y hacemos clic
        opcionBlanco.scrollIntoViewIfNeeded();
        opcionBlanco.click();

        // Espera explícita a que la petición AJAX de filtrado se complete
        esperarQueLaRedEsteInactiva();
        System.out.println("Filtro de color Blanco aplicado exitosamente.");
        return this;
    }

    /**
     * Ordena la lista de productos por 'Menor precio' (menor a mayor).
     */
    public ResultadosBusquedaPage ordenarPorMenorPrecio() {
        System.out.println("Aplicando ordenamiento por Menor precio...");

        // 1. Clic en el botón o desplegable 'Ordenar por'
        esperarVisibilidad(BOTON_ORDENAR_POR, 10);
        pagina.locator(BOTON_ORDENAR_POR).first().click();

        // 2. Clic en la opción 'Menor precio'
        esperarVisibilidad(OPCION_MENOR_PRECIO, 10);
        pagina.locator(OPCION_MENOR_PRECIO).first().click();

        // 3. Espera explícita a que los productos se reordenen vía red
        esperarQueLaRedEsteInactiva();
        System.out.println("Ordenamiento por Menor precio aplicado exitosamente.");
        return this;
    }

    /**
     * Extrae el nombre y precio de los primeros N productos mostrados en la pantalla.
     * Retorna una lista de objetos Producto.
     */
    public List<Producto> extraerPrimerosProductos(int cantidadDeseada) {
        System.out.println("Extrayendo los primeros " + cantidadDeseada + " productos de la pantalla...");
        List<Producto> listaProductos = new ArrayList<>();

        // Damos 2 segundos para que las tarjetas terminen su transición de ordenamiento
        pagina.waitForTimeout(2000);

        // Espera explícita a que las tarjetas de producto estén visibles
        esperarVisibilidad(TARJETAS_PRODUCTOS, 20);

        Locator tarjetas = pagina.locator(TARJETAS_PRODUCTOS);
        int totalDisponibles = tarjetas.count();
        System.out.println("Total de tarjetas de producto detectadas en la página: " + totalDisponibles);

        int limite = Math.min(cantidadDeseada, totalDisponibles);

        for (int i = 0; i < limite; i++) {
            Locator tarjetaActual = tarjetas.nth(i);

            // 1. Extraer el nombre del producto
            String nombre = "";
            Locator locatorNombre = tarjetaActual.locator(TITULO_PRODUCTO);
            if (locatorNombre.count() > 0) {
                nombre = locatorNombre.first().innerText().trim();
            }

            // 2. Extraer el precio
            String textoPrecio = "";
            Locator locatorPrecio = tarjetaActual.locator(PRECIO_PRODUCTO);
            if (locatorPrecio.count() > 0) {
                textoPrecio = locatorPrecio.first().innerText().trim();
            }

            // 3. Convertir a double
            double precioNumerico = Producto.convertirPrecioADouble(textoPrecio);

            // Si por alguna razón el nombre venía vacío (ej. tarjeta duplicada por link), tomamos el texto visible
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

    /**
     * Imprime en consola de forma estética los productos extraídos.
     */
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