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
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);           // Nombre de threads minimum
        executor.setMaxPoolSize(10);           // Nombre de threads maximum
        executor.setQueueCapacity(100);        // Taille de la file d'attente
        executor.setThreadNamePrefix("email-"); // Préfixe pour identifier les threads
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}
