package app.vin.service;

import app.user.model.User;
import app.vin.model.VinHistory;
import app.vin.repository.VinHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class VinHistoryService {

    private final VinHistoryRepository vinHistoryRepository;

    @Autowired
    public VinHistoryService(VinHistoryRepository vinHistoryRepository) {
        this.vinHistoryRepository = vinHistoryRepository;
    }

    public VinHistory saveVinCheck(User user, String vinNumber, String resultJson, 
                                  String manufacturer, String modelYear, 
                                  String assemblyPlant, String status) {
        VinHistory vinHistory = VinHistory.builder()
                .user(user)
                .vinNumber(vinNumber)
                .checkedOn(LocalDateTime.now())
                .resultJson(resultJson)
                .manufacturer(manufacturer)
                .modelYear(modelYear)
                .assemblyPlant(assemblyPlant)
                .status(status)
                .build();
        
        return vinHistoryRepository.save(vinHistory);
    }

    public List<VinHistory> getUserVinHistory(UUID userId) {
        return vinHistoryRepository.findByUserIdOrderByCheckedOnDesc(userId);
    }

    public Optional<VinHistory> findUserVinCheck(UUID userId, String vinNumber) {
        return vinHistoryRepository.findByUserIdAndVinNumber(userId, vinNumber);
    }
}