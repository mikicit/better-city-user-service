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
import dev.mikita.userservice.entity.Service;
import dev.mikita.userservice.entity.UserRole;
import dev.mikita.userservice.entity.UserStatus;
import dev.mikita.userservice.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * The type Service repository.
 */
@Repository
public class ServiceRepository {
    private final CollectionReference collectionReference;
    private final FirebaseAuth firebaseAuth;

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
                             @Value("${firebase.firestore.collections.service}") String collectionName) {
        this.firebaseAuth = firebaseAuth;
        this.collectionReference = firestore.collection(collectionName);
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

        if (userRecord.getCustomClaims().isEmpty() || !userRecord.getCustomClaims().get("role").toString().equals("SERVICE")) {
            throw NotFoundException.create("Service", uid);
        }

        return makeService(userRecord, collectionReference.document(uid).get().get());
    }

    public List<Service> findAll() {
        List<Service> services = new ArrayList<>();

        try {
            QuerySnapshot collection = collectionReference.get().get();

            for (DocumentSnapshot snapshot : collection.getDocuments()) {
                UserRecord userRecord = firebaseAuth.getUser(snapshot.getId());

                if (userRecord.getCustomClaims().isEmpty() || !userRecord.getCustomClaims().get("role").toString().equals("SERVICE")) {
                    continue;
                }

                services.add(makeService(userRecord, snapshot));
            }

            return services;
        } catch (Exception e) {
            return services;
        }
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
                .setEmailVerified(true));

        // Set Custom Claims
        Map<String, Object> customClaims = new HashMap<>();
        customClaims.put("role", UserRole.SERVICE.toString());
        customClaims.put("status", UserStatus.ACTIVE.toString());

        firebaseAuth.setCustomUserClaims(userRecord.getUid(), customClaims);
        collectionReference.document(userRecord.getUid()).set(entityToMap(service));
    }

    /**
     * Update service.
     *
     * @param service the service
     * @return the service
     * @throws FirebaseAuthException the firebase auth exception
     */
    public Service update(Service service) throws FirebaseAuthException, ExecutionException, InterruptedException {
        UserRecord userRecord = firebaseAuth.getUser(service.getUid());

        // Update User Record
        UpdateRequest request = new UpdateRequest(userRecord.getUid());

        if (!Objects.equals(service.getName(), userRecord.getDisplayName())) {
            request.setDisplayName(service.getName());
        }

        if (!Objects.equals(service.getEmail(), userRecord.getEmail())) {
            request.setEmail(service.getEmail());
            request.setEmailVerified(true);
        }

        if (service.getPassword() != null) {
            request.setPassword(service.getPassword());
        }

        if (!Objects.equals(service.getPhoneNumber(), userRecord.getPhoneNumber())) {
            request.setPhoneNumber(service.getPhoneNumber());
        }

        // Update Firestore Document
        DocumentSnapshot snapshot = collectionReference.document(userRecord.getUid()).get().get();
        Map<String, Object> userData = new HashMap<>();

        if (!Objects.equals(service.getName(), snapshot.getString("name"))) {
            userData.put("name", service.getName());
        }

        if (!Objects.equals(service.getDescription(), snapshot.getString("description"))) {
            userData.put("description", service.getDescription());
        }

        if (!Objects.equals(service.getAddress(), snapshot.getString("address"))) {
            userData.put("address", service.getAddress());
        }

        // Update Custom Claims
        Map<String, Object> customClaims = new HashMap<>(userRecord.getCustomClaims());

        if (!Objects.equals(service.getStatus().toString(), customClaims.get("status").toString())) {
            customClaims.put("status", service.getStatus().toString());
        }

        // Update User Record
        request.setCustomClaims(customClaims);
        firebaseAuth.updateUser(request);

        // Update Firestore Document
        if (!userData.isEmpty()) {
            collectionReference.document(userRecord.getUid()).update(userData);
        }

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

    public Long count() {
        try {
            return (long) collectionReference.get().get().size();
        } catch (Exception e) {
            return 0L;
        }
    }

    private Service makeService(UserRecord userRecord, DocumentSnapshot snapshot) {
        Service service = new Service();
        service.setUid(userRecord.getUid());
        service.setEmail(userRecord.getEmail());
        service.setPhoneNumber(userRecord.getPhoneNumber());
        service.setPhoto(userRecord.getPhotoUrl());
        service.setStatus(UserStatus.valueOf(userRecord.getCustomClaims().get("status").toString()));
        service.setCreationDate(LocalDateTime.ofInstant(Instant.ofEpochMilli(userRecord.getUserMetadata().getCreationTimestamp()), java.time.ZoneId.systemDefault()));
        service.setName(snapshot.getString("name"));
        service.setDescription(snapshot.getString("description"));
        service.setAddress(snapshot.getString("address"));

        return service;
    }

    private Map<String, Object> entityToMap(Service service) {
        Map<String, Object> data = new HashMap<>();

        data.put("name", service.getName());
        data.put("description", service.getDescription());
        data.put("address", service.getAddress());

        return data;
    }
}
