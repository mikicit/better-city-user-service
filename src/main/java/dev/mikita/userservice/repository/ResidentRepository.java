package dev.mikita.userservice.repository;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
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
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * The type Resident repository.
 */
@Repository
public class ResidentRepository {
    private final CollectionReference collectionReference;
    private final FirebaseAuth firebaseAuth;

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
                              @Value("${firebase.firestore.collections.resident}") String collectionName) {
        this.firebaseAuth = firebaseAuth;
        this.collectionReference = firestore.collection(collectionName);
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

        if (userRecord.getCustomClaims().isEmpty() || !userRecord.getCustomClaims().get("role").toString().equals("RESIDENT")) {
            throw NotFoundException.create("Resident", uid);
        }

        return makeResident(userRecord, collectionReference.document(uid).get().get());
    }

    public List<Resident> findAll() {
        List<Resident> residents = new ArrayList<>();

        try {
            QuerySnapshot collection = collectionReference.get().get();

            for (DocumentSnapshot snapshot : collection.getDocuments()) {
                UserRecord userRecord = firebaseAuth.getUser(snapshot.getId());

                if (userRecord.getCustomClaims().isEmpty() || !userRecord.getCustomClaims().get("role").toString().equals("RESIDENT")) {
                    continue;
                }

                residents.add(makeResident(userRecord, snapshot));
            }

            return residents;
        } catch (Exception e) {
            return residents;
        }
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
        collectionReference.document(userRecord.getUid()).set(entityToMap(resident));
    }

    /**
     * Update resident.
     *
     * @param resident the resident
     * @return the resident
     * @throws FirebaseAuthException the firebase auth exception
     */
    public Resident update(Resident resident) throws FirebaseAuthException, ExecutionException, InterruptedException {
        UserRecord userRecord = firebaseAuth.getUser(resident.getUid());

        // Update User Record
        UpdateRequest request = new UpdateRequest(userRecord.getUid());

        if (!Objects.equals(resident.getFirstName() + " " + resident.getLastName(), userRecord.getDisplayName())) {
            request.setDisplayName(resident.getFirstName() + " " + resident.getLastName());
        }

        if (!Objects.equals(resident.getEmail(), userRecord.getEmail())) {
            request.setEmail(resident.getEmail());
            request.setEmailVerified(false);
        }

        if (resident.getPassword() != null) {
            request.setPassword(resident.getPassword());
        }

        if (!Objects.equals(resident.getPhoneNumber(), userRecord.getPhoneNumber())) {
            request.setPhoneNumber(resident.getPhoneNumber());
        }

        // Update Firestore Document
        DocumentSnapshot snapshot = collectionReference.document(userRecord.getUid()).get().get();
        Map<String, Object> userData = new HashMap<>();

        if (!Objects.equals(resident.getFirstName(), snapshot.getString("firstName"))) {
            userData.put("firstName", resident.getFirstName());
        }

        if (!Objects.equals(resident.getLastName(), snapshot.getString("lastName"))) {
            userData.put("lastName", resident.getLastName());
        }

        // Update Custom Claims
        Map<String, Object> customClaims = new HashMap<>(userRecord.getCustomClaims());

        if (!Objects.equals(resident.getStatus().toString(), customClaims.get("status").toString())) {
            customClaims.put("status", resident.getStatus().toString());
        }

        // Update User Record
        request.setCustomClaims(customClaims);
        firebaseAuth.updateUser(request);

        // Update Firestore Document
        if (!userData.isEmpty()) {
            collectionReference.document(userRecord.getUid()).update(userData);
        }

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

    private Resident makeResident(UserRecord userRecord, DocumentSnapshot snapshot) {
        Resident resident = new Resident();

        resident.setUid(userRecord.getUid());
        resident.setEmail(userRecord.getEmail());
        resident.setStatus(UserStatus.valueOf(userRecord.getCustomClaims().get("status").toString()));
        resident.setPhoto(userRecord.getPhotoUrl());
        resident.setRole(UserRole.valueOf(userRecord.getCustomClaims().get("role").toString()));
        resident.setFirstName(snapshot.getString("firstName"));
        resident.setLastName(snapshot.getString("lastName"));

        return resident;
    }

    private Map<String, Object> entityToMap(Resident resident) {
        Map<String, Object> data = new HashMap<>();

        data.put("firstName", resident.getFirstName());
        data.put("lastName", resident.getLastName());

        return data;
    }
}
