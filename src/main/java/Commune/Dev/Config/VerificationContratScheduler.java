package Commune.Dev.Config;

import Commune.Dev.Services.ContratMonitoringService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class VerificationContratScheduler {

    private final ContratMonitoringService contratMonitoringService;

    public VerificationContratScheduler(ContratMonitoringService contratMonitoringService) {
        this.contratMonitoringService = contratMonitoringService;
    }

    // Tous les jours à 02:00
    @Scheduled(cron = "0 52 16 * * ?")
    public void executerAnalyseAutomatique() {
        contratMonitoringService.analyserContrats();
    }

}
