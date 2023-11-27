package dev.mikita.userservice.repository;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.UserRecord.CreateRequest;
import com.google.firebase.auth.UserRecord.UpdateRequest;
import dev.mikita.userservice.entity.Service;
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
 * The type Service repository.
 */
@Repository
public class ServiceRepository {
    private final CollectionReference collectionReference;
    private final FirebaseAuth firebaseAuth;
    private final Firestore firestore;

    /**
     * Instantiates a new Service repository.
     *
     * @param firebaseAuth   the firebase auth
     * @param firestore      the firestore
     * @param collectionName the collection name
     */
    @Autowired
    public ServiceRepository(FirebaseAuth firebaseAuth,
                             Firestore firestore,
                             @Value("${service.collection.name}") String collectionName) {
        this.firebaseAuth = firebaseAuth;
        this.collectionReference = firestore.collection(collectionName);
        this.firestore = firestore;
    }

    /**
     * Find service.
     *
     * @param uid the uid
     * @return the service
     * @throws FirebaseAuthException the firebase auth exception
     * @throws ExecutionException    the execution exception
     * @throws InterruptedException  the interrupted exception
     */
    public Service find(String uid) throws FirebaseAuthException, ExecutionException, InterruptedException {
        UserRecord userRecord = firebaseAuth.getUser(uid);

        if (userRecord.getCustomClaims().isEmpty() || !userRecord.getCustomClaims().get("role").toString().equals("ROLE_SERVICE")) {
            throw NotFoundException.create("Service", uid);
        }

        DocumentSnapshot snapshot = collectionReference.document(uid).get().get();

        Service service = new Service();
        service.setUid(userRecord.getUid());
        service.setEmail(userRecord.getEmail());
        service.setPhoto(userRecord.getPhotoUrl());
        service.setStatus(UserStatus.valueOf(userRecord.getCustomClaims().get("status").toString()));
        service.setName(snapshot.getString("name"));
        service.setDescription(snapshot.getString("description"));

        String role = userRecord.getCustomClaims().get("role").toString();
        String enumName = role.replace("ROLE_", "").toUpperCase();
        UserRole userRole = UserRole.valueOf(enumName);

        service.setRole(userRole);

        return service;
    }

    /**
     * Persist.
     *
     * @param service the service
     * @throws FirebaseAuthException the firebase auth exception
     */
    public void persist(Service service) throws FirebaseAuthException {
        UserRecord userRecord = firebaseAuth.createUser(new CreateRequest()
                .setEmail(service.getEmail())
                .setPassword(service.getPassword())
                .setDisplayName(service.getName())
                .setEmailVerified(false));

        // Set Custom Claims
        Map<String, Object> customClaims = new HashMap<>();
        customClaims.put("role", UserRole.SERVICE.toString());
        customClaims.put("status", UserStatus.ACTIVE.toString());

        firebaseAuth.setCustomUserClaims(userRecord.getUid(), customClaims);

        // Set Firestore Document
        Map<String, Object> data = new HashMap<>();
        data.put("name", service.getName());
        data.put("description", service.getDescription());

        collectionReference.document(userRecord.getUid()).set(data);
    }

    /**
     * Update service.
     *
     * @param service the service
     * @return the service
     * @throws FirebaseAuthException the firebase auth exception
     */
    public Service update(Service service) throws FirebaseAuthException {
        UserRecord userRecord = firebaseAuth.getUser(service.getUid());

        // Update Firebase Auth User
        UpdateRequest request = new UpdateRequest(userRecord.getUid())
                .setDisplayName(service.getName());

        if (service.getEmail() != null && !service.getEmail().equals(userRecord.getEmail())) {
            request.setEmail(service.getEmail());
            request.setEmailVerified(false);
        }

        if (service.getPassword() != null) {
            request.setPassword(service.getPassword());
        }

        firebaseAuth.updateUser(request);

        // Update Custom Claims
        Map<String, Object> customClaims = new HashMap<>();
        customClaims.put("role", UserRole.SERVICE.toString());
        customClaims.put("status", UserStatus.ACTIVE.toString());

        firebaseAuth.setCustomUserClaims(userRecord.getUid(), customClaims);

        // Update Firestore Document
        Map<String, Object> data = new HashMap<>();
        data.put("name", service.getName());
        data.put("description", service.getDescription());

        collectionReference.document(userRecord.getUid()).update(data);

        return service;
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
