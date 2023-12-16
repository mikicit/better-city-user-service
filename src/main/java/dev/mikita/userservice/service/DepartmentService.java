package dev.mikita.userservice.service;

import com.google.firebase.auth.FirebaseAuthException;
import dev.mikita.userservice.entity.Department;
import dev.mikita.userservice.repository.DepartmentRepository;
import dev.mikita.userservice.util.Pageable;
import dev.mikita.userservice.util.PagedResult;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class DepartmentService {
    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public Department getDepartment(String uid) throws FirebaseAuthException, ExecutionException, InterruptedException {
        return departmentRepository.find(uid);
    }

    public PagedResult<Department> getDepartmentsByServiceUid(String serviceUid, Pageable pageable)
            throws ExecutionException, InterruptedException {
        return departmentRepository.findByServiceUid(serviceUid, pageable);
    }

    public void createDepartment(String serviceUid, Department department) throws ExecutionException, InterruptedException {
        department.setServiceUid(serviceUid);
        department.setCreationDate(LocalDateTime.now());
        departmentRepository.persist(department);
    }

    public Department updateDepartment(Department department)
            throws ExecutionException, InterruptedException, FirebaseAuthException {
        Department oldDepartment = departmentRepository.find(department.getUid());

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
        modelMapper.getConfiguration().setCollectionsMergeEnabled(false);
        modelMapper.map(department, oldDepartment);

        return departmentRepository.update(oldDepartment);
    }

    public void deleteDepartment(String uid) throws FirebaseAuthException {
        departmentRepository.delete(uid);
    }

    public boolean isServiceOwnerOfDepartment(String serviceUid, String departmentUid) throws ExecutionException, InterruptedException, FirebaseAuthException {
        return departmentRepository.find(departmentUid).getServiceUid().equals(serviceUid);
    }
}
