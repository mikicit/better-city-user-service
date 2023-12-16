package dev.mikita.userservice.repository;

import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.UserRecord.CreateRequest;
import com.google.firebase.auth.UserRecord.UpdateRequest;
import dev.mikita.userservice.entity.Resident;
import dev.mikita.userservice.entity.UserRole;
import dev.mikita.userservice.entity.UserStatus;
import dev.mikita.userservice.exception.NotFoundException;
import dev.mikita.userservice.util.Pageable;
import dev.mikita.userservice.util.PagedResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

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

        if (userRecord.getCustomClaims().isEmpty()
                || !userRecord.getCustomClaims().get("role").toString().equals(UserRole.RESIDENT.toString())) {
            throw NotFoundException.create("Resident", uid);
        }

        return makeResident(userRecord, collectionReference.document(uid).get().get());
    }

    public PagedResult<Resident> findAll(List<UserStatus> statuses, Pageable pageable) {
        List<Resident> residents = new ArrayList<>();

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
                        !userRecord.getCustomClaims().get("role").toString().equals(UserRole.RESIDENT.toString())) {
                    continue;
                }

                residents.add(makeResident(userRecord, snapshot));
            }

            return new PagedResult<>(residents, pageable.getPage(), totalItems, totalPages);
        } catch (Exception e) {
            return new PagedResult<>(residents, pageable.getPage(), 0, 0);
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
        Map<String, Object> data = new HashMap<>();
        data.put("firstName", resident.getFirstName());
        data.put("lastName", resident.getLastName());
        data.put("creationDate", new Date(userRecord.getUserMetadata().getCreationTimestamp()));
        data.put("status", UserStatus.ACTIVE.toString());

        collectionReference.document(userRecord.getUid()).set(data);
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

        if (!Objects.equals(resident.getStatus().toString(), snapshot.getString("status"))) {
            userData.put("status", resident.getStatus().toString());
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
        resident.setStatus(UserStatus.valueOf(snapshot.getString("status")));
        resident.setPhoto(userRecord.getPhotoUrl());
        resident.setRole(UserRole.RESIDENT);
        resident.setCreationDate(LocalDateTime.ofInstant(
                Instant.ofEpochMilli(userRecord.getUserMetadata().getCreationTimestamp()),
                java.time.ZoneId.systemDefault()));
        resident.setFirstName(snapshot.getString("firstName"));
        resident.setLastName(snapshot.getString("lastName"));

        return resident;
    }
}
