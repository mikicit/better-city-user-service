package dev.mikita.userservice.service;

import com.google.firebase.auth.FirebaseAuthException;
import dev.mikita.userservice.entity.Moderator;
import dev.mikita.userservice.entity.UserStatus;
import dev.mikita.userservice.exception.NotFoundException;
import dev.mikita.userservice.repository.ModeratorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

/**
 * The type Moderator service.
 */
@Service
public class ModeratorService {
    private final ModeratorRepository moderatorRepository;

    /**
     * Instantiates a new Moderator service.
     *
     * @param moderatorDao the moderator dao
     */
    @Autowired
    public ModeratorService(ModeratorRepository moderatorDao) {
        this.moderatorRepository = moderatorDao;
    }


    /**
     * Gets moderator.
     *
     * @param uid the uid
     * @return the moderator
     * @throws FirebaseAuthException the firebase auth exception
     * @throws ExecutionException    the execution exception
     * @throws InterruptedException  the interrupted exception
     */
    public Moderator getModerator(String uid) throws FirebaseAuthException, ExecutionException, InterruptedException {
        Moderator moderator = moderatorRepository.find(uid);
        if (moderator == null) {
            throw new NotFoundException("User not found.");
        }
        return moderator;
    }

    /**
     * Create moderator.
     *
     * @param moderator the moderator
     * @throws FirebaseAuthException the firebase auth exception
     */
    public void createModerator(Moderator moderator) throws FirebaseAuthException {
        moderatorRepository.persist(moderator);
    }

    /**
     * Update moderator moderator.
     *
     * @param moderator the moderator
     * @return the moderator
     * @throws FirebaseAuthException the firebase auth exception
     */
    public Moderator updateModerator(Moderator moderator) throws FirebaseAuthException, ExecutionException, InterruptedException {
        if (moderatorRepository.find(moderator.getUid()) == null) {
            throw new NotFoundException("Moderator not found.");
        }
        return moderatorRepository.update(moderator);
    }

    /**
     * Delete moderator.
     *
     * @param uid the uid
     * @throws FirebaseAuthException the firebase auth exception
     * @throws ExecutionException    the execution exception
     * @throws InterruptedException  the interrupted exception
     */
    public void deleteModerator(String uid) throws FirebaseAuthException, ExecutionException, InterruptedException {
        Moderator moderator = moderatorRepository.find(uid);

        if (moderator.getStatus() == UserStatus.DELETED) {
            throw new RuntimeException("User already deleted");
        }

        moderator.setStatus(UserStatus.DELETED);
        moderatorRepository.update(moderator);
    }
}
