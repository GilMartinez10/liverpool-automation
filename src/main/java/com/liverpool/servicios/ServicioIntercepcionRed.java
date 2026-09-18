package com.liverpool.servicios;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liverpool.modelos.Producto;
import com.microsoft.playwright.Page;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Servicio de Intercepción de Red (Parte 2):
 * Captura las respuestas JSON de /api/plp/search, extrae los productos devueltos
 * y valida de forma cruzada que coincidan con lo que se mostró en la pantalla.
 */
public class ServicioIntercepcionRed {

    private final ObjectMapper mapper = new ObjectMapper();
    // Guardamos la URL y el JSON de cada respuesta interceptada
    private final List<RespuestaCapturada> respuestasCapturadas = new CopyOnWriteArrayList<>();

    private static class RespuestaCapturada {
        String url;
        String cuerpoJson;

        RespuestaCapturada(String url, String cuerpoJson) {
            this.url = url;
            this.cuerpoJson = cuerpoJson;
        }
    }

    /**
     * Activa el listener de red en Playwright.
     */
    public void activarEscuchaDeRed(Page pagina) {
        System.out.println("Activando escucha e intercepción de red...");

        pagina.onResponse(respuesta -> {
            try {
                String url = respuesta.url().toLowerCase();
                // Nos enfocamos en las respuestas de búsqueda y catálogo (PLP)
                if (url.contains("/api/plp/search") && respuesta.status() == 200) {
                    byte[] cuerpoBytes = respuesta.body();
                    String jsonString = new String(cuerpoBytes, StandardCharsets.UTF_8);
                    respuestasCapturadas.add(new RespuestaCapturada(respuesta.url(), jsonString));
                    System.out.println("[INTERCEPTOR] Payload de búsqueda capturado: " + respuesta.url());
                }
            } catch (Exception e) {
                // Respuestas que puedan cerrarse asíncronamente
            }
        });
    }

    /**
     * Parsea la última respuesta de /api/plp/search (que contiene el filtro y orden final)
     * y extrae los productos del nodo plpResults.records.
     */
    public List<Producto> extraerProductosDeApiInterceptada() {
        System.out.println("Procesando respuestas interceptadas de la API...");
        List<Producto> productosApi = new ArrayList<>();

        if (respuestasCapturadas.isEmpty()) {
            System.out.println("[AVISO] No se capturaron llamadas a /api/plp/search.");
            return productosApi;
        }

        // Tomamos la última respuesta capturada
        RespuestaCapturada ultimaRespuesta = respuestasCapturadas.get(respuestasCapturadas.size() - 1);
        System.out.println("Analizando payload JSON de: " + ultimaRespuesta.url);

        try {
            JsonNode raiz = mapper.readTree(ultimaRespuesta.cuerpoJson);
            JsonNode dataNode = raiz.has("data") ? raiz.get("data") : raiz;

            // Imprimimos qué llaves tiene 'data'
            if (dataNode.isObject()) {
                List<String> llavesData = new ArrayList<>();
                dataNode.fieldNames().forEachRemaining(llavesData::add);
                System.out.println("Llaves encontradas dentro de 'data': " + llavesData);

                // Buscamos cuál de esas llaves es el arreglo de productos
                for (String llave : llavesData) {
                    JsonNode nodoHijo = dataNode.get(llave);

                    // Si es un arreglo (ej: records, products, items)
                    if (nodoHijo.isArray() && nodoHijo.size() > 0) {
                        System.out.printf("-> Arreglo detectado: '%s' con %d elementos.%n", llave, nodoHijo.size());
                        
                        // Mostramos las llaves del primer producto para conocer su estructura
                        JsonNode primerProd = nodoHijo.get(0);
                        if (primerProd.isObject()) {
                            List<String> llavesProd = new ArrayList<>();
                            primerProd.fieldNames().forEachRemaining(llavesProd::add);
                            System.out.println("   Propiedades del producto: " + llavesProd);
                        }

                        // Extraemos todos los productos del arreglo
                        for (JsonNode nodoProd : nodoHijo) {
                            String nombre = extraerNombre(nodoProd);
                            double precio = extraerPrecio(nodoProd);

                            if (!nombre.isEmpty()) {
                                productosApi.add(new Producto(nombre, precio, String.valueOf(precio)));
                            }
                        }

                        if (!productosApi.isEmpty()) {
                            System.out.println("¡Productos cargados exitosamente desde la lista '" + llave + "'!");
                            break;
                        }
                    } else if (nodoHijo.isObject() && nodoHijo.has("records")) {
                        // Caso cuando está dentro de plpResults.records
                        JsonNode records = nodoHijo.get("records");
                        if (records.isArray()) {
                            for (JsonNode nodoProd : records) {
                                String nombre = extraerNombre(nodoProd);
                                double precio = extraerPrecio(nodoProd);
                                if (!nombre.isEmpty()) {
                                    productosApi.add(new Producto(nombre, precio, String.valueOf(precio)));
                                }
                            }
                            if (!productosApi.isEmpty()) break;
                        }
                    }
                }
            }

            System.out.println("Total de productos extraídos del JSON de la API: " + productosApi.size());

        } catch (Exception e) {
            System.err.println("Error al parsear el JSON de la API: " + e.getMessage());
        }

        return productosApi;
    }
    private void buscarNodosProducto(JsonNode nodoActual, List<JsonNode> resultado) {
        if (nodoActual == null) return;
        if (nodoActual.isObject()) {
            if (nodoActual.has("productDisplayName") && (nodoActual.has("promoPrice") || nodoActual.has("listPrice") || nodoActual.has("minimumPromoPrice"))) {
                resultado.add(nodoActual);
                return;
            }
            nodoActual.fields().forEachRemaining(entry -> buscarNodosProducto(entry.getValue(), resultado));
        } else if (nodoActual.isArray()) {
            for (JsonNode item : nodoActual) {
                buscarNodosProducto(item, resultado);
            }
        }
    }

    private String extraerNombre(JsonNode nodo) {
        String[] llaves = {"productDisplayName", "displayName", "title", "productName"};
        for (String k : llaves) {
            if (nodo.hasNonNull(k)) {
                return nodo.get(k).asText().trim();
            }
        }
        return "";
    }

    private double extraerPrecio(JsonNode nodo) {
        // Si el precio viene dentro del objeto "priceInfo" de Liverpool
        if (nodo.has("priceInfo") && nodo.get("priceInfo").isObject()) {
            return extraerPrecio(nodo.get("priceInfo"));
        }

        // Llaves comunes de precio en Liverpool
        String[] posiblesPrecios = {
                "promoPrice", "minimumPromoPrice", "salePrice", "listPrice", "price", "discountPrice", "originalPrice"
        };
        for (String llave : posiblesPrecios) {
            if (nodo.hasNonNull(llave)) {
                JsonNode val = nodo.get(llave);
                if (val.isNumber()) return val.asDouble();
                if (val.isTextual()) return Producto.convertirPrecioADouble(val.asText());
            }
        }
        return 0.0;
    }

    /**
     * Validación cruzada requerida por la Parte 2:
     * Compara productos de UI contra los de la API, cuenta cuántos coinciden y reporta discrepancias.
     */
    public ResultadoValidacionCruzada realizarValidacionCruzada(List<Producto> productosUI, List<Producto> productosAPI) {
        ResultadoValidacionCruzada resultado = new ResultadoValidacionCruzada();

        System.out.println("\n=======================================================");
        System.out.println("   REPORTE DE VALIDACIÓN CRUZADA: PANTALLA (UI) vs API ");
        System.out.println("=======================================================");

        int coincidencias = 0;

        for (int i = 0; i < productosUI.size(); i++) {
            Producto prodUI = productosUI.get(i);
            Producto prodApi = buscarCoincidenciaEnApi(prodUI, productosAPI);

            System.out.printf("[%d] UI: '%s' | Precio: $%.2f%n", (i + 1), prodUI.getNombre(), prodUI.getPrecio());

            if (prodApi != null) {
                coincidencias++;
                System.out.printf("    -> COINCIDE EN API: '%s' | Precio API: $%.2f%n", prodApi.getNombre(), prodApi.getPrecio());

                // Diferencia de precio
                if (Math.abs(prodUI.getPrecio() - prodApi.getPrecio()) > 0.01) {
                    String disc = String.format("Diferencia de precio en '%s': UI=$%.2f vs API=$%.2f",
                            prodUI.getNombre(), prodUI.getPrecio(), prodApi.getPrecio());
                    resultado.getDiscrepancias().add(disc);
                    System.out.println("    [!] DISCREPANCIA DE PRECIO: " + disc);
                }

                // Título ligeramente diferente (truncado en UI)
                if (!prodUI.getNombre().equalsIgnoreCase(prodApi.getNombre())) {
                    String nota = String.format("Ajuste de título: UI='%s' vs API='%s'",
                            prodUI.getNombre(), prodApi.getNombre());
                    resultado.getDiscrepancias().add(nota);
                    System.out.println("    [i] NOTA EN TÍTULO: " + nota);
                }
            } else {
                String noEncontrado = "El producto UI '" + prodUI.getNombre() + "' no se localizó en la respuesta de la API.";
                resultado.getDiscrepancias().add(noEncontrado);
                System.out.println("    [X] NO ENCONTRADO EN API: " + noEncontrado);
            }
            System.out.println("-------------------------------------------------------");
        }

        resultado.setCantidadCoincidencias(coincidencias);
        System.out.println("TOTAL COINCIDENCIAS: " + coincidencias + " de " + productosUI.size() + " productos de UI.");
        System.out.println("TOTAL DISCREPANCIAS: " + resultado.getDiscrepancias().size());
        System.out.println("=======================================================\n");

        return resultado;
    }

    private Producto buscarCoincidenciaEnApi(Producto prodUI, List<Producto> productosAPI) {
        String uiNorm = normalizarTexto(prodUI.getNombre());

        for (Producto api : productosAPI) {
            String apiNorm = normalizarTexto(api.getNombre());

            // Coincidencia exacta o si uno contiene al otro
            if (uiNorm.equals(apiNorm) || uiNorm.contains(apiNorm) || apiNorm.contains(uiNorm)) {
                return api;
            }

            // Coincidencia por palabras compartidas (al menos 50% de palabras clave)
            if (calcularCoincidenciaPalabras(uiNorm, apiNorm) >= 0.5) {
                return api;
            }
        }
        return null;
    }

    private double calcularCoincidenciaPalabras(String s1, String s2) {
        String[] w1 = s1.split("\\s+");
        String[] w2 = s2.split("\\s+");
        int coincidencias = 0;
        for (String a : w1) {
            if (a.length() < 3) continue;
            for (String b : w2) {
                if (a.equals(b)) {
                    coincidencias++;
                    break;
                }
            }
        }
        return (double) coincidencias / Math.max(w1.length, 1);
    }

    private String normalizarTexto(String texto) {
        return texto == null ? "" : texto.trim().toLowerCase().replaceAll("[^a-z0-9 ]", " ").replaceAll("\\s+", " ");
    }

    public static class ResultadoValidacionCruzada {
        private int cantidadCoincidencias;
        private final List<String> discrepancias = new ArrayList<>();

        public int getCantidadCoincidencias() {
            return cantidadCoincidencias;
        }

        public void setCantidadCoincidencias(int cantidadCoincidencias) {
            this.cantidadCoincidencias = cantidadCoincidencias;
        }

        public List<String> getDiscrepancias() {
            return discrepancias;
        }
    }
}