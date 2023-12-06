package dev.mikita.userservice.repository;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import dev.mikita.userservice.entity.Employee;
import dev.mikita.userservice.entity.UserRole;
import dev.mikita.userservice.entity.UserStatus;
import dev.mikita.userservice.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Repository
public class EmployeeRepository {
    private final CollectionReference collectionReference;
    private final FirebaseAuth firebaseAuth;

    public EmployeeRepository(Firestore firestore,
                              FirebaseAuth firebaseAuth,
                              @Value("${employee.collection.name}") String collectionName) {
        this.firebaseAuth = firebaseAuth;
        this.collectionReference = firestore.collection(collectionName);
    }

    public Employee find(String uid) throws FirebaseAuthException, ExecutionException, InterruptedException {
        UserRecord userRecord = firebaseAuth.getUser(uid);

        if (userRecord.getCustomClaims().isEmpty()
                || !userRecord.getCustomClaims().get("role").toString().equals("EMPLOYEE")) {
            throw NotFoundException.create("Employee", uid);
        }

        return makeEmployee(userRecord, collectionReference.document(uid).get().get());
    }

    public List<Employee> findAllByDepartmentUid(String uid) throws ExecutionException, InterruptedException {
        List<Employee> employees = new ArrayList<>();
        collectionReference.whereEqualTo("departmentUid", uid).get().get().forEach(documentSnapshot -> {
            try {
                UserRecord userRecord = firebaseAuth.getUser(documentSnapshot.getId());
                employees.add(makeEmployee(userRecord, documentSnapshot));
            } catch (FirebaseAuthException e) {
                throw new RuntimeException(e);
            }
        });
        return employees;
    }

    public List<Employee> findAllByServiceUid(String uid) throws ExecutionException, InterruptedException {
        List<Employee> employees = new ArrayList<>();
        collectionReference.whereEqualTo("serviceUid", uid).get().get().forEach(documentSnapshot -> {
            try {
                UserRecord userRecord = firebaseAuth.getUser(documentSnapshot.getId());
                employees.add(makeEmployee(userRecord, documentSnapshot));
            } catch (FirebaseAuthException e) {
                throw new RuntimeException(e);
            }
        });
        return employees;
    }

    public void persist(Employee employee) throws FirebaseAuthException {
        UserRecord userRecord = firebaseAuth.createUser(new UserRecord.CreateRequest()
                .setEmail(employee.getEmail())
                .setPhoneNumber(employee.getPhoneNumber())
                .setPassword(employee.getPassword())
                .setDisplayName(employee.getFirstName() + " " + employee.getLastName())
                .setEmailVerified(true));

        // Set Custom Claims
        Map<String, Object> customClaims = new HashMap<>();
        customClaims.put("role", UserRole.EMPLOYEE.toString());
        customClaims.put("status", UserStatus.ACTIVE.toString());
        customClaims.put("serviceUid", employee.getServiceUid());
        customClaims.put("departmentUid", employee.getDepartmentUid());

        firebaseAuth.setCustomUserClaims(userRecord.getUid(), customClaims);

        // Set Firestore Document
        collectionReference.document(userRecord.getUid()).set(entityToMap(employee));
    }

    public Employee update(Employee employee) throws FirebaseAuthException, ExecutionException, InterruptedException {
        UserRecord userRecord = firebaseAuth.getUser(employee.getUid());

        // Update User Record
        UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(userRecord.getUid());

        if (!Objects.equals(employee.getFirstName() + " " + employee.getLastName(), userRecord.getDisplayName())) {
            request.setDisplayName(employee.getFirstName() + " " + employee.getLastName());
        }

        if (!Objects.equals(employee.getEmail(), userRecord.getEmail())) {
            request.setEmail(employee.getEmail());
            request.setEmailVerified(true);
        }

        if (employee.getPassword() != null) {
            request.setPassword(employee.getPassword());
        }

        if (!Objects.equals(employee.getPhoneNumber(), userRecord.getPhoneNumber())) {
            request.setPhoneNumber(employee.getPhoneNumber());
        }

        // Update Firestore Document
        DocumentSnapshot snapshot = collectionReference.document(userRecord.getUid()).get().get();
        Map<String, Object> userData = new HashMap<>();

        if (!Objects.equals(employee.getFirstName(), snapshot.getString("firstName"))) {
            userData.put("firstName", employee.getFirstName());
        }

        if (!Objects.equals(employee.getLastName(), snapshot.getString("lastName"))) {
            userData.put("lastName", employee.getLastName());
        }

        if (!Objects.equals(employee.getDepartmentUid(), snapshot.getString("departmentUid"))) {
            userData.put("departmentUid", employee.getDepartmentUid());
        }

        // Update Custom Claims
        Map<String, Object> customClaims = new HashMap<>(userRecord.getCustomClaims());

        if (!Objects.equals(employee.getStatus().toString(), customClaims.get("status").toString())) {
            customClaims.put("status", employee.getStatus().toString());
        }

        if (!Objects.equals(employee.getDepartmentUid(), customClaims.get("departmentUid").toString())) {
            customClaims.put("departmentUid", employee.getDepartmentUid());
        }

        // Update User Record
        request.setCustomClaims(customClaims);
        firebaseAuth.updateUser(request);

        // Update Firestore Document
        if (!userData.isEmpty()) {
            collectionReference.document(userRecord.getUid()).update(userData);
        }

        return employee;
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

    private Employee makeEmployee(UserRecord userRecord, DocumentSnapshot snapshot) {
        Employee employee = new Employee();

        employee.setUid(userRecord.getUid());
        employee.setEmail(userRecord.getEmail());
        employee.setPhoneNumber(userRecord.getPhoneNumber());
        employee.setPhoto(userRecord.getPhotoUrl());
        employee.setStatus(UserStatus.valueOf(userRecord.getCustomClaims().get("status").toString()));
        employee.setRole(UserRole.valueOf(userRecord.getCustomClaims().get("role").toString()));
        employee.setFirstName(snapshot.getString("firstName"));
        employee.setLastName(snapshot.getString("lastName"));
        employee.setDepartmentUid(snapshot.getString("departmentUid"));
        employee.setServiceUid(snapshot.getString("serviceUid"));

        return employee;
    }

    private Map<String, Object> entityToMap(Employee employee) {
        Map<String, Object> data = new HashMap<>();

        data.put("firstName", employee.getFirstName());
        data.put("lastName", employee.getLastName());
        data.put("serviceUid", employee.getServiceUid());
        data.put("departmentUid", employee.getDepartmentUid());

        return data;
    }
}
