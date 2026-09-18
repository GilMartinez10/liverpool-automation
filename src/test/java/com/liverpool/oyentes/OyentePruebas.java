package com.liverpool.oyentes;

import com.liverpool.configuracion.FabricaNavegador;
import com.microsoft.playwright.Page;
import io.qameta.allure.Attachment;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;


public class OyentePruebas implements ITestListener {

    @Override
    public void onTestStart(ITestResult result) {
        System.out.println(">>> [OYENTE] Iniciando ejecución de: " + result.getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        System.out.println(">>> [OYENTE] Prueba EXITOSA: " + result.getName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String nombrePrueba = result.getName();
        System.err.println(">>> [OYENTE] Prueba FALLIDA: " + nombrePrueba + ". Capturando pantalla automáticamente...");

        Page pagina = FabricaNavegador.obtenerPagina();
        if (pagina != null) {
            try {
               
                byte[] screenshotBytes = pagina.screenshot(new Page.ScreenshotOptions().setFullPage(true));
                adjuntarScreenshotAllure(screenshotBytes, nombrePrueba);

                
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String carpeta = "target/screenshots/";
                Files.createDirectories(Paths.get(carpeta));
                
                String rutaArchivo = carpeta + nombrePrueba + "_" + timestamp + ".png";
                try (FileOutputStream fos = new FileOutputStream(rutaArchivo)) {
                    fos.write(screenshotBytes);
                }
                System.out.println(">>> [OYENTE] Screenshot guardado exitosamente en: " + rutaArchivo);

            } catch (Exception e) {
                System.err.println("No se pudo capturar el screenshot: " + e.getMessage());
            }
        }
    }

    @Attachment(value = "Screenshot de Fallo: {nombrePrueba}", type = "image/png")
    public byte[] adjuntarScreenshotAllure(byte[] screenshot, String nombrePrueba) {
        return screenshot;
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        System.out.println(">>> [OYENTE] Prueba OMITIDA: " + result.getName());
    }

    @Override
    public void onStart(ITestContext context) {
        System.out.println("=== INICIO DE LA SUITE DE PRUEBAS ===");
    }

    @Override
    public void onFinish(ITestContext context) {
        System.out.println("=== FIN DE LA SUITE DE PRUEBAS ===");
    }
}