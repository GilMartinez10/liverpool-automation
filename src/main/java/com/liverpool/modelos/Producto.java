package com.liverpool.modelos;

import java.util.Objects;

public class Producto {

    private String nombre;
    private double precio;
    private String precioTextoOriginal;

    // Constructor vacío
    public Producto() {
    }

    // Constructor con parámetros
    public Producto(String nombre, double precio, String precioTextoOriginal) {
        this.nombre = nombre != null ? nombre.trim() : "";
        this.precio = precio;
        this.precioTextoOriginal = precioTextoOriginal != null ? precioTextoOriginal.trim() : "";
    }

    // Getters y Setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre != null ? nombre.trim() : "";
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getPrecioTextoOriginal() {
        return precioTextoOriginal;
    }

    public void setPrecioTextoOriginal(String precioTextoOriginal) {
        this.precioTextoOriginal = precioTextoOriginal != null ? precioTextoOriginal.trim() : "";
    }

    /**
     * Método de utilidad para convertir textos como "$12,499.00" o "$899" a un número double (12499.00).
     * Esto nos permite comparar precios numéricamente de forma confiable.
     */
    public static double convertirPrecioADouble(String textoPrecio) {
        if (textoPrecio == null || textoPrecio.trim().isEmpty()) {
            return 0.0;
        }
        try {
            // Si vienen dos precios juntos (ej: "$39900$49900"), separamos por '$' y tomamos el primero
            String[] partes = textoPrecio.split("\\$");
            String precioElegido = "";
            for (String parte : partes) {
                if (!parte.trim().isEmpty()) {
                    precioElegido = parte.trim();
                    break; // Tomamos el primer precio que es el de oferta
                }
            }
            // Quitamos comas y caracteres no numéricos
            String soloNumeros = precioElegido.replaceAll("[^0-9.]", "");
            if (soloNumeros.isEmpty()) return 0.0;
            double valor = Double.parseDouble(soloNumeros);
            // Si el valor viene sin punto decimal (ej: 39900 representando $399.00), lo ajustamos a pesos
            if (valor > 10000 && !soloNumeros.contains(".")) {
                valor = valor / 100.0;
            }
            return valor;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    @Override
    public String toString() {
        return String.format("Producto { Nombre: '%s' | Precio: $%.2f | Texto original: '%s' }", 
                nombre, precio, precioTextoOriginal);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Producto otro = (Producto) obj;
        // Compara nombres ignorando mayúsculas y espacios innecesarios
        return this.nombre.trim().equalsIgnoreCase(otro.nombre.trim());
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre.toLowerCase().trim());
    }
}