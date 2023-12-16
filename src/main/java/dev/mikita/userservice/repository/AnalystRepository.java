package dev.mikita.userservice.repository;

import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import dev.mikita.userservice.entity.*;
import dev.mikita.userservice.exception.NotFoundException;
import dev.mikita.userservice.util.Pageable;
import dev.mikita.userservice.util.PagedResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

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

        if (userRecord.getCustomClaims().isEmpty() ||
                !userRecord.getCustomClaims().get("role").toString().equals(UserRole.ANALYST.toString())) {
            throw NotFoundException.create("Analyst", uid);
        }

        return makeAnalyst(userRecord, collectionReference.document(uid).get().get());
    }

    // Piece of shit. Choosing firebase for this project was a mistake :(
    public PagedResult<Analyst> findAll(List<UserStatus> statuses, Pageable pageable) {
        List<Analyst> analysts = new ArrayList<>();

        try {
            // Total items query
            Query totalItemsQuery;
            if (statuses != null) {
                totalItemsQuery = collectionReference.whereIn("status", statuses.stream()
                        .map(UserStatus::toString)
                        .collect(Collectors.toList()));
            } else {
                totalItemsQuery = collectionReference;
            }

            long totalItems = totalItemsQuery.get().get().size();
            int totalPages = (int) Math.ceil((double) totalItems / pageable.getSize());

            // Items query
            Query query;
            if (statuses != null) {
                query = collectionReference.whereIn("status", statuses.stream()
                        .map(UserStatus::toString)
                        .collect(Collectors.toList()))
                        .orderBy("status", pageable.getSortDirection())
                        .offset(pageable.getOffset())
                        .limit(pageable.getSize());
            } else {
                query = collectionReference
                    .orderBy(pageable.getSortBy(), pageable.getSortDirection())
                    .offset(pageable.getOffset())
                    .limit(pageable.getSize());
            }

            QuerySnapshot querySnapshot = query.get().get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();

            for (QueryDocumentSnapshot snapshot : documents) {
                UserRecord userRecord = firebaseAuth.getUser(snapshot.getId());

                if (userRecord.getCustomClaims().isEmpty() ||
                        !userRecord.getCustomClaims().get("role").toString().equals(UserRole.ANALYST.toString())) {
                    continue;
                }

                analysts.add(makeAnalyst(userRecord, snapshot));
            }

            return new PagedResult<>(analysts, pageable.getPage(), totalItems, totalPages);
        } catch (Exception e) {
            return new PagedResult<>(analysts, pageable.getPage(), 0, 0);
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
        Map<String, Object> data = new HashMap<>();
        data.put("name", analyst.getName());
        data.put("description", analyst.getDescription());
        data.put("creationDate", new Date(userRecord.getUserMetadata().getCreationTimestamp()));
        data.put("status", UserStatus.ACTIVE.toString());

        collectionReference.document(userRecord.getUid()).set(data);
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

        if (!Objects.equals(analyst.getStatus().toString(), snapshot.getString("status"))) {
            userData.put("status", analyst.getStatus().toString());
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
        analyst.setPhoto(userRecord.getPhotoUrl());
        analyst.setStatus(UserStatus.valueOf(snapshot.getString("status")));
        analyst.setRole(UserRole.ANALYST);
        analyst.setCreationDate(LocalDateTime.ofInstant(
                Instant.ofEpochMilli(userRecord.getUserMetadata().getCreationTimestamp()),
                java.time.ZoneId.systemDefault()));
        analyst.setName(snapshot.getString("name"));
        analyst.setDescription(snapshot.getString("description"));

        return analyst;
    }
}
