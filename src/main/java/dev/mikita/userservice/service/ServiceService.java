package dev.mikita.userservice.service;

import com.google.firebase.auth.FirebaseAuthException;
import dev.mikita.userservice.repository.ServiceRepository;
import dev.mikita.userservice.entity.UserStatus;
import dev.mikita.userservice.util.Pageable;
import dev.mikita.userservice.util.PagedResult;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * The type Service service.
 */
@Service
public class ServiceService {
    private final ServiceRepository serviceRepository;

    /**
     * Instantiates a new Service service.
     *
     * @param serviceDao  the service dao
     */
    @Autowired
    public ServiceService(ServiceRepository serviceDao) {
        this.serviceRepository = serviceDao;
    }

    /**
     * Gets service.
     *
     * @param uid the uid
     * @return the service
     * @throws FirebaseAuthException the firebase auth exception
     * @throws ExecutionException    the execution exception
     * @throws InterruptedException  the interrupted exception
     */
    public dev.mikita.userservice.entity.Service getService(String uid) throws FirebaseAuthException, ExecutionException, InterruptedException {
        return serviceRepository.find(uid);
    }

    public PagedResult<dev.mikita.userservice.entity.Service> getServices(List<UserStatus> statuses, Pageable pageable) {
        return serviceRepository.findAll(statuses, pageable);
    }

    public Long getServicesCount() {
        return serviceRepository.count();
    }

    /**
     * Create service.
     *
     * @param service the service
     * @throws FirebaseAuthException the firebase auth exception
     */
    public void createService(dev.mikita.userservice.entity.Service service) throws FirebaseAuthException {
        serviceRepository.persist(service);
    }

    /**
     * Update service dev.mikita.userservice.entity.service.
     *
     * @param service the service
     * @return the dev.mikita.userservice.entity.service
     * @throws FirebaseAuthException the firebase auth exception
     */
    public dev.mikita.userservice.entity.Service updateService(dev.mikita.userservice.entity.Service service)
            throws FirebaseAuthException, ExecutionException, InterruptedException {
        dev.mikita.userservice.entity.Service toUpdateService = serviceRepository.find(service.getUid());

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
        modelMapper.map(service, toUpdateService);

        return serviceRepository.update(toUpdateService);
    }

    public void updateServiceStatus(String uid, UserStatus status)
            throws ExecutionException, InterruptedException, FirebaseAuthException {
        dev.mikita.userservice.entity.Service service = serviceRepository.find(uid);
        service.setStatus(status);
        serviceRepository.update(service);
    }

    /**
     * Delete service.
     *
     * @param uid the uid
     * @throws FirebaseAuthException the firebase auth exception
     * @throws ExecutionException    the execution exception
     * @throws InterruptedException  the interrupted exception
     */
    public void deleteService(String uid) throws FirebaseAuthException, ExecutionException, InterruptedException {
        dev.mikita.userservice.entity.Service service = serviceRepository.find(uid);

        if (service.getStatus() == UserStatus.DELETED) {
            throw new RuntimeException("Service already deleted.");
        }

        service.setStatus(UserStatus.DELETED);
        serviceRepository.update(service);
    }
}
