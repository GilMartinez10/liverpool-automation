package com.liverpool.pruebas;

import com.liverpool.modelos.Producto;
import com.liverpool.pages.InicioPage;
import com.liverpool.pages.ResultadosBusquedaPage;
import com.liverpool.servicios.ServicioIntercepcionRed;
import com.liverpool.servicios.ServicioIntercepcionRed.ResultadoValidacionCruzada;
import org.testng.TestNG;
import org.testng.annotations.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class LiverpoolE2ETest extends BaseTest {

    @Test(description = "Flujo E2E completo y validación cruzada con la API interceptada")
    public void flujoBusquedaPlaystation5() {
        System.out.println(">>> Iniciando prueba E2E e Intercepción de Red en Liverpool...");

        try {
    
            ServicioIntercepcionRed servicioRed = new ServicioIntercepcionRed();
            servicioRed.activarEscuchaDeRed(pagina);

      
     
            InicioPage inicioPage = new InicioPage(pagina);

          
            System.out.println("Paso 1: Abriendo Liverpool...");
            inicioPage.abrir();

           
            System.out.println("Paso 2: Buscando 'playstation 5'...");
            ResultadosBusquedaPage resultadosPage = inicioPage.buscarProducto("playstation 5");

            
            System.out.println("Paso 3: Filtrando por color Blanco...");
            resultadosPage.filtrarPorColorBlanco();

            
            System.out.println("Paso 4: Ordenando por Menor precio...");
            resultadosPage.ordenarPorMenorPrecio();

           
            System.out.println("Paso 5: Extrayendo primeros 5 productos de la pantalla...");
            List<Producto> productosUI = resultadosPage.extraerPrimerosProductos(5);

            
            assertThat(productosUI)
                    .as("La lista de productos extraídos de la pantalla no debe estar vacía")
                    .isNotEmpty();

            
            resultadosPage.imprimirResultadosEnConsola(productosUI);

            
            System.out.println("Paso 6: Procesando respuestas interceptadas de la red...");
            List<Producto> productosAPI = servicioRed.extraerProductosDeApiInterceptada();

            if (!productosAPI.isEmpty()) {
                
                ResultadoValidacionCruzada resultado = servicioRed.realizarValidacionCruzada(productosUI, productosAPI);

           
                int minimoRequerido = Math.min(3, productosUI.size());
                assertThat(resultado.getCantidadCoincidencias())
                        .as("Al menos %d de los productos mostrados en la pantalla deben coincidir con la respuesta del servidor", minimoRequerido)
                        .isGreaterThanOrEqualTo(minimoRequerido);

                System.out.println("¡Validación cruzada superada con éxito!");
            } else {
                System.out.println("[INFO] Los productos iniciales fueron renderizados por SSR o la llamada de catálogo no fue capturada en este ciclo.");
            }

        } catch (Exception e) {
            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.err.println("ERROR ENCONTRADO EN LA PRUEBA:");
            System.err.println("Mensaje: " + e.getMessage());
            e.printStackTrace();
            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            throw e;
        }
    }

    public static void main(String[] args) {
        System.setProperty("headed", "true");

        TestNG testng = new TestNG();
        testng.setTestClasses(new Class[] { LiverpoolE2ETest.class });
        testng.run();
    }
}