package dev.mikita.userservice.repository;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.UserRecord.CreateRequest;
import com.google.firebase.auth.UserRecord.UpdateRequest;
import dev.mikita.userservice.entity.Resident;
import dev.mikita.userservice.entity.UserRole;
import dev.mikita.userservice.entity.UserStatus;
import dev.mikita.userservice.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * The type Resident repository.
 */
@Repository
public class ResidentRepository {
    private final CollectionReference collectionReference;
    private final FirebaseAuth firebaseAuth;
    private final Firestore firestore;

    /**
     * Instantiates a new Resident repository.
     *
     * @param firestore      the firestore
     * @param firebaseAuth   the firebase auth
     * @param collectionName the collection name
     */
    @Autowired
    public ResidentRepository(Firestore firestore,
                              FirebaseAuth firebaseAuth,
                              @Value("${resident.collection.name}") String collectionName) {
        this.firebaseAuth = firebaseAuth;
        this.collectionReference = firestore.collection(collectionName);
        this.firestore = firestore;
    }

    /**
     * Find resident.
     *
     * @param uid the uid
     * @return the resident
     * @throws FirebaseAuthException the firebase auth exception
     * @throws ExecutionException    the execution exception
     * @throws InterruptedException  the interrupted exception
     */
    public Resident find(String uid) throws FirebaseAuthException, ExecutionException, InterruptedException {
        UserRecord userRecord = firebaseAuth.getUser(uid);

        if (userRecord.getCustomClaims().isEmpty() || !userRecord.getCustomClaims().get("role").toString().equals("ROLE_RESIDENT")) {
            throw NotFoundException.create("Resident", uid);
        }

        DocumentSnapshot snapshot = collectionReference.document(uid).get().get();

        Resident resident = new Resident();
        resident.setUid(userRecord.getUid());
        resident.setEmail(userRecord.getEmail());
        resident.setPhoto(userRecord.getPhotoUrl());
        resident.setStatus(UserStatus.valueOf(userRecord.getCustomClaims().get("status").toString()));
        resident.setFirstName(snapshot.getString("firstName"));
        resident.setLastName(snapshot.getString("lastName"));

        String role = userRecord.getCustomClaims().get("role").toString();
        String enumName = role.replace("ROLE_", "").toUpperCase();
        UserRole userRole = UserRole.valueOf(enumName);

        resident.setRole(userRole);

        return resident;
    }

    /**
     * Persist.
     *
     * @param resident the resident
     * @throws FirebaseAuthException the firebase auth exception
     */
    public void persist(Resident resident) throws FirebaseAuthException {
        UserRecord userRecord = firebaseAuth.createUser(new CreateRequest()
                .setEmail(resident.getEmail())
                .setPassword(resident.getPassword())
                .setDisplayName(resident.getFirstName() + " " + resident.getLastName())
                .setEmailVerified(false));

        // Set Custom Claims
        Map<String, Object> customClaims = new HashMap<>();
        customClaims.put("role", UserRole.RESIDENT.toString());
        customClaims.put("status", UserStatus.ACTIVE.toString());

        firebaseAuth.setCustomUserClaims(userRecord.getUid(), customClaims);

        // Set Firestore Document
        Map<String, Object> data = new HashMap<>();
        data.put("firstName", resident.getFirstName());
        data.put("lastName", resident.getLastName());

        collectionReference.document(userRecord.getUid()).set(data);
    }

    /**
     * Update resident.
     *
     * @param resident the resident
     * @return the resident
     * @throws FirebaseAuthException the firebase auth exception
     */
    public Resident update(Resident resident) throws FirebaseAuthException {
        UserRecord userRecord = firebaseAuth.getUser(resident.getUid());

        // Update Firebase Auth User
        UpdateRequest request = new UpdateRequest(userRecord.getUid())
                .setEmail(resident.getEmail())
                .setDisplayName(resident.getFirstName() + " " + resident.getLastName());

        if (resident.getPassword() != null) {
            request.setPassword(resident.getPassword());
        }

        firebaseAuth.updateUser(request);

        // Update Custom Claims
        Map<String, Object> customClaims = new HashMap<>();
        customClaims.put("role", UserRole.RESIDENT.toString());
        customClaims.put("status", UserStatus.ACTIVE.toString());

        firebaseAuth.setCustomUserClaims(userRecord.getUid(), customClaims);

        // Update Firestore Document
        Map<String, Object> data = new HashMap<>();
        data.put("firstName", resident.getFirstName());

        data.put("lastName", resident.getLastName());

        collectionReference.document(userRecord.getUid()).update(data);

        return resident;
    }

    /**
     * Delete.
     *
     * @param uid the uid
     * @throws FirebaseAuthException the firebase auth exception
     */
    public void delete(String uid) throws FirebaseAuthException {
        firebaseAuth.deleteUser(uid);
        collectionReference.document(uid).delete();
    }
}
