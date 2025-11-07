package com.tp.PlanificadorMat.configuracion;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración CORS para permitir comunicación con el frontend
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            // Permitir orígenes de desarrollo y producción
            .allowedOrigins(
                "http://localhost:5173",  // Vite default
                "http://localhost:3000",  // React default
                "http://127.0.0.1:5173",
                "http://127.0.0.1:3000"
            )
            // Permitir todos los métodos HTTP necesarios
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            // Permitir todos los headers
            .allowedHeaders("*")
            // Permitir credenciales
            .allowCredentials(true)
            // Tiempo de cache para preflight requests
            .maxAge(3600);
    }
}

