package dev.mikita.userservice.repository;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.UserRecord.CreateRequest;
import com.google.firebase.auth.UserRecord.UpdateRequest;
import dev.mikita.userservice.entity.Moderator;
import dev.mikita.userservice.entity.UserRole;
import dev.mikita.userservice.entity.UserStatus;
import dev.mikita.userservice.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * The type Moderator repository.
 */
@Repository
public class ModeratorRepository {
    private final FirebaseAuth firebaseAuth;
    private final Firestore firestore;

    /**
     * Instantiates a new Moderator repository.
     *
     * @param firestore    the firestore
     * @param firebaseAuth the firebase auth
     */
    @Autowired
    public ModeratorRepository(Firestore firestore,
                               FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
        this.firestore = firestore;
    }

    /**
     * Find moderator.
     *
     * @param uid the uid
     * @return the moderator
     * @throws FirebaseAuthException the firebase auth exception
     * @throws ExecutionException    the execution exception
     * @throws InterruptedException  the interrupted exception
     */
    public Moderator find(String uid) throws FirebaseAuthException, ExecutionException, InterruptedException {
        UserRecord userRecord = firebaseAuth.getUser(uid);

        if (userRecord.getCustomClaims().isEmpty() || !userRecord.getCustomClaims().get("role").toString().equals("ROLE_MODERATOR")) {
            throw NotFoundException.create("Moderator", uid);
        }

        Moderator moderator = new Moderator();
        moderator.setUid(userRecord.getUid());
        moderator.setEmail(userRecord.getEmail());
        moderator.setPhoto(userRecord.getPhotoUrl());
        moderator.setStatus(UserStatus.valueOf(userRecord.getCustomClaims().get("status").toString()));

        String role = userRecord.getCustomClaims().get("role").toString();
        String enumName = role.replace("ROLE_", "").toUpperCase();
        UserRole userRole = UserRole.valueOf(enumName);

        moderator.setRole(userRole);

        return moderator;
    }

    /**
     * Persist.
     *
     * @param moderator the moderator
     * @throws FirebaseAuthException the firebase auth exception
     */
    public void persist(Moderator moderator) throws FirebaseAuthException {
        UserRecord userRecord = firebaseAuth.createUser(new CreateRequest()
                .setEmail(moderator.getEmail())
                .setPassword(moderator.getPassword())
                .setEmailVerified(true));

        // Set Custom Claims
        Map<String, Object> customClaims = new HashMap<>();
        customClaims.put("role", UserRole.MODERATOR.toString());
        customClaims.put("status", UserStatus.ACTIVE.toString());

        firebaseAuth.setCustomUserClaims(userRecord.getUid(), customClaims);
    }

    /**
     * Update moderator.
     *
     * @param moderator the moderator
     * @return the moderator
     * @throws FirebaseAuthException the firebase auth exception
     */
    public Moderator update(Moderator moderator) throws FirebaseAuthException {
        UserRecord userRecord = firebaseAuth.getUser(moderator.getUid());

        // Update Firebase Auth User
        UpdateRequest request = new UpdateRequest(userRecord.getUid())
                .setEmail(moderator.getEmail())
                .setEmailVerified(true);

        if (moderator.getPassword() != null) {
            request.setPassword(moderator.getPassword());
        }

        firebaseAuth.updateUser(request);

        // Update Custom Claims
        Map<String, Object> customClaims = new HashMap<>();
        customClaims.put("role", UserRole.MODERATOR.toString());
        customClaims.put("status", UserStatus.ACTIVE.toString());

        firebaseAuth.setCustomUserClaims(userRecord.getUid(), customClaims);

        return moderator;
    }

    /**
     * Delete.
     *
     * @param uid the uid
     * @throws FirebaseAuthException the firebase auth exception
     */
    public void delete(String uid) throws FirebaseAuthException {
        firebaseAuth.deleteUser(uid);
    }
}
