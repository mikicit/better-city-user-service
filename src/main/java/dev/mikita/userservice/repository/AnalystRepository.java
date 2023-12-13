package dev.mikita.userservice.repository;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import dev.mikita.userservice.entity.*;
import dev.mikita.userservice.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Repository
public class AnalystRepository {
    private final CollectionReference collectionReference;
    private final FirebaseAuth firebaseAuth;

    @Autowired
    public AnalystRepository(Firestore firestore,
                             FirebaseAuth firebaseAuth,
                             @Value("${firebase.firestore.collections.analyst}") String collectionName) {
        this.firebaseAuth = firebaseAuth;
        this.collectionReference = firestore.collection(collectionName);
    }

    public Analyst find(String uid) throws ExecutionException, InterruptedException, FirebaseAuthException {
        UserRecord userRecord = firebaseAuth.getUser(uid);

        if (userRecord.getCustomClaims().isEmpty() || !userRecord.getCustomClaims().get("role").toString().equals("ANALYST")) {
            throw NotFoundException.create("Analyst", uid);
        }

        return makeAnalyst(userRecord, collectionReference.document(uid).get().get());
    }

    public List<Analyst> findAll() {
        List<Analyst> analysts = new ArrayList<>();

        try {
            QuerySnapshot collection = collectionReference.get().get();

            for (DocumentSnapshot snapshot : collection.getDocuments()) {
                UserRecord userRecord = firebaseAuth.getUser(snapshot.getId());

                if (userRecord.getCustomClaims().isEmpty() ||
                        !userRecord.getCustomClaims().get("role").toString().equals("ANALYST")) {
                    continue;
                }

                analysts.add(makeAnalyst(userRecord, snapshot));
            }

            return analysts;
        } catch (Exception e) {
            return analysts;
        }
    }

    public void persist(Analyst analyst) throws ExecutionException, InterruptedException, FirebaseAuthException {
        UserRecord userRecord = firebaseAuth.createUser(new UserRecord.CreateRequest()
                .setEmail(analyst.getEmail())
                .setPassword(analyst.getPassword())
                .setDisplayName(analyst.getName())
                .setEmailVerified(true));

        // Set Custom Claims
        Map<String, Object> customClaims = new HashMap<>();
        customClaims.put("role", UserRole.ANALYST.toString());
        customClaims.put("status", UserStatus.ACTIVE.toString());

        firebaseAuth.setCustomUserClaims(userRecord.getUid(), customClaims);

        // Set Firestore Document
        collectionReference.document(userRecord.getUid()).set(entityToMap(analyst));
    }

    public Analyst update(Analyst analyst) throws ExecutionException, InterruptedException, FirebaseAuthException {
        UserRecord userRecord = firebaseAuth.getUser(analyst.getUid());

        // Update User Record
        UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(userRecord.getUid());

        if (!Objects.equals(analyst.getName(), userRecord.getDisplayName())) {
            request.setDisplayName(analyst.getName());
        }

        if (!Objects.equals(analyst.getEmail(), userRecord.getEmail())) {
            request.setEmail(analyst.getEmail());
            request.setEmailVerified(true);
        }

        if (analyst.getPassword() != null) {
            request.setPassword(analyst.getPassword());
        }

        // Update Firestore Document
        DocumentSnapshot snapshot = collectionReference.document(userRecord.getUid()).get().get();
        Map<String, Object> userData = new HashMap<>();

        if (!Objects.equals(analyst.getName(), snapshot.getString("name"))) {
            userData.put("name", analyst.getName());
        }

        if (!Objects.equals(analyst.getDescription(), snapshot.getString("description"))) {
            userData.put("description", analyst.getDescription());
        }

        // Update Custom Claims
        Map<String, Object> customClaims = new HashMap<>(userRecord.getCustomClaims());

        if (!Objects.equals(analyst.getStatus().toString(), customClaims.get("status").toString())) {
            customClaims.put("status", analyst.getStatus().toString());
        }

        // Update User Record
        request.setCustomClaims(customClaims);
        firebaseAuth.updateUser(request);

        // Update Firestore Document
        if (!userData.isEmpty()) {
            collectionReference.document(userRecord.getUid()).update(userData);
        }

        return analyst;
    }

    public void delete(String uid) throws FirebaseAuthException {
        firebaseAuth.deleteUser(uid);
        collectionReference.document(uid).delete();
    }

    private Analyst makeAnalyst(UserRecord userRecord, DocumentSnapshot snapshot) {
        Analyst analyst = new Analyst();

        analyst.setUid(userRecord.getUid());
        analyst.setEmail(userRecord.getEmail());
        analyst.setStatus(UserStatus.valueOf(userRecord.getCustomClaims().get("status").toString()));
        analyst.setPhoto(userRecord.getPhotoUrl());
        analyst.setRole(UserRole.valueOf(userRecord.getCustomClaims().get("role").toString()));
        analyst.setCreationDate(LocalDateTime.ofInstant(
                Instant.ofEpochMilli(userRecord.getUserMetadata().getCreationTimestamp()),
                java.time.ZoneId.systemDefault()));
        analyst.setName(snapshot.getString("name"));
        analyst.setDescription(snapshot.getString("description"));

        return analyst;
    }

    private Map<String, Object> entityToMap(Analyst analyst) {
        Map<String, Object> data = new HashMap<>();

        data.put("name", analyst.getName());
        data.put("description", analyst.getDescription());

        return data;
    }
}
