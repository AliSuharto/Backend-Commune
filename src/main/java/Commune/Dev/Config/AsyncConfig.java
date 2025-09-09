package Commune.Dev.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration pour l'exécution asynchrone des tâches
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Configuration du pool de threads pour l'envoi d'emails asynchrone
     */
    @Bean(name = "emailTaskExecutor")
    public Executor emailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);        // Nombre minimum de threads
        executor.setMaxPoolSize(5);         // Nombre maximum de threads
        executor.setQueueCapacity(100);     // Taille de la queue
        executor.setThreadNamePrefix("Email-"); // Préfixe pour les noms de threads
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
