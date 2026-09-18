package com.liverpool.modelos;

import java.util.Objects;

public class Producto {

    private String nombre;
    private double precio;
    private String precioTextoOriginal;


    public Producto() {
    }

 
    public Producto(String nombre, double precio, String precioTextoOriginal) {
        this.nombre = nombre != null ? nombre.trim() : "";
        this.precio = precio;
        this.precioTextoOriginal = precioTextoOriginal != null ? precioTextoOriginal.trim() : "";
    }

  
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
     * Método para convertir textos como "$12,499.00" o "$899" a un número double (12499.00).
     */
    public static double convertirPrecioADouble(String textoPrecio) {
        if (textoPrecio == null || textoPrecio.trim().isEmpty()) {
            return 0.0;
        }
        try {
           
            String[] partes = textoPrecio.split("\\$");
            String precioElegido = "";
            for (String parte : partes) {
                if (!parte.trim().isEmpty()) {
                    precioElegido = parte.trim();
                    break; 
                }
            }
          
            String soloNumeros = precioElegido.replaceAll("[^0-9.]", "");
            if (soloNumeros.isEmpty()) return 0.0;
            double valor = Double.parseDouble(soloNumeros);
          
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
      
        return this.nombre.trim().equalsIgnoreCase(otro.nombre.trim());
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre.toLowerCase().trim());
    }
}