package dev.mikita.userservice.service;

import com.google.firebase.auth.FirebaseAuthException;
import dev.mikita.userservice.entity.Analyst;
import dev.mikita.userservice.entity.UserStatus;
import dev.mikita.userservice.repository.AnalystRepository;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class AnalystService {
    private final AnalystRepository analystRepository;

    @Autowired
    public AnalystService(AnalystRepository analystRepository) {
        this.analystRepository = analystRepository;
    }

    public List<Analyst> getAnalysts() {
        return analystRepository.findAll();
    }

    public Analyst getAnalyst(String uid) throws ExecutionException, InterruptedException, FirebaseAuthException {
        return analystRepository.find(uid);
    }

    public void createAnalyst(Analyst analyst) throws ExecutionException, InterruptedException, FirebaseAuthException {
        analystRepository.persist(analyst);
    }

    public Analyst updateAnalyst(Analyst analyst) throws ExecutionException, InterruptedException, FirebaseAuthException {
        Analyst toUpdateAnalyst = analystRepository.find(analyst.getUid());

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
        modelMapper.map(analyst, toUpdateAnalyst);

        return analystRepository.update(toUpdateAnalyst);
    }

    public void updateAnalystStatus(String uid, UserStatus status)
            throws ExecutionException, InterruptedException, FirebaseAuthException {
        Analyst analyst = analystRepository.find(uid);
        analyst.setStatus(status);
        analystRepository.update(analyst);
    }

    public void deleteAnalyst(String uid) throws FirebaseAuthException {
        analystRepository.delete(uid);
    }
}
