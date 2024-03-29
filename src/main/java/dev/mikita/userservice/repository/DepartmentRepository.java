package dev.mikita.userservice.repository;

import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseAuthException;
import dev.mikita.userservice.entity.Department;
import dev.mikita.userservice.exception.NotFoundException;
import dev.mikita.userservice.util.Pageable;
import dev.mikita.userservice.util.PagedResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Repository
public class DepartmentRepository {
    private final CollectionReference collectionReference;

    @Autowired
    public DepartmentRepository(Firestore firestore,
                                @Value("${firebase.firestore.collections.department}") String collectionName) {
        this.collectionReference = firestore.collection(collectionName);
    }

    public PagedResult<Department> findByServiceUid(String uid, Pageable pageable)
            throws ExecutionException, InterruptedException {
        List<Department> departments = new ArrayList<>();

        // Total items query
        Query totalItemsQuery = collectionReference.whereEqualTo("serviceUid", uid);

        long totalItems = totalItemsQuery.get().get().size();
        int totalPages = (int) Math.ceil((double) totalItems / pageable.getSize());

        // Items query
        Query query = collectionReference.whereEqualTo("serviceUid", uid)
                .offset(pageable.getOffset())
                .limit(pageable.getSize());

        List<QueryDocumentSnapshot> documents = query.get().get().getDocuments();
        documents.forEach(document -> departments.add(snapshotToEntity(document)));

        return new PagedResult<>(departments, pageable.getPage(), totalItems, totalPages);
    }

    public Department find(String uid) throws FirebaseAuthException, ExecutionException, InterruptedException {
        DocumentSnapshot snapshot = collectionReference.document(uid).get().get();
        if (!snapshot.exists()) throw new NotFoundException("Department not found");
        return snapshotToEntity(snapshot);
    }

    public void persist(Department department) throws ExecutionException, InterruptedException {
        collectionReference.document().set(entityToMap(department)).get();
    }

    public Department update(Department department) throws ExecutionException, InterruptedException {
        DocumentSnapshot snapshot = collectionReference.document(department.getUid()).get().get();

        Map<String, Object> departmentData = new HashMap<>();

        if (!Objects.equals(department.getName(), snapshot.getString("name"))) {
            departmentData.put("name", department.getName());
        }

        if (!Objects.equals(department.getDescription(), snapshot.getString("description"))) {
            departmentData.put("description", department.getDescription());
        }

        if (!Objects.equals(department.getAddress(), snapshot.getString("address"))) {
            departmentData.put("address", department.getAddress());
        }

        if (!Objects.equals(department.getPhoneNumber(), snapshot.getString("phoneNumber"))) {
            departmentData.put("phoneNumber", department.getPhoneNumber());
        }

        if (!Objects.equals(department.getCategories(), snapshot.get("categories"))) {
            departmentData.put("categories", department.getCategories());
        }

        // Update Firestore Document
        if (!departmentData.isEmpty()) {
            collectionReference.document(department.getUid()).update(departmentData);
        }

        return department;
    }

    public void delete(String uid) throws FirebaseAuthException {
        collectionReference.document(uid).delete();
    }

    public boolean exists(String uid) throws ExecutionException, InterruptedException {
        return collectionReference.document(uid).get().get().exists();
    }

    private Department snapshotToEntity(DocumentSnapshot snapshot) {
        Department department = new Department();

        department.setUid(snapshot.getId());
        department.setName(snapshot.getString("name"));
        department.setDescription(snapshot.getString("description"));
        department.setAddress(snapshot.getString("address"));
        department.setPhoneNumber(snapshot.getString("phoneNumber"));
        department.setCreationDate(LocalDateTime.ofInstant(
                Objects.requireNonNull(snapshot.getDate("creationDate")).toInstant(),
                ZoneId.systemDefault()));
        department.setServiceUid(snapshot.getString("serviceUid"));
        department.setCategories((List<Long>) snapshot.get("categories"));

        return department;
    }

    private Map<String, Object> entityToMap(Department department) {
        Map<String, Object> data = new HashMap<>();

        data.put("name", department.getName());
        data.put("description", department.getDescription());
        data.put("address", department.getAddress());
        data.put("phoneNumber", department.getPhoneNumber());
        data.put("creationDate", Date.from(department.getCreationDate().atZone(ZoneId.systemDefault()).toInstant()));
        data.put("categories", department.getCategories());
        data.put("serviceUid", department.getServiceUid());

        return data;
    }
}
