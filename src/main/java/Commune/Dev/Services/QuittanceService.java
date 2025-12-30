package Commune.Dev.Services;

import Commune.Dev.Dtos.QuittanceDTO;
import Commune.Dev.Repositories.QuittanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuittanceService {

    private final QuittanceRepository quittanceRepository;

    @Transactional(readOnly = true)
    public List<QuittanceDTO> getQuittancesByPercepteurId(Long percepteurId) {
        if (percepteurId == null) {
            throw new IllegalArgumentException("Le percepteurId ne peut pas être null");
        }
        return quittanceRepository.findQuittanceDTOByPercepteurId(percepteurId);
    }
}
