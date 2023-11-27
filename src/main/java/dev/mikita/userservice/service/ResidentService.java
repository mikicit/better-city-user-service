package dev.mikita.userservice.service;

import com.google.firebase.auth.FirebaseAuthException;
import dev.mikita.userservice.exception.NotFoundException;
import dev.mikita.userservice.repository.ResidentRepository;
import dev.mikita.userservice.entity.Resident;
import dev.mikita.userservice.entity.UserStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.concurrent.ExecutionException;

/**
 * The type Resident service.
 */
@Service
public class ResidentService {
    private final ResidentRepository residentRepository;


    /**
     * Instantiates a new Resident service.
     *
     * @param residentDao the resident dao
     */
    @Autowired
    public ResidentService(ResidentRepository residentDao) {
        this.residentRepository = residentDao;
    }

    /**
     * Gets resident.
     *
     * @param uid the uid
     * @return the resident
     * @throws FirebaseAuthException the firebase auth exception
     * @throws ExecutionException    the execution exception
     * @throws InterruptedException  the interrupted exception
     */
    public Resident getResident(String uid) throws FirebaseAuthException, ExecutionException, InterruptedException {
        Resident resident = residentRepository.find(uid);
        if (resident == null || resident.getStatus() == UserStatus.DELETED) {
            throw new NotFoundException("Resident not found");
        }
        return resident;
    }

    /**
     * Create resident.
     *
     * @param resident the resident
     * @throws FirebaseAuthException the firebase auth exception
     */
    public void createResident(Resident resident) throws FirebaseAuthException {
        residentRepository.persist(resident);
    }

    /**
     * Update resident resident.
     *
     * @param resident the resident
     * @return the resident
     * @throws FirebaseAuthException the firebase auth exception
     */
    public Resident updateResident(Resident resident) throws FirebaseAuthException, ExecutionException, InterruptedException {
        if (residentRepository.find(resident.getUid()) == null) {
            throw new NotFoundException("Resident not found");
        }
        return residentRepository.update(resident);
    }

    /**
     * Delete resident.
     *
     * @param uid the uid
     * @throws FirebaseAuthException the firebase auth exception
     * @throws ExecutionException    the execution exception
     * @throws InterruptedException  the interrupted exception
     */
    public void deleteResident(String uid) throws FirebaseAuthException, ExecutionException, InterruptedException {
        Resident resident = residentRepository.find(uid);

        if (resident.getStatus() == UserStatus.DELETED) {
            throw new RuntimeException("User already deleted");
        }

        resident.setStatus(UserStatus.DELETED);
        residentRepository.update(resident);
    }
}
