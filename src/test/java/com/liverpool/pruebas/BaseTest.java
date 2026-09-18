package com.liverpool.pruebas;

import com.liverpool.configuracion.FabricaNavegador;
import com.liverpool.oyentes.OyentePruebas;
import com.microsoft.playwright.Page;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;


@Listeners(OyentePruebas.class)
public abstract class BaseTest {

    protected Page pagina;

    @BeforeMethod
    public void configurarPrueba() {
        pagina = FabricaNavegador.iniciarNavegador();
    }

    @AfterMethod
    public void finalizarPrueba() {
        FabricaNavegador.cerrarNavegador();
    }
}